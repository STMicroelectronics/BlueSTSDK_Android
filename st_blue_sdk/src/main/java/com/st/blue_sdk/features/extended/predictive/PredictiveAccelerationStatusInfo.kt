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
class PredictiveAccelerationStatusInfo(
    val statusX: FeatureField<Status>,
    val statusY: FeatureField<Status>,
    val statusZ: FeatureField<Status>,
    val accX: FeatureField<Float?>,
    val accY: FeatureField<Float?>,
    val accZ: FeatureField<Float?>
) : Loggable {
    override val logHeader: String =
        "${statusX.logHeader}, ${statusY.logHeader}, ${statusZ.logHeader}, ${accX.logHeader}, ${accY.logHeader}, ${accZ.logHeader}"

    override val logValue: String =
        "${statusX.logValue}, ${statusY.logValue}, ${statusZ.logValue}, ${accX.logValue}, ${accY.logValue}, ${accZ.logValue}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${statusX.name} = ${statusX.value} ${statusX.unit}\n")
        sampleValue.append("\t${statusY.name} = ${statusY.value} ${statusY.unit}\n")
        sampleValue.append("\t${statusZ.name} = ${statusZ.value} ${statusZ.unit}\n")
        sampleValue.append("\t${accX.name} = ${accX.value} ${accX.unit}\n")
        sampleValue.append("\t${accY.name} = ${accY.value} ${accY.unit}\n")
        sampleValue.append("\t${accZ.name} = ${accZ.value} ${accZ.unit}\n")
        return sampleValue.toString()
    }
}