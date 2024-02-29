/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.motion_intensity

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class MotionIntensityInfo(
    val intensity: FeatureField<Short>,
) : Loggable {
    override val logHeader: String = intensity.logHeader

    override val logValue: String = intensity.logValue

    override val logDoubleValues: List<Double> = listOf(intensity.value.toDouble())

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${intensity.name} = ${intensity.value}\n")
        return sampleValue.toString()
    }
}
