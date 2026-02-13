package com.st.blue_sdk.features.extended.robotics_movement.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.robotics_movement.RoboticsMovement

class MoveCommandDifferentialDriveArticulatingMove (
    feature : RoboticsMovement,
    val action : List<RoboticsActionBits>,
    val speed : Byte,
    val rotationAngle : Byte,
    val linearAngle : Byte,
    val res : ByteArray
) : FeatureCommand(feature = feature , commandId = RoboticsMovement.MOVE_COMMAND_DIFFERENTIAL_DRIVE_ARTICULATING_MOVE)