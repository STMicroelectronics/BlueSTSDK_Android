/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.predictive

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
class PredictiveSpeedStatusInfo(
    val statusX: FeatureField<Status>,
    val statusY: FeatureField<Status>,
    val statusZ: FeatureField<Status>,
    val speedX: FeatureField<Float>,
    val speedY: FeatureField<Float>,
    val speedZ: FeatureField<Float>
) : Loggable {
    override val logHeader: String =
        "${statusX.logHeader}, ${statusY.logHeader}, ${statusZ.logHeader}, ${speedX.logHeader}, ${speedY.logHeader}, ${speedZ.logHeader}"

    override val logValue: String =
        "${statusX.logValue}, ${statusY.logValue}, ${statusZ.logValue}, ${speedX.logValue}, ${speedY.logValue}, ${speedZ.logValue}"
    override val logDoubleValues: List<Double> = listOf(
        Status.statusToByte(statusX.value).toDouble(),
        Status.statusToByte(statusY.value).toDouble(),
        Status.statusToByte(statusZ.value).toDouble(),
        speedX.value.toDouble(),
        speedY.value.toDouble(),
        speedZ.value.toDouble()
    )

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${statusX.name} = ${statusX.value}\n")
        sampleValue.append("\t${statusY.name} = ${statusY.value}\n")
        sampleValue.append("\t${statusZ.name} = ${statusZ.value}\n")
        sampleValue.append("\t${speedX.name} = ${speedX.value} ${speedX.unit}\n")
        sampleValue.append("\t${speedY.name} = ${speedY.value} ${speedY.unit}\n")
        sampleValue.append("\t${speedZ.name} = ${speedZ.value} ${speedZ.unit}\n")
        return sampleValue.toString()
    }
}