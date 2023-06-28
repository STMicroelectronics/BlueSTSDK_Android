/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.battery

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class BatteryInfo(
    val status: FeatureField<BatteryStatus>,
    val percentage: FeatureField<Float>,
    val voltage: FeatureField<Float>,
    val current: FeatureField<Float>
) : Loggable {
    override val logHeader: String =
        "${status.logHeader}, ${percentage.logHeader}, ${voltage.logHeader}, ${current.logHeader}"

    override val logValue: String =
        "${status.logValue}, ${percentage.logValue}, ${voltage.logValue}, ${current.logValue}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${percentage.name} = ${percentage.value} ${percentage.unit}\n")
        sampleValue.append("\t${status.name} = ${status.value}\n")
        sampleValue.append("\t${voltage.name} = ${voltage.value} ${voltage.unit}\n")
        if (current.value.isNaN()) {
            sampleValue.append("\t${current.name} = Not Available\n")
        } else {
            sampleValue.append("\t${current.name} = ${current.value} ${current.unit}\n")
        }
        return sampleValue.toString()
    }
}

enum class BatteryStatus {
    LowBattery,
    Discharging,
    PluggedNotCharging,
    Charging,
    Unknown,
    Error
}
