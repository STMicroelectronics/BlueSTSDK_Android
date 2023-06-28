/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.audio.opus

const val BV_OPUS_FRAME_SIZE_2_5 = 0x20.toByte()
const val BV_OPUS_FRAME_SIZE_5 = 0x21.toByte()
const val BV_OPUS_FRAME_SIZE_10 = 0x22.toByte()
const val BV_OPUS_FRAME_SIZE_20 = 0x23.toByte()
const val BV_OPUS_FRAME_SIZE_40 = 0x24.toByte()
const val BV_OPUS_FRAME_SIZE_60 = 0x25.toByte()

const val BV_OPUS_SAMPLING_FREQ_8 = 0x30.toByte()
const val BV_OPUS_SAMPLING_FREQ_16 = 0x31.toByte()
const val BV_OPUS_SAMPLING_FREQ_24 = 0x32.toByte()
const val BV_OPUS_SAMPLING_FREQ_48 = 0x33.toByte()

const val BV_OPUS_CHANNELS_1 = 0x40.toByte()
const val BV_OPUS_CHANNELS_2 = 0x41.toByte()

const val OPUS_DEC_MS = 20.toFloat()
const val OPUS_DEC_SAMPLING_FREQ = 16000
const val OPUS_DEC_CHANNELS = 1
const val OPUS_DEC_FRAME_SIZE_PCM =
    ((OPUS_DEC_SAMPLING_FREQ / 1000) * OPUS_DEC_MS).toInt()