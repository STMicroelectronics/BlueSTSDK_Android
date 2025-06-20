/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.robotics_movement.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.robotics_movement.RoboticsMovement

class MoveCommandDifferentialDrivePWMSpeed(
        feature : RoboticsMovement,
        val action :  List<RoboticsActionBits>,
        val leftMode : UByte = 0x00u,
        val leftWheel : Short,
        val rightMode : UByte = 0x00u,
        val rightWheel : Short,
        val res : Long
    ) : FeatureCommand(feature = feature , commandId = RoboticsMovement.MOVE_COMMAND_DIFFERENTIAL_DRIVE_PWM_SPEED)