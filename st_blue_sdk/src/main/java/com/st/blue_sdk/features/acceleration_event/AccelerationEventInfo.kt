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
    val numSteps: FeatureField<Short>
) : Loggable {
    override val logHeader: String =
        (listOf(numSteps) + accEvent).joinToString(separator = ", ") { it.logHeader }

    override val logValue: String =
        (listOf(numSteps) + accEvent).joinToString(separator = ", ") { it.logValue }

    override val logDoubleValues: List<Double> =
        accEvent.map { retAccelerationTypeCode(it.value).toDouble() }

    override fun toString(): String {
        val sampleValue = StringBuilder()
        accEvent.forEach { sampleValue.append("\t${it.name} = ${it.value}\n") }
        numSteps.value.let { sampleValue.append("\t${numSteps.name} = ${numSteps.value}\n") }
        return sampleValue.toString()
    }

    companion object {
        fun emptyAccelerationEventInfo() =
            AccelerationEventInfo(
                accEvent = emptyList(), numSteps = FeatureField(
                    value = 0,
                    name = "Steps"
                )
            )
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

fun retAccelerationTypeCode(accEvent: AccelerationType) = when (accEvent) {
    AccelerationType.OrientationTopRight -> 1
    AccelerationType.OrientationBottomRight -> 2
    AccelerationType.OrientationBottomLeft -> 3
    AccelerationType.OrientationTopLeft -> 4
    AccelerationType.OrientationUp -> 5
    AccelerationType.OrientationDown -> 6
    AccelerationType.Tilt -> 1.shl(3)
    AccelerationType.FreeFall -> 1.shl(4)
    AccelerationType.SingleTap -> 1.shl(5)
    AccelerationType.DoubleTap -> 1.shl(6)
    AccelerationType.WakeUp -> 1.shl(7)
    AccelerationType.Pedometer -> 1.shl(8)
    else -> 0
}

fun getAccelerationType(accCode: Int) = when (accCode) {
    1 -> AccelerationType.OrientationTopRight
    2 -> AccelerationType.OrientationBottomRight
    3 -> AccelerationType.OrientationBottomLeft
    4 -> AccelerationType.OrientationTopLeft
    5 -> AccelerationType.OrientationUp
    6 -> AccelerationType.OrientationDown
    1.shl(3) -> AccelerationType.Tilt
    1.shl(4) -> AccelerationType.FreeFall
    1.shl(5) -> AccelerationType.SingleTap
    1.shl(6) -> AccelerationType.DoubleTap
    1.shl(7) -> AccelerationType.WakeUp
    1.shl(8) -> AccelerationType.Pedometer
    else -> AccelerationType.NoEvent
}