/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.raw_controlled

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class RawControlledInfo(
    val data: List<FeatureField<Byte>>
) : Loggable {
    override val logHeader: String = data.joinToString(separator = ", ") { it.logHeader }

    override val logValue: String = data.joinToString(separator = ", ") { it.logValue }

    override val logDoubleValues: List<Double> = listOf()

    override fun toString(): String {
        val sampleValue = StringBuilder()
        data.forEach { sampleValue.append("\t${it.name} = ${it.value}\n") }
        return sampleValue.toString()
    }
}
