package com.st.blue_sdk.board_catalog.api.serializers

import com.st.blue_sdk.board_catalog.models.BoardStatus
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = BoardStatus::class)
class BoardStatusTypeSerializer : KSerializer<BoardStatus> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BoardStatus", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BoardStatus =
        BoardStatus.valueOf(decoder.decodeString().uppercase())

    override fun serialize(encoder: Encoder, value: BoardStatus) {
        val encoded = value.name.lowercase()
        encoder.encodeString(encoded)
    }
}