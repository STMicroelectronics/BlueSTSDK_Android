/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.mic_level

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class MicLevelInfo(
    val micsLevel: List<FeatureField<Short>>
) : Loggable {
    override val logHeader: String = micsLevel.joinToString(separator = ", ") { it.logHeader }

    override val logValue: String = micsLevel.joinToString(separator = ", ") { it.logValue }

    override fun toString(): String {
        val sampleValue = StringBuilder()
        for (element in micsLevel) {
            sampleValue.append("\t${element.name} = ${element.value} ${element.unit}\n")
        }
        return sampleValue.toString()
    }
}
