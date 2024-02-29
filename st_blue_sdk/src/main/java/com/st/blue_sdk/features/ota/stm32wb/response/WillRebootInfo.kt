/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota.stm32wb.response

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.logger.Loggable

class WillRebootInfo(feature: Feature<*>, commandId: Byte, val infoType: WillRebootInfoType) :
    FeatureResponse(feature, commandId), Loggable {

    override val logHeader: String = ""

    override val logValue: String = ""

    override val logDoubleValues: List<Double> = listOf()
}

enum class WillRebootInfoType {
    REBOOT,
    READY_TO_RECEIVE_FILE,
    ERROR_NO_FREE,
    OTHER
}