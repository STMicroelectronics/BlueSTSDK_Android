/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.external.stm32.switch_status

import com.st.blue_sdk.features.*

class SwitchStatus(
    isEnabled: Boolean,
    type: Type = Type.EXTERNAL_STM32,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = false
) : Feature<SwitchInfo>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp
) {

    companion object {

        const val NAME = "SwitchInfo"

        private const val SWITCH_STATUS_DEV_ID_INDEX = 0
        private const val SWITCH_STATUS_BUTTON_ID_INDEX = 1
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<SwitchInfo> {

        require(data.size - dataOffset >= 2) { "There are no 2 bytes available to read" }

        val deviceId = FeatureField(
            name = "DeviceId",
            max = 0x06,
            min = 0x00,
            value = data[SWITCH_STATUS_DEV_ID_INDEX]
        )

        val isSwitchPressed = FeatureField(
            name = "SwitchPressed",
            value = data[SWITCH_STATUS_BUTTON_ID_INDEX] == 0x01.toByte()
        )

        return FeatureUpdate(
            featureName = name,
            readByte = 2,
            timeStamp = timeStamp,
            rawData = data,
            data = SwitchInfo(deviceId, isSwitchPressed)
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}