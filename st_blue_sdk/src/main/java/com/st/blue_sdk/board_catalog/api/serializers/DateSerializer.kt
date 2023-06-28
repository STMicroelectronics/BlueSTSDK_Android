/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.api.serializers

import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

@Serializer(forClass = Date::class)
class DateSerializer : KSerializer<Date> {

    private var formatStrings = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm",
        "yyyy-MM-dd"
    )

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        val encoded = SimpleDateFormat(formatStrings.first(), Locale.getDefault()).format(value)
        encoder.encodeString(encoded)
    }

    override fun deserialize(decoder: Decoder): Date {
        val value = decoder.decodeString()
        formatStrings.forEach { formatter ->
            val format = SimpleDateFormat(formatter, Locale.getDefault())
            try {
                return format.parse(value)!!
            } catch (e: Exception) {
                Log.d(DateSerializer::class.simpleName, "Error parsing Date: $value")
            }
        }

        throw IllegalArgumentException("Date format not supported")
    }
}
