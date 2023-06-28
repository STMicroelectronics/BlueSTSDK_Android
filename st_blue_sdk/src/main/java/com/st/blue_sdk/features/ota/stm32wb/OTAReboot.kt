/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota.stm32wb

import com.st.blue_sdk.LoggableUnit
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.ota.stm32wb.requests.RebootToOTAMode

class OTAReboot(
    isEnabled: Boolean,
    type: Type = Type.EXTERNAL_STM32,
    identifier: Int,
    name: String = NAME
) : Feature<LoggableUnit>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    isDataNotifyFeature = false
) {

    companion object {
        const val NAME = "OTAReboot"
        const val REBOOT_OTA_MODE: Byte = 0x01
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<LoggableUnit> {
        return FeatureUpdate(
            timeStamp = timeStamp,
            rawData = byteArrayOf(),
            readByte = 0,
            data = LoggableUnit()
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is RebootToOTAMode -> byteArrayOf(
                command.commandId,
                command.sectorOffset,
                command.numSector
            )
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return null
    }
}