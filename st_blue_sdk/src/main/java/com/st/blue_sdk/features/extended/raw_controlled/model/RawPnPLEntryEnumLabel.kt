package com.st.blue_sdk.features.extended.raw_controlled.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawPnPLEntryEnumLabel(
    @SerialName(value = "value")
    val value: Int,
    @SerialName(value = "label")
    val label: String,
)