/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.robotics_movement

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.extended.robotics_movement.request.CoordinateOrigin
import com.st.blue_sdk.features.extended.robotics_movement.request.CurrentCoordinate
import com.st.blue_sdk.features.extended.robotics_movement.request.GetRobotTopology
import com.st.blue_sdk.features.extended.robotics_movement.request.MoveCommandDifferentialDrivePWMSpeed
import com.st.blue_sdk.features.extended.robotics_movement.request.MoveCommandDifferentialDriveSimpleMove
import com.st.blue_sdk.features.extended.robotics_movement.request.RobotDirection
import com.st.blue_sdk.features.extended.robotics_movement.request.RoboticsActionBits
import com.st.blue_sdk.features.extended.robotics_movement.request.SetNavigationMode
import com.st.blue_sdk.features.extended.robotics_movement.request.TopologyBit
import com.st.blue_sdk.features.extended.robotics_movement.response.RoboticsMovementResponse
import com.st.blue_sdk.utils.NumberConversion

class RoboticsMovement(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<RoboticsMovementInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Robot Movement"
        const val NUMBER_BYTES = 2
        const val GET_ROBOT_TOPOLOGY : Byte = 0x10
        const val SET_NAVIGATION_MODE : Byte = 0x21
        const val COORDINATE_ORIGIN : Byte = 0x22
        const val CURRENT_COORDINATE : Byte = 0x23
        const val MOVE_COMMAND_DIFFERENTIAL_DRIVE_PWM_SPEED : Byte = 0x24
        const val MOVE_COMMAND_DIFFERENTIAL_DRIVE_SIMPLE_MOVE : Byte = 0x25

        fun getCommandType(commandCode: Short) = when (commandCode) {
            0X10.toShort() -> GET_ROBOT_TOPOLOGY
            0X21.toShort() -> SET_NAVIGATION_MODE
            0X22.toShort() -> COORDINATE_ORIGIN
            0X23.toShort() -> CURRENT_COORDINATE
            0X24.toShort() -> MOVE_COMMAND_DIFFERENTIAL_DRIVE_PWM_SPEED
            0x25.toShort() -> MOVE_COMMAND_DIFFERENTIAL_DRIVE_SIMPLE_MOVE

            else -> throw IllegalArgumentException("Unknown command type: $commandCode")
        }

        fun getDirectionCharacter(direction: RobotDirection) = when (direction) {
            RobotDirection.FORWARD -> 70 // 'F'
            RobotDirection.BACKWARD -> 66 // 'B'
            RobotDirection.LEFT -> 76 // 'L'
            RobotDirection.RIGHT -> 82 // 'R'
            RobotDirection.STOP -> 83 // 'S'

            else -> throw IllegalArgumentException("Unknown command type: $direction")
        }

        fun byteArrayToUInt32(data: ByteArray, dataOffset: Int): UInt {
            require(data.size >= dataOffset + 4) { "Not enough bytes to convert to UInt32" }

            return ((data[dataOffset].toUInt() and 0xFFu) shl 24) or
                    ((data[dataOffset + 1].toUInt() and 0xFFu) shl 16) or
                    ((data[dataOffset + 2].toUInt() and 0xFFu) shl 8) or
                    (data[dataOffset + 3].toUInt() and 0xFFu)
        }

        fun isBitSet(value: UInt, bitPosition: Int): Boolean {
            val mask = 1u shl bitPosition
            return (value and mask) != 0u
        }

        fun getTopologyName(topology: UInt) : List<FeatureField<TopologyBit>> {

            val functionalities = mutableListOf<FeatureField<TopologyBit>>()

            for(bitPosition in 0..31){
                if(isBitSet(topology, bitPosition)){
                    val functionality = when (bitPosition) {
                        0 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.REMOTE_CONTROL_DIFFERENTIAL
                        )
                        1 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.REMOTE_CONTROL_STEERING
                        )
                        2 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.REMOTE_CONTROL_MECHANUM
                        )
                        3 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.REMOTE_CONTROL_RESERVED
                        )
                        4 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.ODOMETRY
                        )
                        5 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.IMU_AVAILABLE
                        )
                        6 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.ABSOLUTE_SPEED_CONTROL
                        )
                        7 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.ARM_SUPPORT
                        )
                        8 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.ATTACHMENT_SUPPORT
                        )
                        9 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.AUTO_DOCKING
                        )
                        10 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.WIRELESS_CHARGING
                        )
                        11 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.OBSTACLE_DETECTION_FORWARD
                        )
                        12 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.OBSTACLE_DETECTION_MULTIDIRECTIONAL
                        )
                        13 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.ALARM
                        )
                        14 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.HEADLIGHTS
                        )
                        15 -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.WARNING_LIGHTS
                        )
                        else -> FeatureField(
                            name = "Topology",
                            value = TopologyBit.RFU
                        )
                    }

                    functionality.let { functionalities.add(it) }
                }
            }

            return functionalities
        }

        fun getNavigationMode(navigation: UByte) : List<FeatureField<TopologyBit>> {
            val navigationModes = mutableListOf<FeatureField<TopologyBit>>()

            val navigationMode =  when(navigation){
                0x01.toUByte() -> FeatureField(
                    name = "Navigation Mode",
                    value = TopologyBit.REMOTE_CONTROL
                )

                0x02.toUByte() -> FeatureField(
                    name =  "Navigation Mode",
                    value = TopologyBit.FREE_NAVIGATION
                )

                0x03.toUByte() -> FeatureField(
                    name = "Navigation Mode",
                    value = TopologyBit.FOLLOW_ME
                )

                else -> FeatureField(
                    name = "Navigation Mode",
                    value = TopologyBit.RFU
                )
            }

            navigationModes.add(navigationMode)

            return navigationModes
        }
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<RoboticsMovementInfo> {
        require(data.size - dataOffset > 0) { "There are no bytes available to read for $name feature" }

        //it contains the first byte after data offset
        val commandId = NumberConversion.byteToUInt8(data,dataOffset)

        val commandType = getCommandType(commandId)

        when (commandType) {
            GET_ROBOT_TOPOLOGY -> {
                val action = NumberConversion.byteToUInt8(data,dataOffset+1).toUByte()
                val topology = byteArrayToUInt32(data,dataOffset + 2)
                val topologyList = getTopologyName(topology)

                return FeatureUpdate(
                    featureName = name,
                    readByte = data.size,
                    timeStamp = timeStamp,
                    rawData = data,
                    data = RoboticsMovementInfo(
                        commandId = commandId,
                        action = RoboticsActionBits.evaluateActionCode(action) ,
                        data = topologyList
                    )
                )
            }

            SET_NAVIGATION_MODE -> {
                val action = NumberConversion.byteToUInt8(data,dataOffset+1).toUByte()
                val navigationModeId = NumberConversion.byteToUInt8(data,dataOffset+2).toUByte()
                val navigationMode = getNavigationMode(navigationModeId)

                return FeatureUpdate(
                    featureName = name,
                    readByte = data.size,
                    timeStamp = timeStamp,
                    rawData = data,
                    data = RoboticsMovementInfo(
                        commandId = commandId,
                        action = RoboticsActionBits.evaluateActionCode(action) ,
                        data = navigationMode
                    )
                )
            }

            else ->{
                val action = NumberConversion.byteToUInt8(data,dataOffset+1).toUByte()
                return FeatureUpdate(
                    featureName = name,
                    readByte = data.size,
                    timeStamp = timeStamp,
                    rawData = data,
                    data = RoboticsMovementInfo(
                        commandId = 0,
                        action = RoboticsActionBits.evaluateActionCode(action) ,
                        data = null
                    )
                )
            }
        }
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray?{
        return when(command){
            is GetRobotTopology -> packCommandRequest(
                featureBit,
                GET_ROBOT_TOPOLOGY,
                byteArrayOf(
                    RoboticsActionBits.packActions(command.action),
                )
            )

            is SetNavigationMode -> packCommandRequest(
                featureBit,
                SET_NAVIGATION_MODE,
                byteArrayOf(
                    RoboticsActionBits.packActions(command.action),
                    command.navigationMode.toByte(),
                    command.armed.toByte(),
                    command.res.toByte()
                )
            )

            is CoordinateOrigin -> packCommandRequest(
                featureBit,
                COORDINATE_ORIGIN,
                byteArrayOf(
                    RoboticsActionBits.packActions(command.action),
                    command.xCoordinate.toByte(),
                    command.yCoordinate.toByte(),
                    command.theta.toByte(),
                    command.res.toByte()
                )
            )

            is CurrentCoordinate -> packCommandRequest(
                featureBit,
                CURRENT_COORDINATE,
                byteArrayOf(
                    RoboticsActionBits.packActions(command.action),
                    command.xCoordinate.toByte(),
                    command.yCoordinate.toByte(),
                    command.theta.toByte(),
                    command.interval.toByte(),
                    command.res.toByte()
                )
            )

            is MoveCommandDifferentialDrivePWMSpeed -> packCommandRequest(
                featureBit,
                MOVE_COMMAND_DIFFERENTIAL_DRIVE_PWM_SPEED,
                byteArrayOf(
                    RoboticsActionBits.packActions(command.action),
                    command.leftMode.toByte(),
                    command.leftWheel.toByte(),
                    command.rightMode.toByte(),
                    command.rightWheel.toByte(),
                    command.res.toByte()
                )
            )

            is MoveCommandDifferentialDriveSimpleMove -> packCommandRequest(
                featureBit,
                MOVE_COMMAND_DIFFERENTIAL_DRIVE_SIMPLE_MOVE,
                data = byteArrayOf(
                    RoboticsActionBits.packActions(command.action),
                    getDirectionCharacter(command.direction).toByte(),
                    command.speed.toByte(),
                    command.angle.toByte(),
                ) + command.res
            )

            else -> null
        }
    }


    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return unpackCommandResponse(data)?.let {
            if(mask != it.featureMask) return null

            when(it.commandId){
                GET_ROBOT_TOPOLOGY -> {
                    return RoboticsMovementResponse(
                        feature = this,
                        commandId = GET_ROBOT_TOPOLOGY,
                        payload = it.payload
                    )
                }

                SET_NAVIGATION_MODE -> {
                    return RoboticsMovementResponse(
                        feature = this,
                        commandId = SET_NAVIGATION_MODE,
                        payload = it.payload
                    )
                }

                else -> null
            }
        }
    }
}