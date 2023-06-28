/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.advertise

import android.util.SparseArray
import kotlin.math.min

object AdvertiseParser {

    fun split(advertise: ByteArray): SparseArray<ByteArray> {
        val splitAdvertise = SparseArray<ByteArray>()
        var ptr = 0
        while (ptr < advertise.size - 2) {

            val length: Int = advertise[ptr++].toInt() and 0xff

            if (length == 0)
                break

            val type = advertise[ptr++]

            //min between the length field and the remaining array length
            val fieldLength = min(length - 1, advertise.size - ptr)
            val data = ByteArray(fieldLength)
            System.arraycopy(advertise, ptr, data, 0, fieldLength)
            splitAdvertise.put(type.toInt(), data)
            ptr += fieldLength
        }
        return splitAdvertise
    }
}