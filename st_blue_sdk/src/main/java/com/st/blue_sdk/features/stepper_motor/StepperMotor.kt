/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.stepper_motor

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class StepperMotor(
    isEnabled: Boolean,
    type: Type = Type.STANDARD,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = false
) : Feature<StepperMotorInfo>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp,
    isDataNotifyFeature = false
) {

    companion object {
        const val NAME = "Stepper Motor"
        const val SET_STEPPER_MOTOR: Byte = 0x00
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<StepperMotorInfo> {

        val statusIndex = NumberConversion.byteToUInt8(data, dataOffset).toInt()
        val motorStatus = StepperMotorInfo.StepperMotorState.entries[statusIndex]
        return FeatureUpdate(
            featureName = name,
            readByte = 1,
            timeStamp = timeStamp,
            rawData = data,
            data = StepperMotorInfo(
                motorStatus = FeatureField(name = "Motor Status", value = motorStatus)
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is StepperMotorCommand -> {
                var out = byteArrayOf(command.command.ordinal.toByte())
                if (command.command == StepperMotorCommand.MotorCommand.MOTOR_MOVE_STEPS_FORWARD ||
                    command.command == StepperMotorCommand.MotorCommand.MOTOR_MOVE_STEPS_BACKWARD
                ) {
                    out =
                        out.plus(NumberConversion.BigEndian.uint32ToBytes(command.numberOfSteps.toLong()))
                }
                out
            }
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}