/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.ota.debug

import com.st.blue_sdk.utils.NumberConversion
import java.util.zip.Checksum

/**
 * Class that compute the crc32 using the same algorithm used by the stm32 chip,
 * The algorithm work on uint32 so the buffer must have a length multiple of 4.
 */
class STM32Crc32 : Checksum {

    companion object {

        private const val INITIAL_VALUE = -0x1

        private val CRC_TABLE = intArrayOf( // Nibble lookup table for 0x04C11DB7 polynomial
            0x00000000,
            0x04C11DB7,
            0x09823B6E,
            0x0D4326D9,
            0x130476DC,
            0x17C56B6B,
            0x1A864DB2,
            0x1E475005,
            0x2608EDB8,
            0x22C9F00F,
            0x2F8AD6D6,
            0x2B4BCB61,
            0x350C9B64,
            0x31CD86D3,
            0x3C8EA00A,
            0x384FBDBD
        )

        private fun crc32Fast(crc: Int, data: Int): Int {

            var out = crc xor data // Apply all 32-bits

            // Process 32-bits, 4 at a time, or 8 rounds
            out =
                out shl 4 xor CRC_TABLE[out ushr 28] // Assumes 32-bit reg, masking index to 4-bits
            out = out shl 4 xor CRC_TABLE[out ushr 28] //  0x04C11DB7 Polynomial used in STM32
            out = out shl 4 xor CRC_TABLE[out ushr 28]
            out = out shl 4 xor CRC_TABLE[out ushr 28]
            out = out shl 4 xor CRC_TABLE[out ushr 28]
            out = out shl 4 xor CRC_TABLE[out ushr 28]
            out = out shl 4 xor CRC_TABLE[out ushr 28]
            out = out shl 4 xor CRC_TABLE[out ushr 28]
            return out
        }
    }

    private var currentCrc = INITIAL_VALUE

    override fun getValue(): Long {
        return currentCrc.toLong()
    }

    override fun reset() {
        currentCrc = INITIAL_VALUE
    }

    override fun update(bytes: ByteArray?, offset: Int, length: Int) {
        require(length % 4 == 0) { "length must be multiple of 4" }
        //else
        var i = 0
        while (i < length) {
            val value: Int = NumberConversion.LittleEndian.bytesToInt32(bytes, offset + i * 4)
            currentCrc = crc32Fast(currentCrc, value)
            i += 4
        }
    }

    override fun update(i: Int) {
        update(NumberConversion.BigEndian.uint32ToBytes(i.toLong()), 0, 4)
    }
}