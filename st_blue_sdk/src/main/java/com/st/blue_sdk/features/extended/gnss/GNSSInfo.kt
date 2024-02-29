/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.gnss

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class GNSSInfo(
    val latitude: FeatureField<Float>,
    val longitude: FeatureField<Float>,
    val altitude: FeatureField<Float>,
    val numSatellites: FeatureField<Int>,
    val signalQuality: FeatureField<Int>
) : Loggable {
    override val logHeader: String =
        "${latitude.logHeader}, ${longitude.logHeader}, ${altitude.logHeader}, ${numSatellites.logHeader}, ${signalQuality.logHeader}"

    override val logValue: String =
        "${latitude.logValue}, ${longitude.logValue}, ${altitude.logValue}, ${numSatellites.logValue}, ${signalQuality.logValue}"

    override val logDoubleValues: List<Double> = listOf(
        latitude.value.toDouble(),
        longitude.value.toDouble(),
        altitude.value.toDouble(),
        numSatellites.value.toDouble(),
        signalQuality.value.toDouble()
    )

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${latitude.name} = ${latitude.value} ${latitude.unit}\n")
        sampleValue.append("\t${longitude.name} = ${longitude.value} ${longitude.unit}\n")
        sampleValue.append("\t${altitude.name} = ${altitude.value} ${altitude.unit}\n")
        sampleValue.append("\t${numSatellites.name} = ${numSatellites.value}\n")
        sampleValue.append("\t${signalQuality.name} = ${signalQuality.value} ${signalQuality.unit}\n")
        return sampleValue.toString()
    }
}
