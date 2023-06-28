/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.fitness_activity

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class FitnessActivityInfo(
    val activity: FeatureField<FitnessActivityType>,
    val count: FeatureField<Int>
) : Loggable {
    override val logHeader: String = "${activity.logHeader}, ${count.logHeader}"

    override val logValue: String = "${activity.logValue}, ${count.logValue}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${activity.name} = ${activity.value}\n")
        sampleValue.append("\t${count.name} = ${count.value}\n")
        return sampleValue.toString()
    }
}

enum class FitnessActivityType {
    NoActivity,
    BicepCurl,
    Squat,
    PushUp,
    Error
}