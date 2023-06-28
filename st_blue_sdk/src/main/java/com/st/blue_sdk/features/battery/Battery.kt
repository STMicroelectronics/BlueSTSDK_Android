/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.battery

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.battery.request.GetBatteryCapacity
import com.st.blue_sdk.features.battery.request.GetBatteryMaxAbsorbedCurrent
import com.st.blue_sdk.features.battery.response.BatteryAbsorbedCurrentResponse
import com.st.blue_sdk.features.battery.response.BatteryCapacityResponse
import com.st.blue_sdk.utils.NumberConversion
import kotlin.experimental.and
import kotlin.math.max
import kotlin.math.min

class Battery(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<BatteryInfo>(name = name, type = type, isEnabled = isEnabled, identifier = identifier) {

    companion object {

        const val NAME = "Battery"

        const val UNKNOWN_CURRENT_VALUE = Short.MIN_VALUE

        const val COMMAND_GET_BATTERY_CAPACITY: Byte = 0x01
        const val COMMAND_GET_MAX_ABSORBED_CURRENT: Byte = 0X02
        const val NUMBER_BYTES = 7
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<BatteryInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val tempStatus = NumberConversion.byteToUInt8(data, dataOffset + 6)
        val tempCurrent = NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 4)
        val current = extractCurrentValue(tempCurrent, hasHighResolutionCurrent(tempStatus))

        val batteryInfo = BatteryInfo(
            percentage = FeatureField(
                value = extractPercentage(
                    NumberConversion.LittleEndian.bytesToInt16(
                        data,
                        dataOffset
                    )
                ), max = 100f, min = 0f, unit = "%", name = "Level"
            ),
            voltage = FeatureField(
                value = NumberConversion.LittleEndian.bytesToInt16(
                    data,
                    dataOffset + 2
                ) / 1000.0f, max = 10f, min = 0f, unit = "V", name = "Voltage"
            ),
            current = FeatureField(
                value = current, max = 10f, min = -10f, unit = "mA", name = "Current"
            ),
            status = FeatureField(
                value = getBatteryStatus(tempStatus),
                max = BatteryStatus.Error,
                min = BatteryStatus.LowBattery,
                name = "Status"
            )
        )

        return FeatureUpdate(
            rawData = data,
            readByte = NUMBER_BYTES,
            timeStamp = timeStamp,
            data = batteryInfo
        )
    }

    private fun getBatteryStatus(status: Short) = when ((status and 0x7F).toInt()) {
        0x00 -> BatteryStatus.LowBattery
        0x01 -> BatteryStatus.Discharging
        0x02 -> BatteryStatus.PluggedNotCharging
        0x03 -> BatteryStatus.Charging
        0x04 -> BatteryStatus.Unknown
        else -> BatteryStatus.Error
    }

    private fun extractPercentage(rawPercentage: Short): Float {
        val percentage = rawPercentage / 10.0f
        return max(0.0f, min(100.0f, percentage))
    }

    private fun hasUnknownCurrent(status: Short): Boolean {
        return status == UNKNOWN_CURRENT_VALUE
    }

    private fun extractCurrentValue(currentValue: Short, hightResolution: Boolean): Float {
        if (hasUnknownCurrent(currentValue)) return Float.NaN
        return if (hightResolution) currentValue.toFloat() * 0.1f // current/10
        else currentValue.toFloat()
    }

    private fun hasHighResolutionCurrent(status: Short): Boolean {
        return status and 0x80 != 0.toShort()
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is GetBatteryCapacity -> packCommandRequest(
                featureBit,
                COMMAND_GET_BATTERY_CAPACITY,
                byteArrayOf()
            )
            is GetBatteryMaxAbsorbedCurrent -> packCommandRequest(
                featureBit,
                COMMAND_GET_MAX_ABSORBED_CURRENT,
                byteArrayOf()
            )
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return unpackCommandResponse(data)?.let {
            if (mask != it.featureMask) return null

            when (it.commandId) {
                COMMAND_GET_BATTERY_CAPACITY -> {
                    val capacity = NumberConversion.LittleEndian.bytesToUInt16(it.payload)
                    BatteryCapacityResponse(
                        feature = this,
                        commandId = it.commandId,
                        capacity = capacity
                    )
                }
                COMMAND_GET_MAX_ABSORBED_CURRENT -> {
                    val current = NumberConversion.LittleEndian.bytesToInt16(data) / 10.0f
                    BatteryAbsorbedCurrentResponse(
                        feature = this,
                        commandId = it.commandId,
                        current = current
                    )
                }
                else -> null
            }
        }
    }
}