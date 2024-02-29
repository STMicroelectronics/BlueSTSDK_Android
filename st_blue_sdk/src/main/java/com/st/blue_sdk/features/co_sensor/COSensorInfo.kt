/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.co_sensor

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class COSensorInfo(
    val concentration: FeatureField<Float>
) : Loggable {
    override val logHeader: String = concentration.logHeader

    override val logValue: String = concentration.logValue
    override val logDoubleValues: List<Double> = listOf(concentration.value.toDouble())

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${concentration.name} = ${concentration.value} ${concentration.unit}\n")
        return sampleValue.toString()
    }
}
