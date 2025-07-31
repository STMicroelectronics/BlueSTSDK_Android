/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.beam_forming.BeamDirectionType
import com.st.blue_sdk.features.beam_forming.BeamForming
import com.st.blue_sdk.features.beam_forming.request.ChangeBeamFormingDirection
import com.st.blue_sdk.features.beam_forming.request.EnableDisableBeamForming
import com.st.blue_sdk.features.beam_forming.request.UseStrongBeamFormingAlgorithm
import com.st.blue_sdk.services.audio.AudioService
import com.st.blue_sdk.services.audio.codec.CodecType
import com.st.blue_sdk.services.audio.codec.opus.OpusParamsFullDuplex
import com.st.blue_sdk.services.audio.toByteArray
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transform
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AudioScreenViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val audioService: AudioService
) : ViewModel() {

    companion object {
        private val TAG = AudioScreenViewModel::class.simpleName
    }

    private lateinit var audioRecord: AudioRecord

    suspend fun testAudio(nodeId: String): Flow<ByteArray> {

        if (audioService.init(nodeId).not()) {
            Log.e(TAG, "Audio codec initialization failed")
            return flowOf()
        }

        delay(1000)

        if (audioService.getCodecType(nodeId) == CodecType.ADPCM) {
            blueManager.nodeFeatures(nodeId)
                .firstOrNull { featureName -> featureName.name == BeamForming.NAME }
                ?.let { feature ->
                    Log.d(TAG, "Send BeamForming commands")

                    val beamFormingFeature = feature as BeamForming

                    blueManager.enableFeatures(nodeId, listOf(feature))

                    val enableBeamFormingCommand =
                        EnableDisableBeamForming(beamFormingFeature, true)
                    blueManager.writeFeatureCommand(nodeId, enableBeamFormingCommand, 0)

                    val useStrongAlgorithm = UseStrongBeamFormingAlgorithm(beamFormingFeature, true)
                    blueManager.writeFeatureCommand(nodeId, useStrongAlgorithm, 0)

                    val changeDirection =
                        ChangeBeamFormingDirection(
                            beamFormingFeature,
                            BeamDirectionType.Right
                        )
                    blueManager.writeFeatureCommand(nodeId, changeDirection, 0)
                }
        }

        return audioService.startDecodingIncomingAudioStream(nodeId).transform {
            emit(it.toByteArray())
        }
    }

    fun getAudioDecodeParams(nodeId: String) = audioService.getDecodeParams(nodeId = nodeId)

    fun startAudioRecord(nodeId: String) {

        audioRecord = initAudioRecord(16000, 1)

        audioRecord.startRecording()
        audioService.enableAudio(nodeId)
        audioService.setEncodeParams(nodeId, OpusParamsFullDuplex())

        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "${exception.message}", exception)
        }

//        viewModelScope.launch(handler) {
//            withContext(Dispatchers.IO) {
//                val buffer = ShortArray(320)
//                while (isActive) {
//                    audioRecord.read(buffer, 0, 320)
//                    audioService.sendVoiceAudioStream(nodeId, buffer)
//                    delay(5)
//                }
//            }
//        }

        val buffer1 = ShortArray(320)
        val buffer2 = ShortArray(320)
//        var isActiveBuffer1 = true
//
//
//
//        viewModelScope.launch(handler) {
//            withContext(Dispatchers.IO) {
//                while (isActive) {
//                    val start = Date().time
//                    if(isActiveBuffer1) {
//                        audioRecord.read(buffer1, 0, 320)
//                    } else {
//                        audioRecord.read(buffer2, 0, 320)
//                    }
//                    val sleepTime = 20-(Date().time-start)
//                    if(sleepTime>0) {
//                        delay(sleepTime)
//                    }
//                }
//            }
//        }
//
//        viewModelScope.launch(handler) {
//            withContext(Dispatchers.IO) {
//                while (isActive) {
//                    val start = Date().time
//                    isActiveBuffer1 = if(isActiveBuffer1) {
//                        audioService.sendVoiceAudioStream(nodeId, buffer2)
//                        false
//                    } else {
//                        audioService.sendVoiceAudioStream(nodeId, buffer1)
//                        true
//                    }
//                    val sleepTime = 20-(Date().time-start)
//                    if(sleepTime>0) {
//                        delay(sleepTime)
//                    }
//                }
//            }
//        }

        val isCurrentBuffer1 : Flow<Boolean> = flow {
            viewModelScope.launch(handler) {
                withContext(Dispatchers.IO) {
                    var isActiveBuffer1 = true
                    while (isActive) {
                        val start = Date().time
                        isActiveBuffer1 = if (isActiveBuffer1) {
                            audioRecord.read(buffer1, 0, 320)
                            emit(true)
                            false
                        } else {
                            audioRecord.read(buffer2, 0, 320)
                            emit(false)
                            true
                        }
                        val sleepTime = 20 - (Date().time - start)
                        if(sleepTime>0) {
                            delay(sleepTime)
                        }
                    }
                }
            }
        }

        viewModelScope.launch(handler) {
            withContext(Dispatchers.IO) {
                isCurrentBuffer1.collect {
                    if(it) {
                        audioService.sendVoiceAudioStream(nodeId, buffer1)
                    } else {
                        audioService.sendVoiceAudioStream(nodeId, buffer2)
                    }
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun initAudioRecord(samplingFreq: Int, channels: Short): AudioRecord {

        val ch =
            if (channels.toInt() == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO

        val minBufSize = AudioTrack.getMinBufferSize(
            samplingFreq,
            ch,
            AudioFormat.ENCODING_PCM_16BIT
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(samplingFreq)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                )
                .build()
        } else {
            AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                samplingFreq,
                channels.toInt(),
                AudioFormat.ENCODING_PCM_16BIT,
                minBufSize
            )
        }
    }

    fun stopAudioDemo(nodeId: String) {
        audioService.destroy(nodeId)
    }
}