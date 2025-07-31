/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

@file:Suppress("PackageName")

package com.st.BlueSTSDK.Features.Audio.Opus

class OpusManager {

    init {
        System.loadLibrary("opusinterface")
    }

    fun decoderInit(sampFreq: Int, channels: Int): Int = OpusDecInit(sampFreq, channels)

    private external fun OpusDecInit(sampFreq: Int, channels: Int): Int

    fun decode(input: ByteArray, in_length: Int, frameSizePcm: Int) =
        OpusDecode(input, in_length, frameSizePcm)

    private external fun OpusDecode(input: ByteArray, in_length: Int, frameSizePcm: Int): ByteArray?

    fun encoderInit(
        sampFreq: Int,
        channels: Int,
        app: Int,
        bitrate: Int,
        cvbr: Boolean,
        complexity: Int
    ) = OpusEncInit(sampFreq, channels, app, bitrate, cvbr, complexity)

    private external fun OpusEncInit(
        sampFreq: Int,
        channels: Int,
        app: Int,
        bitrate: Int,
        cvbr: Boolean,
        complexity: Int
    ): Int

    fun encode(
        input: ShortArray,
        encodedFrameSize: Int,
        frameSizePcm: Int,
        channels: Int
    ) = OpusEncode(input, encodedFrameSize, frameSizePcm, channels)

    external fun OpusEncode(
        input: ShortArray,
        encodedFrameSize: Int,
        frameSizePcm: Int,
        channels: Int
    ): ByteArray?
}
