/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models.wesu

import kotlin.experimental.and

data class Register(val address: Int, val size: Int, val access: Access, val target: Target) {

    /**
     * Type used for register store target
     */
    enum class Target {
        PERSISTENT, SESSION, BOTH
    }

    /**
     * Type used for register access type
     */
    enum class Access {
        R, W, RW
    }

    companion object {

        /**
         * Get the payload of the read register
         * @param buffer packet received from the device
         *
         * @return the value(s) of the read registers
         */
        fun getPayload(buffer: ByteArray): ByteArray? {
            var toPayload: ByteArray? = null
            if (buffer.size > 4) {
                toPayload = ByteArray(buffer.size - 4) //skip header
                System.arraycopy(buffer, 4, toPayload, 0, toPayload.size)
            }
            return toPayload
        }

        /**
         * Get the Target of the received register
         * @param buffer packet received from the device
         *
         * @return the Mode of the read registers
         */
        fun getTarget(buffer: ByteArray): Target {
            return if (buffer[0] and 0x40.toByte() == 0x40.toByte()) Target.PERSISTENT else Target.SESSION
        }

        /**
         * Check if the buffer is for a write operation
         * @param buffer packet received from the device
         *
         * @return true if write bit is set
         */
        fun isWriteOperation(buffer: ByteArray): Boolean {
            return buffer[0] and 0x20.toByte() == 0x20.toByte()
        }

        /**
         * Check if the buffer is for a write operation
         * @param buffer packet received from the device
         *
         * @return true if Read bit is set
         */
        fun isReadOperation(buffer: ByteArray): Boolean {
            return !isWriteOperation(buffer)
        }

        /**
         * Get the address of the received register
         * @param buffer packet received from the device
         *
         * @return the address of the register read or write operation
         */
        fun getAddress(buffer: ByteArray): Int {
            return (buffer[1] and 0xFF.toByte()).toInt()
        }

        /**
         * Get the error of the received register
         * @param buffer packet received from the device
         *
         * @return the error code of the register read or write operation
         */
        fun getError(buffer: ByteArray): Int {
            return (buffer[2] and 0xFF.toByte()).toInt()
        }

        /**
         * Get the size of the received register
         * @param buffer packet received from the device
         *
         * @return the size of the register read or write operation
         */
        fun getSize(buffer: ByteArray): Int {
            return (buffer[3] and 0xFF.toByte()).toInt()
        }
    }

    /**
     * fill buffer header for the mode with proper write/read options and ack
     * @param header buffer to fill
     * @param target Target memory Persistent/Session
     * @param write write or read
     * @param ack ack required
     */
    private fun setHeader(
        header: ByteArray,
        target: Target,
        write: Boolean,
        ack: Boolean
    ) {
        //CTRL byte
        header[0] = (0x80 or  //Exec op
                (if (target == Target.PERSISTENT) 0x40 else 0x00) or  //target register session vs persistent
                (if (write) 0x20 else 0x00) or  //Write or Read operation
                if (ack) 0x08 else 0x00).toByte() //Ack required
        //ADDR
        header[1] = address.toByte()
        //ERR
        header[2] = 0.toByte()
        //LEN
        header[3] = size.toByte()
    }

    /**
     * Get the buffer for read the register
     * @param target Mode Persistent/Session
     *
     * @return the packet (buffer) to send to the device to read the register
     */
    fun toReadPacket(target: Target): ByteArray? {
        var toBuffer: ByteArray? = null
        if (this.target == Target.BOTH || target == this.target) {
            if (access == Access.R || access == Access.RW) {
                toBuffer = ByteArray(4)
                setHeader(toBuffer, target, false, true)
            }
        }
        return toBuffer
    }

    /**
     * Get the buffer for write the register
     * @param target Mode Persistent/Session
     * @param payload data to write in the device register
     *
     * @return the packet (buffer) to send to the device to write the register with the value
     * defined in the payload
     */
    fun toWritePacket(
        target: Target,
        payload: ByteArray?
    ): ByteArray? {
        var toBuffer: ByteArray? = null
        if (payload != null) {
            if (this.target == Target.BOTH || target == this.target) {
                if ((access == Access.RW || access == Access.W) &&
                    payload.size <= size * 2
                ) {
                    toBuffer = ByteArray(4 + payload.size)
                    setHeader(toBuffer, target, true, true)
                    System.arraycopy(payload, 0, toBuffer, 4, payload.size)
                }
            }
        }
        return toBuffer
    }
}