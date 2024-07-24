/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.demo.R
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

@Composable
fun AudioScreen(
    navController: NavHostController,
    viewModel: AudioScreenViewModel,
    deviceId: String,
    modifier: Modifier = Modifier
) {

    BackHandler(true) {
        viewModel.stopAudioDemo(deviceId)
        navController.navigateUp()
    }

    LaunchedEffect(key1 = deviceId) {
        viewModel.testAudio(deviceId)
            .onStart {
                initAudioTrack(viewModel.getAudioDecodeParams(deviceId))
                viewModel.startAudioRecord(deviceId)
            }
            .onEach { playAudio(it) }
            .launchIn(this)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(text = stringResource(R.string.st_audioScreenTitle))
    }
}

private var audioTrack: AudioTrack? = null

private fun initAudioTrack(decodeParams: DecodeParams) {

    val minBufSize = AudioTrack.getMinBufferSize(
        decodeParams.samplingFreq,
        if (decodeParams.channels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    audioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC,
        decodeParams.samplingFreq,
        decodeParams.channels,
        AudioFormat.ENCODING_PCM_16BIT,
        minBufSize,
        AudioTrack.MODE_STREAM
    )

    audioTrack?.play()
}

fun playAudio(sample: ByteArray) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        audioTrack?.write(
            sample, 0, sample.size,
            AudioTrack.WRITE_NON_BLOCKING
        )
    }
}