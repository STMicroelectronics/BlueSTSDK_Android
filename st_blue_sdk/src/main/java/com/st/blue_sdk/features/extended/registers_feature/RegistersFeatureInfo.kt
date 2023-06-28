/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.registers_feature

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class RegistersFeatureInfo(
    val registers: List<FeatureField<Short>>,
    val statusPages: List<FeatureField<Short>>
) : Loggable {
    override val logHeader: String =
        (registers + statusPages).joinToString(separator = ", ") { it.logHeader }

    override val logValue: String =
        (registers + statusPages).joinToString(separator = ", ") { it.logValue }

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\tRegisters:\n")
        registers.forEach { sampleValue.append("\t\t${it.name} = ${it.value}\n") }
        sampleValue.append("\tStatus:\n")
        statusPages.forEach { sampleValue.append("\t\t${it.name} = ${it.value}\n") }
        return sampleValue.toString()
    }
}
