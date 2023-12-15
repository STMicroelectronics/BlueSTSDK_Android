/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.api.serializers

import com.st.blue_sdk.board_catalog.models.BoardFotaType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class BoardFotaTypeSerializer : KSerializer<BoardFotaType> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BoardFotaType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BoardFotaType =
        BoardFotaType.valueOf(decoder.decodeString().uppercase())

    override fun serialize(encoder: Encoder, value: BoardFotaType) {
        val encoded = value.name.lowercase()
        encoder.encodeString(encoded)
    }
}
