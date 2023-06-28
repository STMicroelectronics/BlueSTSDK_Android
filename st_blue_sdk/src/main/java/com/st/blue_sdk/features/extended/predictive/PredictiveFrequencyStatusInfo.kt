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
class PredictiveFrequencyStatusInfo(
    val statusX: FeatureField<Status>,
    val statusY: FeatureField<Status>,
    val statusZ: FeatureField<Status>,
    val worstXFreq: FeatureField<Float?>,
    val worstYFreq: FeatureField<Float?>,
    val worstZFreq: FeatureField<Float?>,
    val worstXValue: FeatureField<Float?>,
    val worstYValue: FeatureField<Float?>,
    val worstZValue: FeatureField<Float?>
) : Loggable {
    override val logHeader: String =
        "${statusX.logHeader}, ${statusY.logHeader}, ${statusZ.logHeader}, ${worstXFreq.logHeader}, ${worstYFreq.logHeader}, ${worstZFreq.logHeader}, ${worstXValue.logHeader}, ${worstYValue.logHeader}, ${worstZValue.logHeader}"

    override val logValue: String =
        "${statusX.logValue}, ${statusY.logValue}, ${statusZ.logValue}, ${worstXFreq.logValue}, ${worstYFreq.logValue}, ${worstZFreq.logValue}, ${worstXValue.logValue}, ${worstYValue.logValue}, ${worstZValue.logValue}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${statusX.name} = ${statusX.value} ${statusX.unit}\n")
        sampleValue.append("\t${statusY.name} = ${statusY.value} ${statusY.unit}\n")
        sampleValue.append("\t${statusZ.name} = ${statusZ.value} ${statusZ.unit}\n")
        sampleValue.append("\t${worstXFreq.name} = ${worstXFreq.value} ${worstXFreq.unit}\n")
        sampleValue.append("\t${worstYFreq.name} = ${worstYFreq.value} ${worstYFreq.unit}\n")
        sampleValue.append("\t${worstZFreq.name} = ${worstZFreq.value} ${worstZFreq.unit}\n")
        sampleValue.append("\t${worstXValue.name} = ${worstXValue.value} ${worstXValue.unit}\n")
        sampleValue.append("\t${worstYValue.name} = ${worstYValue.value} ${worstYValue.unit}\n")
        sampleValue.append("\t${worstZValue.name} = ${worstZValue.value} ${worstZValue.unit}\n")
        return sampleValue.toString()
    }
}