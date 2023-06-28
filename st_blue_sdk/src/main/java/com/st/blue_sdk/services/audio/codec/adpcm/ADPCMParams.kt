/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio.codec.adpcm

import com.st.blue_sdk.services.audio.codec.DecodeParams

data class ADPCMParams(
    override var samplingFreq: Int,
    override var channels: Int,
    val index: Short,
    val predSample: Int,
) : DecodeParams