/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota.stm32wb

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.ota.stm32wb.response.WillRebootInfo
import com.st.blue_sdk.features.ota.stm32wb.response.WillRebootInfoType

class OTAWillReboot(
    name: String = NAME,
    type: Type = Type.EXTERNAL_STM32,
    isEnabled: Boolean,
    identifier: Int
) : Feature<WillRebootInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = false,
    isDataNotifyFeature = false
) {

    companion object {
        const val NAME = "OTAWillReboot"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<WillRebootInfo> {

        require(data.size - dataOffset >= 1) { "There are byte available to read" }
        val infoType = when(data[dataOffset].toInt()) {
            1 -> WillRebootInfoType.REBOOT
            2 -> WillRebootInfoType.READY_TO_RECEIVE_FILE
            3 -> WillRebootInfoType.ERROR_NO_FREE
            else -> WillRebootInfoType.OTHER
        }
        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = 1,
            data = WillRebootInfo(this, -1, infoType)
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return null
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return null
    }
}