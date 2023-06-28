@file:UseSerializers(DateSerializer::class)

/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features

import com.st.blue_sdk.board_catalog.api.serializers.DateSerializer
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class FeatureUpdate<T>(
    val readByte: Int,
    val timeStamp: Long,
    val notificationTime: Date = Date(),
    val data: T,
    val rawData: ByteArray
) : Loggable where T : Loggable {
    override val logHeader: String =
        "notificationTime, timeStamp, RawData, ${data.logHeader.replace("%", "%%")}"

    private val rawDataString = rawData.contentToString().replace(", ", " ")

    override val logValue: String =
        "$notificationTime, $timeStamp, $rawDataString, ${data.logValue}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeatureUpdate<*>

        if (readByte != other.readByte) return false

        if (timeStamp != other.timeStamp) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = readByte
        result = 31 * result + data.hashCode()
        return result
    }

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\ttimeStamp =$timeStamp\n")
        sampleValue.append("\treadByte = ${readByte}\n")
        sampleValue.append("\tdata =\n${data}\n")
        sampleValue.append("Details:\n")
        sampleValue.append("\tnotificationTime = ${notificationTime}\n")
        sampleValue.append("\trawData = ${rawDataString}\n")
        return sampleValue.toString()
    }
}
