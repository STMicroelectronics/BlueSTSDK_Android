package com.st.blue_sdk.features.extended.gesture_navigation

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class GestureNavigationInfo(
    val gesture: FeatureField<GestureNavigationGestureType>,
    val button: FeatureField<GestureNavigationButton>
) : Loggable {
    override val logHeader: String = "${gesture.logHeader}, ${button.logHeader}"

    override val logValue: String = "${gesture.value}, ${button.value}"

    override val logDoubleValues: List<Double> =
        listOf(gesture.value.value.toDouble(), button.value.value.toDouble())

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${gesture.name} = ${gesture.value}\n")
        sampleValue.append("\t${button.name} = ${button.value}\n")
        return sampleValue.toString()
    }
}

enum class GestureNavigationGestureType(val value: Short) {
    Undefined(0),
    SwipeLeftToRight(1),
    SwipeRightToLeft(2),
    SwipeUpToDown(3),
    SwipeDownToUp(4),
    SinglePress(5),
    DoublePress(6),
    TriplePress(7),
    LongPress(8),
    Error(9);

    companion object {
        fun fromShort(value: Short) = entries.first { it.value == value }
    }
}

enum class GestureNavigationButton(val value: Short) {
    Undefined(0),
    Left(1),
    Right(2),
    Up(3),
    Down(4),
    Error(5);

    companion object {
        fun fromShort(value: Short) = values().first { it.value == value }
    }
}