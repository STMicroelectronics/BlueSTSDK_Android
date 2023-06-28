/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.euler_angle

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class EulerAngleInfo(
    val yaw: FeatureField<Float>,
    val pitch: FeatureField<Float>,
    val roll: FeatureField<Float>
) : Loggable {
    override val logHeader: String = "${yaw.logHeader}, ${pitch.logHeader}, ${roll.logHeader}"

    override val logValue: String = "${yaw.logValue}, ${pitch.logValue}, ${roll.logValue}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${yaw.name} = ${yaw.value} ${yaw.unit}\n")
        sampleValue.append("\t${pitch.name} = ${pitch.value} ${pitch.unit}\n")
        sampleValue.append("\t${roll.name} = ${roll.value} ${roll.unit}\n")
        return sampleValue.toString()
    }
}
