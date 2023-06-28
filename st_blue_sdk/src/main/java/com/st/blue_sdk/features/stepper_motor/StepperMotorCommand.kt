/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.stepper_motor

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.stepper_motor.StepperMotor.Companion.SET_STEPPER_MOTOR

class StepperMotorCommand(
    feature: Feature<*>,
    commandId: Byte = SET_STEPPER_MOTOR,
    val command: MotorCommand,
    val numberOfSteps: UInt
) : FeatureCommand(feature, commandId) {

    enum class MotorCommand {
        MOTOR_STOP_RUNNING_WITHOUT_TORQUE, // Stops running with HiZ.
        MOTOR_STOP_RUNNING_WITH_TORQUE,    // Stops running with torque applied.
        MOTOR_RUN_FORWARD,                 // Runs forward indefinitely.
        MOTOR_RUN_BACKWARD,                // Runs backward indefinitely.
        MOTOR_MOVE_STEPS_FORWARD,          // Moves steps forward.
        MOTOR_MOVE_STEPS_BACKWARD          // Moves steps backward.
    }
}