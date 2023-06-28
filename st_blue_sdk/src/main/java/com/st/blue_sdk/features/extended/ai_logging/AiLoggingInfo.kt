/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.ai_logging

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
class AiLoggingInfo(
    val isLogging: FeatureField<LoggingStatus>
) : Loggable {
    override val logHeader: String = isLogging.logHeader

    override val logValue: String = isLogging.logValue

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${isLogging.name} = ${isLogging.value} ${isLogging.unit}\n")
        return sampleValue.toString()
    }
}

enum class LoggingStatus {
    STOPPED,
    STARTED,
    NO_SD,
    IO_ERROR,
    UPDATE,
    UNKNOWN
}