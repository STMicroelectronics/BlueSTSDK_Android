/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.logging.sd

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
class SDLoggingInfo(
    val loggingStatus: FeatureField<LoggingStatus>,
    val featureIds: FeatureField<Set<Int>>,
    val logInterval: FeatureField<Long>
) : Loggable {

    override val logHeader: String =
        "${loggingStatus.logHeader} ,${featureIds.logHeader}, ${logInterval.logHeader}"

    override val logValue: String =
        "${loggingStatus.logHeader} ,${featureIds.logValue}, ${logInterval.logValue}"
}

enum class LoggingStatus {
    STOPPED, STARTED, NO_SD, IO_ERROR
}