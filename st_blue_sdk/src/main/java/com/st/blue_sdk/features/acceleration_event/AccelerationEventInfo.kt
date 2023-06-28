/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.acceleration_event

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class AccelerationEventInfo(
    val accEvent: List<FeatureField<AccelerationType>>,
    val numSteps: FeatureField<Short?>
) : Loggable {
    override val logHeader: String =
        (listOf(numSteps) + accEvent).joinToString(separator = ", ") { it.logHeader }

    override val logValue: String =
        (listOf(numSteps) + accEvent).joinToString(separator = ", ") { it.logValue }

    override fun toString(): String {
        val sampleValue = StringBuilder()
        accEvent.forEach { sampleValue.append("\t${it.name} = ${it.value}\n") }
        numSteps.value?.let { sampleValue.append("\t${numSteps.name} = ${numSteps.value}\n") }
        return sampleValue.toString()
    }
}

enum class AccelerationType {
    NoEvent,
    OrientationTopRight,
    OrientationBottomRight,
    OrientationBottomLeft,
    OrientationTopLeft,
    OrientationUp,
    OrientationDown,
    Tilt,
    FreeFall,
    SingleTap,
    DoubleTap,
    WakeUp,
    Pedometer
}
