/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.robotics_movement.request

sealed class RoboticsActionBits {
    data object WRITE : RoboticsActionBits()
    data object READ : RoboticsActionBits()
    data object ACK_FLAG : RoboticsActionBits()
    data class PERIODIC_READ(val interval: Interval) : RoboticsActionBits()
    data class ERROR(val errorCode: ErrorCode) : RoboticsActionBits()

    enum class ErrorCode(val code: UByte) {
        GENERAL_ERROR(0x0u),
        COMMAND_NOT_SUPPORTED(0x1u),
        COMMAND_NOT_EXPECTED(0x2u),
        COMMAND_NOT_ALLOWED(0x3u),
        PARAMS_NOT_CORRECT(0x4u),
        PARAMS_OUT_OF_RANGE(0x5u),
        INTERNAL_ERROR(0x6u),
        SUB_SYSTEM_ERROR(0x7u),
        SUB_SYSTEM_COMMUNICATION_FAILED(0x8u),
        AUTHENTICATION_REQUIRED(0x9u),
        RFU_A(0xAu),
        RFU_B(0xBu),
        RFU_C(0xCu),
        UNKNOWN_ERROR(0xFu)
    }

    // Enum representing intervals (low 4 bits)
    enum class Interval(val code: UByte) {
        CANCEL_PERIODIC_READ(0x0u),
        MS100(0x1u),
        MS200(0x2u),
        MS500(0x3u),
        S1(0x4u),
        S2(0x5u),
        S5(0x6u),
        S10(0x7u),
        S20(0x8u),
        M1(0x9u),
        M2(0xAu),
        M5(0xBu),
        M10(0xCu),
        M20(0xDu),
        HR1(0xEu),
        DO_NOT_CHANGE(0xFu)
    }
    companion object{
        fun packActions(actions: List<RoboticsActionBits>): Byte {
            var actionByte: UByte = 0u
            var extraPackData: UByte = 0u

            for (action in actions) {
                when (action) {
                    WRITE -> actionByte = actionByte or 0b00010000u
                    READ -> actionByte = actionByte or 0b00100000u
                    ACK_FLAG -> actionByte = actionByte or 0b10000000u
                    is PERIODIC_READ -> {
                        actionByte = actionByte or 0b01000000u
                        extraPackData = action.interval.code
                    }
                    is ERROR -> {
                        val packedByte: UByte = 0b11110000u
                        extraPackData = action.errorCode.code
                        break
                    }
                }
            }

            val packedByte: UByte = (actionByte and 0xF0u) or (extraPackData and 0x0Fu)
            return packedByte.toByte()
        }

        fun evaluateActionCode(byte: UByte): List<RoboticsActionBits>? {
            val highBits = byte and 0xF0u
            val lowBits = byte and 0x0Fu

            val actionReceived = mutableListOf<RoboticsActionBits>()

            if (highBits == 0b11110000u.toUByte()) {
                val errorCode = ErrorCode.values().find { it.code == lowBits }
                if (errorCode != null) {
                    actionReceived.add(ERROR(errorCode))
                } else {
                    actionReceived.add(ERROR(ErrorCode.UNKNOWN_ERROR))
                }
            } else {
                if (highBits and 0b10000000u.toUByte() != 0u.toUByte()) {
                    actionReceived.add(ACK_FLAG)
                }
                if (highBits and 0b01000000u.toUByte() != 0u.toUByte()) {
                    val interval = Interval.values().find { it.code == lowBits }
                    if (interval != null) {
                        actionReceived.add(PERIODIC_READ(interval))
                    }
                }
                if (highBits and 0b00100000u.toUByte() != 0u.toUByte()) {
                    actionReceived.add(READ)
                }
                if (highBits and 0b00010000u.toUByte() != 0u.toUByte()) {
                    actionReceived.add(WRITE)
                }
            }

            return if (actionReceived.isEmpty()) null else actionReceived
        }
    }
}

