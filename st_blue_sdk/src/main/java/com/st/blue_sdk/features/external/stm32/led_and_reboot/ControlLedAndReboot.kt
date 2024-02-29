/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.external.stm32.led_and_reboot

import com.st.blue_sdk.LoggableUnit
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate

class ControlLedAndReboot(
    isEnabled: Boolean,
    type: Type = Type.EXTERNAL_STM32,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = false
) : Feature<LoggableUnit>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp,
    isDataNotifyFeature = false
) {

    companion object {
        const val NAME = "ControlLedAndRadioProtocolReboot"

        const val SWITCH_OFF_COMMAND: Byte = 0x00
        const val SWITCH_ON_COMMAND: Byte = 0x01
        const val RADIO_REBOOT_COMMAND: Byte = 0x02
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<LoggableUnit> {

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = byteArrayOf(),
            readByte = 0,
            data = LoggableUnit()
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is ControlLedCommand -> {
                byteArrayOf(command.deviceId) + if (command.turnOn) SWITCH_ON_COMMAND else SWITCH_OFF_COMMAND
            }
            is RebootCommand -> byteArrayOf(command.deviceId, RADIO_REBOOT_COMMAND)
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}