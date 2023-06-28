/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.pressure

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class PressureInfo(
    val pressure: FeatureField<Float>
) : Loggable {
    override val logHeader: String = pressure.logHeader

    override val logValue: String = pressure.logValue

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${pressure.name} = ${pressure.value} ${pressure.unit}\n")
        return sampleValue.toString()
    }
}
