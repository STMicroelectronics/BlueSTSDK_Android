/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models

data class Sample(
    val timestamp: Long = 0,
    val data: Array<Number>,
    val dataDesc: Array<Field>,
    val notificationTime: Long = System.currentTimeMillis()
) {
    override fun toString(): String =
        "Timestamp: $timestamp Data: ${data.contentToString()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sample

        if (timestamp != other.timestamp) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + dataDesc.contentHashCode()
        result = 31 * result + notificationTime.hashCode()
        return result
    }
}
