/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.predictive

enum class Status {
    GOOD,
    WARNING,
    BAD,
    UNKNOWN;

    companion object {
        private fun statusFromByte(value: Byte) = when (value.toInt()) {
            0x00 -> GOOD
            0x01 -> WARNING
            0x02 -> BAD
            else -> UNKNOWN
        }

        fun extractXStatus(value: Short) =
            statusFromByte((value.toInt() shr 4 and 0x03).toByte())

        fun extractYStatus(value: Short) =
            statusFromByte((value.toInt() shr 2 and 0x03).toByte())

        fun extractZStatus(value: Short) =
            statusFromByte((value.toInt() and 0x03).toByte())
    }
}