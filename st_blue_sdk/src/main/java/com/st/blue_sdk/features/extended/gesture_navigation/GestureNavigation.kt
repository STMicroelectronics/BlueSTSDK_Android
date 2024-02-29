package com.st.blue_sdk.features.extended.gesture_navigation

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class GestureNavigation(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<GestureNavigationInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {

        const val NAME = "Navigation"
        const val NUMBER_BYTES = 2
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<GestureNavigationInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }
        val gesture = NumberConversion.byteToUInt8(data, dataOffset)
        val button = NumberConversion.byteToUInt8(data, dataOffset + 1)
        val gestureInfo = GestureNavigationInfo(
            gesture = FeatureField(
                name = "Gesture",
                max = GestureNavigationGestureType.Error,
                min = GestureNavigationGestureType.Undefined,
                value = GestureNavigationGestureType.fromShort(gesture)
            ),
            button = FeatureField(
                name = "Button",
                max = GestureNavigationButton.Error,
                min = GestureNavigationButton.Undefined,
                value = GestureNavigationButton.fromShort(button)
            )
        )
        return FeatureUpdate(
            featureName = name,
            rawData = data,
            readByte = NUMBER_BYTES,
            timeStamp = timeStamp,
            data = gestureInfo
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}