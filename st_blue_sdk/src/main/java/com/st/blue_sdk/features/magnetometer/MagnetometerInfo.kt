/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.magnetometer

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class MagnetometerInfo(
    val x: FeatureField<Float>,
    val y: FeatureField<Float>,
    val z: FeatureField<Float>,
) : Loggable {
    override val logHeader: String = "${x.logHeader}, ${y.logHeader}, ${z.logHeader}"

    override val logValue: String = "${x.logValue}, ${y.logValue}, ${z.logValue}"
    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${x.name} = ${x.value} ${x.unit}\n")
        sampleValue.append("\t${y.name} = ${y.value} ${y.unit}\n")
        sampleValue.append("\t${z.name} = ${z.value} ${z.unit}\n")
        return sampleValue.toString()
    }
}
