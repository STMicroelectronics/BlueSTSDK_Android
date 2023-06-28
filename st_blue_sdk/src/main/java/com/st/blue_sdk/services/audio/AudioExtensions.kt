/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio

import java.nio.ByteBuffer

fun ShortArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(2 * this.size)
    this.forEach { shortValue ->
        val high: Byte = (shortValue.toInt() shr 8).toByte()
        val low: Byte = (shortValue.toInt() and 0xFF).toByte()
        buffer.put(low)
        buffer.put(high)
    }
    return buffer.array()
}