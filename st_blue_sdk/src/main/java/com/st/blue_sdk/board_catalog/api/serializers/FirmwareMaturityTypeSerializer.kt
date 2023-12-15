package com.st.blue_sdk.board_catalog.api.serializers

import com.st.blue_sdk.board_catalog.models.FirmwareMaturity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class FirmwareMaturityTypeSerializer : KSerializer<FirmwareMaturity> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FirmwareMaturityType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): FirmwareMaturity =
        FirmwareMaturity.valueOf(decoder.decodeString().uppercase())

    override fun serialize(encoder: Encoder, value: FirmwareMaturity) {
        val encoded = value.name.lowercase()
        encoder.encodeString(encoded)
    }
}