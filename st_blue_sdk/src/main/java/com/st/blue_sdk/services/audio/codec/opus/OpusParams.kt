/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio.codec.opus

import com.st.blue_sdk.features.extended.audio.opus.OPUS_DEC_CHANNELS
import com.st.blue_sdk.features.extended.audio.opus.OPUS_DEC_MS
import com.st.blue_sdk.features.extended.audio.opus.OPUS_DEC_SAMPLING_FREQ
import com.st.blue_sdk.services.audio.codec.DecodeParams

data class OpusParams(
    val frameSize: Float,
    override var samplingFreq: Int,
    override var channels: Int
) : DecodeParams {
    companion object {
        fun getDefault() = OpusParams(
            frameSize = OPUS_DEC_MS,
            samplingFreq = OPUS_DEC_SAMPLING_FREQ,
            channels = OPUS_DEC_CHANNELS,
        )
    }
}
