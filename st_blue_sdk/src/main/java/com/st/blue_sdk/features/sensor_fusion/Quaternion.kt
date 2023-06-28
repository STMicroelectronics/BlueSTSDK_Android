/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.sensor_fusion

import kotlinx.serialization.Serializable
import kotlin.math.sqrt

@Serializable
data class Quaternion(
    val timeStamp: Long,
    val qi: Float,
    val qj: Float,
    val qk: Float,
    val qs: Float
) {

    override fun toString(): String {
        return "\tTS= $timeStamp\n\t\tqi = $qi\n\t\tqj = $qj\n\t\tqk = $qk\n\t\tqs = $qs\n"
    }

    companion object {
        fun getQs(qi: Float, qj: Float, qk: Float): Float {
            val t = 1 - (qi * qi + qj * qj + qk * qk)
            return if (t > 0) sqrt(t) else 0f
        }
    }
}
