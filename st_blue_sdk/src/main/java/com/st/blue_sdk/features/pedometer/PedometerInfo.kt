/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.pedometer

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class PedometerInfo(
    val steps: FeatureField<Long>,
    val frequency: FeatureField<Short>
) : Loggable {
    override val logHeader: String = "${steps.logHeader}, ${frequency.logHeader}"

    override val logValue: String = "${steps.logValue}, ${frequency.logValue}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${steps.name} = ${steps.value}\n")
        sampleValue.append("\t${frequency.name} = ${frequency.value} ${frequency.unit}\n")
        return sampleValue.toString()
    }
}
