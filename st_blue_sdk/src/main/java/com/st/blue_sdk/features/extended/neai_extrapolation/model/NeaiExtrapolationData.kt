
package com.st.blue_sdk.features.extended.neai_extrapolation.model
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.experimental.and

@Serializable
data class NeaiExtrapolationData(
    @SerialName("phase")
    @Serializable(with = PhaseTypeSerializer::class)
    val phase: PhaseType,
    @Serializable(with = StateTypeSerializer::class)
    @SerialName("state")
    val state: StateType?=null,
    @SerialName("target")
    val target: Float?=null,
    @SerialName("unit")
    val unit: String?=null,
    @SerialName("stub")
    val stub: Boolean=true
)

object PhaseTypeSerializer : KSerializer<PhaseType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("PhaseType", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: PhaseType) {
        encoder.encodeInt(getPhaseCode(phase = value))
    }

    override fun deserialize(decoder: Decoder): PhaseType {
        return getPhaseValue(decoder.decodeInt().toShort())
    }
}

object StateTypeSerializer : KSerializer<StateType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StateType", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: StateType) {
        encoder.encodeInt(getStateCode(state = value))
    }

    override fun deserialize(decoder: Decoder): StateType {
        return getStateValue(decoder.decodeInt().toShort())
    }
}

enum class PhaseType {
    Idle,
    Extrapolation,
    Busy,
    Null
}

enum class StateType {
    Ok,
    Init_Not_Called,
    Board_Error,
    Knowledge_Error,
    Not_Enough_Learning,
    Minimal_Learning_done,
    Unknown_Error,
    Null
}


fun getPhaseValue(phase: Short) = when ((phase and 0xFF).toInt()) {
    0x00 -> PhaseType.Idle
    0x01 -> PhaseType.Extrapolation
    0x02 -> PhaseType.Busy
    else -> PhaseType.Null
}

fun getPhaseCode(phase: PhaseType) = when (phase) {
    PhaseType.Idle -> 0x00
    PhaseType.Extrapolation -> 0x01
    PhaseType.Busy -> 0x02
    PhaseType.Null -> 0xFF
}

fun getStateValue(state: Short) = when ((state and 0xFF).toInt()) {
    0x00 -> StateType.Ok
    0x7B -> StateType.Init_Not_Called
    0x7C -> StateType.Board_Error
    0x7D -> StateType.Knowledge_Error
    0x7E -> StateType.Not_Enough_Learning
    0x7F -> StateType.Minimal_Learning_done
    0x80 -> StateType.Unknown_Error
    else -> StateType.Null
}

fun getStateCode(state: StateType) = when (state) {
    StateType.Ok -> 0x00
    StateType.Init_Not_Called -> 0x7B
    StateType.Board_Error -> 0x7C
    StateType.Knowledge_Error -> 0x7D
    StateType.Not_Enough_Learning -> 0x7E
    StateType.Minimal_Learning_done -> 0x7F
    StateType.Unknown_Error -> 0x80
    StateType.Null -> 0xFF
}