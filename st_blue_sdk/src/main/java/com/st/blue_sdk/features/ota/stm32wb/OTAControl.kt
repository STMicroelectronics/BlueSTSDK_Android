/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota.stm32wb

import android.util.Log
import com.st.blue_sdk.LoggableUnit
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.ota.stm32wb.requests.CancelUpload
import com.st.blue_sdk.features.ota.stm32wb.requests.FinishUpload
import com.st.blue_sdk.features.ota.stm32wb.requests.StartUpload
import com.st.blue_sdk.features.ota.stm32wb.requests.StopUpload
import com.st.blue_sdk.utils.NumberConversion

class OTAControl(
    name: String = NAME,
    type: Type = Type.EXTERNAL_STM32,
    isEnabled: Boolean,
    identifier: Int
) : Feature<LoggableUnit>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    isDataNotifyFeature = false
) {

    companion object {

        const val NAME = "OTAControlFeature"
        const val STOP_COMMAND: Byte = 0x00
        const val START_M0_COMMAND: Byte = 0x01
        const val START_M4_COMMAND: Byte = 0x02
        const val UPLOAD_FINISHED_COMMAND: Byte = 0x07
        const val CANCEL_COMMAND: Byte = 0x08
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is StartUpload -> {
                var commandBytes = NumberConversion.BigEndian.uint32ToBytes(command.address)
                if(command.nbSectorsToErase != null) commandBytes += command.nbSectorsToErase.toByte()
                commandBytes[0] = command.commandId
                return commandBytes
            }
            is FinishUpload -> byteArrayOf(command.commandId)
            is CancelUpload -> byteArrayOf(command.commandId)
            is StopUpload -> byteArrayOf(command.commandId)
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<LoggableUnit> = FeatureUpdate(
        featureName = name,
        timeStamp = timeStamp,
        rawData = byteArrayOf(),
        readByte = 0,
        data = LoggableUnit()
    )
}
