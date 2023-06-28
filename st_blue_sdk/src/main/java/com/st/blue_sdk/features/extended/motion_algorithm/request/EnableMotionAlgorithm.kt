/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.motion_algorithm.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.motion_algorithm.AlgorithmType
import com.st.blue_sdk.features.extended.motion_algorithm.MotionAlgorithm

class EnableMotionAlgorithm(
    feature: MotionAlgorithm,
    val algorithmType: AlgorithmType
) : FeatureCommand(feature = feature, commandId = ENABLE_MOTION_ALGORITHM) {
    companion object {
        const val ENABLE_MOTION_ALGORITHM = 1.toByte()
    }
}