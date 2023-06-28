/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.motor_time_param

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
class MotorTimeParameterInfo(
    val accPeakX: FeatureField<Float?>,
    val accPeakY: FeatureField<Float?>,
    val accPeakZ: FeatureField<Float?>,
    val rmsSpeedX: FeatureField<Float?>,
    val rmsSpeedY: FeatureField<Float?>,
    val rmsSpeedZ: FeatureField<Float?>
) : Loggable {
    override val logHeader: String =
        "${accPeakX.logHeader}, ${accPeakY.logHeader}, ${accPeakZ.logHeader}, ${rmsSpeedX.logHeader}, ${rmsSpeedY.logHeader}, ${rmsSpeedZ.logHeader}"

    override val logValue: String =
        "${accPeakX.logValue}, ${accPeakY.logValue}, ${accPeakZ.logValue}, ${rmsSpeedX.logValue}, ${rmsSpeedY.logValue}, ${rmsSpeedZ.logValue}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${accPeakX.name} = ${accPeakX.value} ${accPeakX.unit}\n")
        sampleValue.append("\t${accPeakY.name} = ${accPeakY.value} ${accPeakY.unit}\n")
        sampleValue.append("\t${accPeakZ.name} = ${accPeakZ.value} ${accPeakZ.unit}\n")
        sampleValue.append("\t${rmsSpeedX.name} = ${rmsSpeedX.value} ${rmsSpeedX.unit}\n")
        sampleValue.append("\t${rmsSpeedY.name} = ${rmsSpeedY.value} ${rmsSpeedY.unit}\n")
        sampleValue.append("\t${rmsSpeedZ.name} = ${rmsSpeedZ.value} ${rmsSpeedZ.unit}\n")
        return sampleValue.toString()
    }
}
