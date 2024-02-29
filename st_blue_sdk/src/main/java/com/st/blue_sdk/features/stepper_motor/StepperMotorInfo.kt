/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.stepper_motor

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
class StepperMotorInfo(
    val motorStatus: FeatureField<StepperMotorState>
) : Loggable {

    enum class StepperMotorState {
        INACTIVE,
        RUNNING
    }

    override val logHeader: String = motorStatus.logHeader

    override val logValue: String = motorStatus.logValue

    override val logDoubleValues: List<Double> = listOf()

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${motorStatus.name} = ${motorStatus.value} ${motorStatus.unit}\n")
        return sampleValue.toString()
    }
}