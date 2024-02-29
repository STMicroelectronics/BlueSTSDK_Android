/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.mems_gesture

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion
import kotlin.experimental.and

class MemsGesture(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<MemsGestureInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "MEMS Gesture"

        fun getGestureType(gesture: Short) = when ((gesture and 0x0F).toInt()) {
            0x00 -> MemsGestureType.Unknown
            0x01 -> MemsGestureType.PickUp
            0x02 -> MemsGestureType.Glance
            0x03 -> MemsGestureType.WakeUp
            else -> MemsGestureType.Error
        }

        fun getGestureTypeCode(gesture: MemsGestureType) = when (gesture) {
            MemsGestureType.Unknown -> 0x00
            MemsGestureType.PickUp -> 0x01
            MemsGestureType.Glance -> 0x02
            MemsGestureType.WakeUp -> 0x03
            MemsGestureType.Error -> 0x0F
        }
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<MemsGestureInfo> {
        require(data.size - dataOffset > 0) { "There are no bytes available to read for $name feature" }

        val gesture = MemsGestureInfo(
            gesture = FeatureField(
                value = getGestureType(NumberConversion.byteToUInt8(data, dataOffset)),
                min = MemsGestureType.Unknown,
                max = MemsGestureType.Error,
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