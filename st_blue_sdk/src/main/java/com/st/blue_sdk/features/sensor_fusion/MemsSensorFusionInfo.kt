/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.sensor_fusion

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class MemsSensorFusionInfo(
    val quaternions: List<FeatureField<Quaternion>>
) : Loggable {
    override val logHeader: String =
        quaternions.joinToString(separator = ", ") { it.logHeader }

    override val logValue: String =
        quaternions.joinToString(separator = ", ") { it.logValue }

    override val logDoubleValues: List<Double> = listOf()

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("Quaternions = [\n")
        quaternions.forEach { sampleValue.append("{\n${it.value}},\n") }
        sampleValue.append("]\n")
        return sampleValue.toString()
    }
}
