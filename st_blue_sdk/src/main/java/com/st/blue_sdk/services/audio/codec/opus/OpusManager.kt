/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio.codec.opus

import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusConf
import com.st.blue_sdk.features.extended.audio.opus.OPUS_DEC_FRAME_SIZE_PCM
import com.st.blue_sdk.services.audio.codec.AudioCodecManager
import com.st.blue_sdk.services.audio.codec.CodecType
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.codec.EncodeParams
import com.st.blue_sdk.utils.BlueVoiceOpusTransportProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.st.BlueSTSDK.Features.Audio.Opus.OpusManager as Codec

class OpusManager(
    private val coroutineScope: CoroutineScope,
    private val isLe2MPhySupported: Boolean
) : AudioCodecManager {

    override val type: CodecType = CodecType.OPUS
    override val codecName: String = "Opus"
    override val channels: Int = 1
    override val samplingFreq: Int = 8000
    override var isAudioEnabled: Boolean = true
        private set

    private lateinit var encodeParams: EncodeParams
    private var decodeParams: OpusParams = OpusParams.getDefault()
    private var buffer: ShortArray
    private var transportProtocol: BlueVoiceOpusTransportProtocol
    private val codec = Codec()

    private var configCollectionJob: Job? = null

    init {
        codec.decoderInit(decodeParams.samplingFreq, decodeParams.channels)
        buffer = ShortArray(size = OPUS_DEC_FRAME_SIZE_PCM)
        transportProtocol = BlueVoiceOpusTransportProtocol(OPUS_DEC_FRAME_SIZE_PCM * 2)
    }

    override suspend fun init(configFlow: Flow<FeatureUpdate<*>>): Boolean {
        configCollectionJob?.cancel()
        configCollectionJob = configFlow.onEach { update ->
            when (update.data) {
                is AudioOpusConf -> {
                    if (update.data.isConfCommand()) {
                        update.data.onOff.value?.let { audioEnabled ->
                            enable(audioEnabled)
                        }
                    } else {
                        update.data.channels.value?.let { channels ->
                            update.data.samplingFreq.value?.let { samplingFreq ->
                                update.data.frameSize.value?.let { frameSize ->
                                    setDecodeParams(
                                        OpusParams(
                                            frameSize = frameSize,
                                            samplingFreq = samplingFreq,
                                            channels = channels
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }.launchIn(coroutineScope)

        return true
    }

    override fun enable(enable: Boolean) {
        isAudioEnabled = enable
    }

    override fun setEncodeParams(params: EncodeParams) {
        encodeParams = params

        codec.encoderInit(
            encodeParams.samplingFreq,
            encodeParams.channels,
            encodeParams.application,
            encodeParams.bitRate(isLe2MPhySupported = isLe2MPhySupported),
            encodeParams.isVbr,
            encodeParams.complexity
        )
    }

    private fun setDecodeParams(params: DecodeParams) {
        decodeParams = params as OpusParams

        codec.decoderInit(decodeParams.samplingFreq, decodeParams.channels)
        val opusDecFrameSizePCM =
            (((decodeParams.samplingFreq / 1000) * decodeParams.frameSize)).toInt()
        transportProtocol = BlueVoiceOpusTransportProtocol(opusDecFrameSizePCM)
        buffer = ShortArray(size = opusDecFrameSizePCM)
    }

    override fun getDecodeParams(): DecodeParams = decodeParams

    override fun encode(data: ShortArray): ByteArray {

        return codec.encode(
            data,
            encodeParams.frameSize,
            encodeParams.frameSizePCM,
            encodeParams.channels
        ) ?: byteArrayOf()
    }

    private fun decodeFrame(data: ByteArray): ShortArray {
        val opusDecFrameSizePCM =
            (((decodeParams.samplingFreq / 1000) * decodeParams.frameSize)).toInt()
        codec.decode(data, data.size, opusDecFrameSizePCM)?.let {
            for (i in 0 until it.size / 2) {
                val low = it[2 * i].toInt() and 0xFF
                val high = (it[2 * i + 1].toInt() and 0xFF) shl 8
                buffer[i] = (low or high).toShort()
            }
        }

        return buffer
    }

    override fun decode(data: ByteArray): ShortArray =
        transportProtocol.unpackData(data)?.let { opusFrame ->
            decodeFrame(opusFrame)
        } ?: ShortArray(size = 0)

    override fun reset() = Unit

    override fun destroy() {
        configCollectionJob?.cancel()
    }
}