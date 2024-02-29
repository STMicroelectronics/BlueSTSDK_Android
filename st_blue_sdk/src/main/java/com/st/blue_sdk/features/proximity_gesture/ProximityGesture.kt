/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.proximity_gesture

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion
import kotlin.experimental.and

class ProximityGesture(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<ProximityGestureInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Proximity Gesture"

        fun getGestureType(gesture: Short) = when ((gesture and 0x0F).toInt()) {
            0x00 -> ProximityGestureType.Unknown
            0x01 -> ProximityGestureType.Tap
            0x02 -> ProximityGestureType.Left
            0x03 -> ProximityGestureType.Right
            else -> ProximityGestureType.Error
        }

        fun getGestureTypeCode(gesture: ProximityGestureType) = when (gesture) {
            ProximityGestureType.Unknown -> 0x00
            ProximityGestureType.Tap -> 0x01
            ProximityGestureType.Left -> 0x02
            ProximityGestureType.Right -> 0x03
            ProximityGestureType.Error -> 0x0F
        }
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<ProximityGestureInfo> {
        require(data.size - dataOffset > 0) { "There are no bytes available to read for $name feature" }

        val gesture = ProximityGestureInfo(
            gesture = FeatureField(
                value = getGestureType(NumberConversion.byteToUInt8(data, dataOffset)),
                min = ProximityGestureType.Unknown,
                max = ProximityGestureType.Error,
                name = "Gesture"
            )
        )
        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp, rawData = data, readByte = 1, data = gesture
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}