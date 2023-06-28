/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.external.std

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class HeartRateInfo(
    val heartRate: FeatureField<Int>,
    val energyExpended: FeatureField<Int>,
    val rrInterval: FeatureField<Float>,
    val skinContactDetected: FeatureField<Boolean>,
    val skinContactSupported: FeatureField<Boolean>

) : Loggable {
    override val logHeader: String =
        "${heartRate.logHeader}, ${energyExpended.logHeader}, ${rrInterval.logHeader}"

    override val logValue: String =
        "${heartRate.logValue}, ${energyExpended.logValue}, ${rrInterval.logValue}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${heartRate.name} = ${heartRate.value} ${heartRate.unit}\n")
        if (energyExpended.value != -1) {
            sampleValue.append("\t${energyExpended.name} = ${energyExpended.value} ${energyExpended.unit}\n")
        }
        if (!rrInterval.value.isNaN()) {
            sampleValue.append("\t${rrInterval.name} = ${rrInterval.value} ${rrInterval.unit}\n")
        }

        sampleValue.append("\t${skinContactDetected.name} = ${skinContactDetected.value}\n")
        sampleValue.append("\t${skinContactSupported.name} = ${skinContactSupported.value}\n")

        return sampleValue.toString()
    }
}