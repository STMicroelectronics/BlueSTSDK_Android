/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.acceleration_event

import android.util.Log
import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.acceleration_event.request.EnableDetectionAccelerationEvent
import com.st.blue_sdk.features.acceleration_event.response.EnableAccelerationEventResponse
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration
import com.st.blue_sdk.utils.NumberConversion

class AccelerationEvent(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<AccelerationEventInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val ACC_EVENT_ENABLE_COMMAND = 0.toByte()
        const val NAME = "Acceleration Event"
    }

    private var mIsPedometerEnabled = false

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<AccelerationEventInfo> {
        require(data.size - dataOffset > 0) { "There are no bytes available to read for $name feature" }

        val numBytes = minOf(data.size - dataOffset, 3)

        val accEvent = if (numBytes >= 3) {
            //Extract Event&Pedometer data
            AccelerationEventInfo(
                accEvent = getListAccelerationType(
                    NumberConversion.byteToUInt8(
                        data,
                        dataOffset
                    ).toInt()
                ),
                numSteps = FeatureField(
                    value = NumberConversion.LittleEndian.bytesToUInt16(
                        data,
                        dataOffset + 1
                    )
                        .toShort(),
                    name = "Steps"
                ),
            )
        } else if ((numBytes >= 2) && mIsPedometerEnabled) {
            //Extract Pedometer data
            //if we have only pedometer data... we had also a pedometer event
            AccelerationEventInfo(
                numSteps = FeatureField(
                    value = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset)
                        .toShort(),
                    name = "Steps"
                ),
                accEvent = getListAccelerationType(retAccelerationTypeCode(AccelerationType.Pedometer))
            )
        } else {
            //Extract Event
            AccelerationEventInfo(
                numSteps = FeatureField(name = "numSteps", value = null),
                accEvent = getListAccelerationType(
                    NumberConversion.byteToUInt8(
                        data,
                        dataOffset
                    ).toInt()
                )
            )
        }
        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp, rawData = data, readByte = numBytes, data = accEvent
        )
    }

    private fun getListAccelerationType(accEvent: Int): List<FeatureField<AccelerationType>> {
        val accEventList: MutableList<FeatureField<AccelerationType>> = mutableListOf()

        //Add Orientation Event
        if ((accEvent and 7) != 0) {
            accEventList.add(
                FeatureField(
                    value = getAccelerationType(accEvent and 7),
                    name = "Acc Event"
                )
            )
        }

        //Add All other Events
        for (count in 3..8) {
            val singleEvent = accEvent and (1.shl(count))
            if (singleEvent != 0) {
                accEventList.add(
                    FeatureField(
                        value = getAccelerationType(singleEvent),
                        name = "Acc Event"
                    )
                )
            }
        }

        return accEventList.toList()
    }


    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        if (command is EnableDetectionAccelerationEvent) {
            return if (command.enable) {
                if (command.event == DetectableEventType.Pedometer) {
                    mIsPedometerEnabled = true
                }
                packCommandRequest(
                    featureBit,
                    command.event.byte,
                    byteArrayOf(1)
                )
            } else {
                if (command.event == DetectableEventType.Pedometer) {
                    mIsPedometerEnabled = false
                }
                packCommandRequest(
                    featureBit,
                    command.event.byte,
                    byteArrayOf(0)
                )
            }
        } else {
            return null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return unpackCommandResponse(data)?.let {
            if (mask != it.featureMask) return null

            when (it.commandId) {
                ACC_EVENT_ENABLE_COMMAND -> {
                    val event = DetectableEventType.create(it.payload[0])
                    EnableAccelerationEventResponse(
                        feature = this,
                        event = event
                    )
                }

                else -> null
            }
        }
    }
}

enum class DetectableEventType(val byte: Byte) {
    None(0.toByte()),
    Multiple('m'.code.toByte()),
    Orientation('o'.code.toByte()),
    Pedometer('p'.code.toByte()),
    SingleTap('s'.code.toByte()),
    DoubleTap('d'.code.toByte()),
    FreeFall('f'.code.toByte()),
    WakeUp('w'.code.toByte()),
    Tilt('t'.code.toByte());

    companion object {
        fun create(byte: Byte): DetectableEventType =
            try {
                entries.first { it.byte == byte }
            } catch (e: Exception) {
                Log.d("Acceleration Event", e.stackTraceToString())
                None
            }
    }
}