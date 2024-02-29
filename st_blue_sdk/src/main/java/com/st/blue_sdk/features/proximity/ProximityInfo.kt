/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.proximity

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class ProximityInfo(
    val proximity: FeatureField<Int>
) : Loggable {
    override val logHeader: String = proximity.logHeader

    override val logValue: String = proximity.logValue

    override val logDoubleValues: List<Double> = listOf(proximity.value.toDouble())

    companion object {
        /**
         * the sensor return this value when the object is out of range
         */
        const val OUT_OF_RANGE_VALUE = 0xFFFF

        /**
         * maximum object distance managed by the sensor
         */
        const val LOW_RANGE_DATA_MAX = 0x00FE
        const val HIGH_RANGE_DATA_MAX = 0X7FFE
    }

    override fun toString(): String {
        val sampleValue = StringBuilder()
        if (proximity.value != OUT_OF_RANGE_VALUE) {
            sampleValue.append("\t${proximity.name} = ${proximity.value} ${proximity.unit}\n")
        } else {
            sampleValue.append("\t${proximity.name} = Out of Range\n")
        }
        return sampleValue.toString()
    }
}
