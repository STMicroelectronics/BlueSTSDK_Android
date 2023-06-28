package com.st.blue_sdk.board_catalog.api.serializers

import com.st.blue_sdk.board_catalog.models.BootLoaderType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = BootLoaderType::class)
class BootLoaderTypeSerializer : KSerializer<BootLoaderType> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BootLoaderType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BootLoaderType =
        BootLoaderType.valueOf(decoder.decodeString().uppercase())

    override fun serialize(encoder: Encoder, value: BootLoaderType) {
        val encoded = value.name.lowercase()
        encoder.encodeString(encoded)
    }
}