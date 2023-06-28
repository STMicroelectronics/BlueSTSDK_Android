/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio.codec.opus

import com.st.blue_sdk.services.audio.codec.EncodeParams

class OpusParamsFullBand : EncodeParams {
    override val frameSize: Int = 300
    override val samplingFreq: Int = 48000
    override val channels: Int = 2
    override val frameSizePCM: Int = 1920
    override val application: Int = 2049
    override val highBitRate: Int = 192000
    override val lowBitRate: Int = 96000
    override val isVbr: Boolean = false
    override val complexity: Int = 4
}