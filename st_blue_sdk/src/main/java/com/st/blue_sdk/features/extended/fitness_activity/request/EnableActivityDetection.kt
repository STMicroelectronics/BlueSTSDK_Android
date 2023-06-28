/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.fitness_activity.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.fitness_activity.FitnessActivity
import com.st.blue_sdk.features.extended.fitness_activity.FitnessActivityType

class EnableActivityDetection(
    feature: FitnessActivity,
    val activityType: FitnessActivityType
) : FeatureCommand(feature = feature, commandId = ENABLE_FITNESS_ACTIVITY) {
    companion object {
        const val ENABLE_FITNESS_ACTIVITY = 1.toByte()
    }
}