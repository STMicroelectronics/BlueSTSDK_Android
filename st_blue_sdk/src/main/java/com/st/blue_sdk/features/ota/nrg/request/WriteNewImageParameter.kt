/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota.nrg.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.ota.nrg.NewImageFeature

class WriteNewImageParameter(
    feature: NewImageFeature,
    val otaAckEvery: Byte,
    val imageSize: Long,
    val baseAddress: Long
) : FeatureCommand(feature = feature, commandId = NewImageFeature.SEND_PARAMETER)