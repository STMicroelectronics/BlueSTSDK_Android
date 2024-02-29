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
import kotlin.experimental.and

@Serializable
data class FitnessActivityInfo(
    val activity: FeatureField<FitnessActivityType>,
    val count: FeatureField<Int>
) : Loggable {
    override val logHeader: String = "${activity.logHeader}, ${count.logHeader}"

    override val logValue: String = "${activity.logValue}, ${count.logValue}"

    override val logDoubleValues: List<Double> =
        listOf(getFitnessActivityCode(activity.value).toDouble(), count.value.toDouble())

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

fun getFitnessActivityType(activity: Short) = when ((activity and 0x0F).toInt()) {
    0x00 -> FitnessActivityType.NoActivity
    0x01 -> FitnessActivityType.BicepCurl
    0x02 -> FitnessActivityType.Squat
    0x03 -> FitnessActivityType.PushUp
    else -> FitnessActivityType.Error
}

fun getFitnessActivityCode(activity: FitnessActivityType) = when (activity) {
    FitnessActivityType.BicepCurl -> 0x01.toByte()
    FitnessActivityType.Squat -> 0x02.toByte()
    FitnessActivityType.PushUp -> 0x03.toByte()
    FitnessActivityType.NoActivity -> 0x00.toByte()
    FitnessActivityType.Error -> 0x0F.toByte()
}