package com.st.blue_sdk.features.extended.asset_tracking_event

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventData
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventType
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingOrientationType
import com.st.blue_sdk.features.extended.asset_tracking_event.model.FallAssetTrackingEvent
import com.st.blue_sdk.features.extended.asset_tracking_event.model.ShockAssetTrackingEvent
import com.st.blue_sdk.features.extended.asset_tracking_event.model.StatusAssetTrackingEvent
import com.st.blue_sdk.utils.NumberConversion

class AssetTrackingEvent(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<AssetTrackingEventInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Asset Tracking Event"
        const val NUMBER_BYTES_FALL = 5
        const val NUMBER_BYTES_SHOCK = 1 + 4 * 4
        const val NUMBER_BYTES_FULL_SHOCK = 1 + 4 * 4 + 3 * 1 + 3 * 4
        const val NUMBER_BYTES_STATUS = 1 + 4 + 1
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<AssetTrackingEventInfo> {

        var code = NumberConversion.byteToUInt8(data, dataOffset)
        val type = AssetTrackingEventType.getAssetTrackingEvent(code)

        var fall: FallAssetTrackingEvent? = null
        var shock: ShockAssetTrackingEvent? = null
        var status: StatusAssetTrackingEvent? = null

        when (type) {
            AssetTrackingEventType.Fall -> {

                require(data.size - dataOffset == NUMBER_BYTES_FALL) { "We need $NUMBER_BYTES_FALL bytes available for Fall Event  $name feature " }

                val heightCm = NumberConversion.LittleEndian.bytesToFloat(
                    data,
                    dataOffset + 1
                )

                fall = FallAssetTrackingEvent(heightCm = heightCm)
            }

            AssetTrackingEventType.Shock -> {

                require(data.size - dataOffset >= NUMBER_BYTES_SHOCK) { "We need $NUMBER_BYTES_SHOCK bytes available for Shock Event  $name feature " }
                val durationMSec = NumberConversion.LittleEndian.bytesToFloat(
                    data,
                    dataOffset + 1
                )
                val intensityX = NumberConversion.LittleEndian.bytesToFloat(
                    data,
                    dataOffset + 1 + 4
                )
                val intensityY = NumberConversion.LittleEndian.bytesToFloat(
                    data,
                    dataOffset + 1 + 2 * 4
                )
                val intensityZ = NumberConversion.LittleEndian.bytesToFloat(
                    data,
                    dataOffset + 1 + 3 * 4
                )

                //Check if we have the Full Shock Event format
                if (data.size - dataOffset > NUMBER_BYTES_SHOCK) {
                    require(data.size - dataOffset == NUMBER_BYTES_FULL_SHOCK) { "We need $NUMBER_BYTES_FULL_SHOCK bytes available for Full Shock Event  $name feature " }
                    code = NumberConversion.byteToUInt8(data, dataOffset + 1 + 4 * 4)
                    val orientationX =
                        AssetTrackingOrientationType.getAssetTrackingOrientationCode(code)
                    code = NumberConversion.byteToUInt8(data, dataOffset + 1 + 4 * 4 + 1)
                    val orientationY =
                        AssetTrackingOrientationType.getAssetTrackingOrientationCode(code)
                    code = NumberConversion.byteToUInt8(data, dataOffset + 1 + 4 * 4 + 2 * 1)
                    val orientationZ =
                        AssetTrackingOrientationType.getAssetTrackingOrientationCode(code)
                    val angleX = NumberConversion.LittleEndian.bytesToFloat(
                        data,
                        dataOffset + 1 + 4 * 4 + 3 * 1
                    )
                    val angleY = NumberConversion.LittleEndian.bytesToFloat(
                        data,
                        dataOffset + 1 + 4 * 4 + 3 * 1 + 4
                    )
                    val angleZ = NumberConversion.LittleEndian.bytesToFloat(
                        data,
                        dataOffset + 1 + 4 * 4 + 3 * 1 + 2 * 4
                    )
                    shock = ShockAssetTrackingEvent(
                        durationMSec = durationMSec,
                        intensityG = floatArrayOf(intensityX, intensityY, intensityZ),
                        orientations = arrayOf(orientationX, orientationY, orientationZ),
                        angles = floatArrayOf(angleX, angleY, angleZ)
                    )
                } else {
                    shock = ShockAssetTrackingEvent(
                        durationMSec = durationMSec,
                        intensityG = floatArrayOf(intensityX, intensityY, intensityZ),
                        orientations = emptyArray(),
                        angles = floatArrayOf()
                    )
                }

            }

            AssetTrackingEventType.Motion, AssetTrackingEventType.Stationary -> {
                require(data.size - dataOffset == NUMBER_BYTES_STATUS) { "We need $NUMBER_BYTES_STATUS bytes available for Status Event  $name feature " }
                val current = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset+1)

                //Read and Clip PowerIndex to 1..10
                val powerIndex = NumberConversion.byteToUInt8(data, dataOffset + 1 + 4).coerceIn(1,10)

                status = StatusAssetTrackingEvent(current = current, powerIndex = powerIndex)
            }

            AssetTrackingEventType.Reset, AssetTrackingEventType.Null -> {}
        }

        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = AssetTrackingEventInfo(
                event = FeatureField(
                    value = AssetTrackingEventData(
                        type = type,
                        fall = fall,
                        shock = shock,
                        status = status
                    ), name = "Asset Tracking Event"
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}

