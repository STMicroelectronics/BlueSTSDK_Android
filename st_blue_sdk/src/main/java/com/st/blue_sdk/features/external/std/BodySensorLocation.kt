package com.st.blue_sdk.features.external.std

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.utils.NumberConversion

class BodySensorLocation(
    isEnabled: Boolean,
    type: Type,
    identifier: Int,
    name: String = NAME
) : Feature<BodySensorLocationInfo>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = false
) {

    companion object {
        const val NAME = "Body Sensor Location"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<BodySensorLocationInfo> {
        var offset = dataOffset
        val value = NumberConversion.byteToUInt8(
            data,
            dataOffset
        ).toInt()
        offset++

        return FeatureUpdate(
            featureName = name,
            readByte = offset - dataOffset,
            rawData = data,
            timeStamp = timeStamp,
            data = BodySensorLocationInfo(
                bodySensorLocation = FeatureField(
                    name = "Body Sensor Location",
                    value = getBodySensorLocationType(value)
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}

private fun getBodySensorLocationType(locationCode: Int) = when (locationCode) {
    0 -> BodySensorLocationType.Other
    1 -> BodySensorLocationType.Chest
    2 -> BodySensorLocationType.Wrist
    3 -> BodySensorLocationType.Finger
    4 -> BodySensorLocationType.Hand
    5 -> BodySensorLocationType.EarLobe
    6 -> BodySensorLocationType.Foot
    else -> BodySensorLocationType.NotKnown
}