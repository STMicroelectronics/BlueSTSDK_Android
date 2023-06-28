/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.temperature

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class TemperatureInfo(
    val temperature: FeatureField<Float>,
    val featureId: FeatureField<Int>
) : Loggable {
    override val logValue: String =
        "${temperature.logValue}, ${featureId.logValue}"

    override val logHeader: String =
        "${temperature.logHeader}, ${featureId.logHeader}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${temperature.name} = ${temperature.value} ${temperature.unit}\n")
        sampleValue.append("\t${featureId.name} = ${featureId.value} ${featureId.unit ?: ""}\n")
        return sampleValue.toString()
    }
}
