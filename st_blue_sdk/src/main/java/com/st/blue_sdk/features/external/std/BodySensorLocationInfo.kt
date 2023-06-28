package com.st.blue_sdk.features.external.std

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class BodySensorLocationInfo(
    val bodySensorLocation: FeatureField<BodySensorLocationType>,

    ) : Loggable {
    override val logHeader: String =
        bodySensorLocation.logHeader

    override val logValue: String =
        bodySensorLocation.logValue

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${bodySensorLocation.name} = ${bodySensorLocation.value}\n")

        return sampleValue.toString()
    }
}

enum class BodySensorLocationType {
    Other,
    Chest,
    Wrist,
    Finger,
    Hand,
    EarLobe,
    Foot,
    NotKnown
}
