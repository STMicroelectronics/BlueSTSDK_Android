/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.color_ambient_light

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class ColorAmbientLightInfo(
    val lux: FeatureField<Int>,
    val cct: FeatureField<Short>,
    val uvIndex: FeatureField<Short>
) : Loggable {
    override val logHeader: String = "${lux.logHeader}, ${cct.logHeader}, ${uvIndex.logHeader}"

    override val logValue: String = "${lux.logValue}, ${cct.logValue}, ${uvIndex.logValue}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${lux.name} = ${lux.value} ${lux.unit}\n")
        sampleValue.append("\t${cct.name} = ${cct.value} ${cct.unit}\n")
        sampleValue.append("\t${uvIndex.name} = ${uvIndex.value}\n")
        return sampleValue.toString()
    }
}
