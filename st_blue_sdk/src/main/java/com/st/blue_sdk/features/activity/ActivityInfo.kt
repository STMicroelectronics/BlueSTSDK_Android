@file:UseSerializers(DateSerializer::class)
/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.activity

import com.st.blue_sdk.board_catalog.api.serializers.DateSerializer
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class ActivityInfo(
    val activity: FeatureField<ActivityType>,
    val algorithm: FeatureField<Short>,
    val date: FeatureField<Date>
) : Loggable {
    override val logHeader: String =
        "${activity.logHeader}, ${algorithm.logHeader}, ${date.logHeader}"

    override val logValue: String = "${activity.logValue}, ${algorithm.logValue}, ${date.logValue}"

    companion object {
        const val ALGORITHM_NOT_DEFINED: Short = 0xFF
    }

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${activity.name} = ${activity.value}\n")
        if (algorithm.value != ALGORITHM_NOT_DEFINED) {
            sampleValue.append("\t${algorithm.name} = ${algorithm.value}\n")
        }
        sampleValue.append("\t${date.name} = ${date.value}\n")
        return sampleValue.toString()
    }
}

enum class ActivityType {
    NoActivity,
    Stationary,
    Walking,
    FastWalking,
    Jogging,
    Biking,
    Driving,
    Stairs,
    AdultInCar,
    Error
}