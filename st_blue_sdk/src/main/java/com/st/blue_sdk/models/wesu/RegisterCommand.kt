/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models.wesu

import com.st.blue_sdk.models.Field
import com.st.blue_sdk.utils.NumberConversion
import kotlin.experimental.and

data class RegisterCommand(
    val register: Register,
    val target: Register.Target,
    val data: ByteArray = byteArrayOf()
) {

    companion object {

        fun create(
            register: Register,
            target: Register.Target,
            fieldType: Field.Type,
            value: Long
        ): RegisterCommand {

            val data = when (fieldType) {
                Field.Type.Float -> NumberConversion.LittleEndian.uint32ToBytes(value)
                Field.Type.Int8 -> byteArrayOf(value.toByte())
                Field.Type.UInt8 -> byteArrayOf(value.toByte() and 0xFF.toByte())
                Field.Type.Int16 -> NumberConversion.LittleEndian.int16ToBytes(value.toShort())
                Field.Type.UInt16 -> NumberConversion.LittleEndian.uint16ToBytes(value.toInt())
                Field.Type.Int32 -> NumberConversion.LittleEndian.int32ToBytes(value.toInt())
                Field.Type.UInt32, Field.Type.Int64 -> NumberConversion.LittleEndian.uint32ToBytes(
                    value
                )
                else -> throw IllegalAccessException()
            }

            return RegisterCommand(register, target, data)
        }

        fun create(
            register: Register,
            target: Register.Target,
            value: Float
        ): RegisterCommand {
            return RegisterCommand(
                register,
                target,
                NumberConversion.LittleEndian.floatToBytes(value)
            )
        }

        fun create(
            register: Register,
            target: Register.Target,
            value: String
        ): RegisterCommand {
            return RegisterCommand(register, target, value.toByteArray())
        }

        fun create(
            register: Register,
            target: Register.Target,
            data: ByteArray
        ): RegisterCommand {
            return RegisterCommand(register, target, Register.getPayload(data) ?: byteArrayOf())
        }
    }

    /**
     * Get the short value received from this command
     *
     * @return the short value received from this command
     */
    fun getDataShort(): Short {
        return if (data.size == 2) {
            NumberConversion.LittleEndian.bytesToInt16(data)
        } else -1
        //Error condition
    }

    /**
     * Get the buffer managed from this command
     *
     * @return the data buffer to send or received from this command
     */
    fun getDataChar(): CharArray {
        val out = CharArray(data.size)
        for (i in data.indices) out[i] = data[i].toInt().toChar()
        return out
    }

    /**
     * Get the packet to write the data payload to the target register of this command
     *
     * @return the data packet to sent through the config control characteristic
     */
    fun toWritePacket(): ByteArray? {
        return register.toWritePacket(target, data)
    }

    /**
     * Get the packet to read target register of this command
     *
     * @return the data packet to sent through the config control characteristic
     */
    fun toReadPacket(): ByteArray? {
        return register.toReadPacket(target)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegisterCommand

        if (register != other.register) return false
        if (target != other.target) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = register.hashCode()
        result = 31 * result + target.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}