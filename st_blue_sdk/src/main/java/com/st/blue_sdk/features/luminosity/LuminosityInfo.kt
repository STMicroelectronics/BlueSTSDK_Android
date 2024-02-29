/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.luminosity

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class LuminosityInfo(
    val luminosity: FeatureField<Int>
) : Loggable {
    override val logHeader: String = luminosity.logHeader
    override val logValue: String = luminosity.logValue

    override val logDoubleValues: List<Double> = listOf(luminosity.value.toDouble())

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${luminosity.name} = ${luminosity.value} ${luminosity.unit}\n")
        return sampleValue.toString()
    }
}
