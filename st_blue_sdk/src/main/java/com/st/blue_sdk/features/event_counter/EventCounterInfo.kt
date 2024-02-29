/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.event_counter

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class EventCounterInfo(
    val count: FeatureField<Int>
) : Loggable {
    override val logHeader: String = count.logHeader

    override val logValue: String = count.logValue
    override val logDoubleValues: List<Double> = listOf(count.value.toDouble())

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${count.name} = ${count.value}\n")
        return sampleValue.toString()
    }
}
