package com.st.blue_sdk.features.extended.raw_pnpl_controlled.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawPnPLCustomEntry(
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "type")
    val type:RawPnPLCustomEntryFormat,
    @SerialName(value = "size")
    val elements: Int=1

) {
    @kotlinx.serialization.Transient
    var values: MutableList<Any> = mutableListOf()

    enum class RawPnPLCustomEntryFormat {
        uint8_t,
        char,
        int8_t,
        uint16_t,
        int16_t,
        uint32_t,
        int32_t,
        float
    }
}
