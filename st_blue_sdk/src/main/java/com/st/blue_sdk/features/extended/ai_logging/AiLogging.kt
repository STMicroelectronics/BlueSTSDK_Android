/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.ai_logging

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.ai_logging.requst.StartLogging
import com.st.blue_sdk.features.extended.ai_logging.requst.StopLogging
import com.st.blue_sdk.features.extended.ai_logging.requst.UpdateAnnotation
import com.st.blue_sdk.utils.NumberConversion
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

class AiLogging(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<AiLoggingInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    isDataNotifyFeature = false
) {
    companion object {
        const val NAME = "AiLogging"
        const val NUMBER_BYTES = 1
        const val LOGGING_STOPPED = 0x00.toByte()
        const val LOGGING_STARTED = 0x01.toByte()
        const val LOGGING_NO_SD = 0x02.toByte()
        const val LOGGING_IO_ERROR = 0x03.toByte()
        private const val LOGGING_UPDATE = 0x04.toByte()
        const val LOGGING_UNKNOWN = 0XFF.toByte()
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<AiLoggingInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        return FeatureUpdate(
            timeStamp = timeStamp,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = AiLoggingInfo(
                isLogging = FeatureField(
                    name = "LoggingStatus",
                    value = when (data[dataOffset]) {
                        LOGGING_STOPPED -> LoggingStatus.STOPPED
                        LOGGING_STARTED -> LoggingStatus.STARTED
                        LOGGING_NO_SD -> LoggingStatus.NO_SD
                        LOGGING_IO_ERROR -> LoggingStatus.IO_ERROR
                        LOGGING_UPDATE -> LoggingStatus.UPDATE
                        LOGGING_UNKNOWN -> LoggingStatus.UNKNOWN
                        else -> LoggingStatus.UNKNOWN
                    }
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? =
        when (command) {
            is StartLogging -> {
                val logMaskBytes = NumberConversion.LittleEndian.uint32ToBytes(command.logMask)
                val environmentFreqHZ = (command.environmentalFreq * 10).roundToInt()
                val environmentFreqBytes =
                    NumberConversion.LittleEndian.uint16ToBytes(environmentFreqHZ)
                val inertialFreqHZ = (command.environmentalFreq * 10).roundToInt()
                val inertialFreqBytes = NumberConversion.LittleEndian.uint16ToBytes(inertialFreqHZ)
                byteArrayOf(LOGGING_STARTED) + logMaskBytes + environmentFreqBytes + inertialFreqBytes + byteArrayOf(
                    command.audioVolume
                )
            }
            is StopLogging -> byteArrayOf(LOGGING_STOPPED)
            is UpdateAnnotation -> {
                val labelToByteArray = command.label.toByteArray(StandardCharsets.UTF_8)
                byteArrayOf(LOGGING_UPDATE) + labelToByteArray.take(18) + byteArrayOf('\u0000'.code.toByte())
            }
            else -> null
        }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}