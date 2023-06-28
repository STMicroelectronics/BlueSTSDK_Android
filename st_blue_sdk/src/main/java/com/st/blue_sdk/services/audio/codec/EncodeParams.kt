/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio.codec

interface EncodeParams {
    val frameSize: Int

    val samplingFreq: Int

    val channels: Int

    val frameSizePCM: Int

    val application: Int

    fun bitRate(isLe2MPhySupported: Boolean): Int =
        if (isLe2MPhySupported) highBitRate else lowBitRate

    val highBitRate: Int

    val lowBitRate: Int

    val isVbr: Boolean

    val complexity: Int
}
