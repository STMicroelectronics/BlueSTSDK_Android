/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota.nrg.response

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
class ExpectedImageSeqNumber(
    val nextExpectedCharBlock: FeatureField<Int>,
    val errorAck: FeatureField<ErrorCode>
) : Loggable {

    override val logHeader: String
        get() = "${nextExpectedCharBlock.logHeader}, ${errorAck.logHeader}"
    override val logValue: String
        get() = "${nextExpectedCharBlock.logHeader}, ${errorAck.logHeader}"

    enum class ErrorCode {

        FLASH_WRITE_FAILED, FLASH_VERIFY_FAILED, CHECK_SUM_ERROR, SEQUENCE_ERROR, NO_ERROR, UNKNOWN_ERROR;

        companion object {
            fun buildErrorCode(ack: Byte): ErrorCode {
                return when (ack) {
                    0xFF.toByte() -> FLASH_WRITE_FAILED
                    0x3C.toByte() -> FLASH_VERIFY_FAILED
                    0x0F.toByte() -> CHECK_SUM_ERROR
                    0xF0.toByte() -> SEQUENCE_ERROR
                    0.toByte() -> NO_ERROR
                    else -> UNKNOWN_ERROR
                }
            }
        }
    }
}
