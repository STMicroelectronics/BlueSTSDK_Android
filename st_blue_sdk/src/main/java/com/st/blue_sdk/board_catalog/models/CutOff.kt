package com.st.blue_sdk.board_catalog.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CutOff(
    @SerialName(value = "label")
    var label: String,
    @SerialName(value = "value")
    var value: Int
) {
    override fun toString(): String {
        return "Cutoff{" +
                "label='" + label + '\'' +
                ", value=" + value +
                '}'
    }
}
