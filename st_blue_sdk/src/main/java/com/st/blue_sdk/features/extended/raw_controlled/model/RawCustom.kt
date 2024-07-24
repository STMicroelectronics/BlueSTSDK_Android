package com.st.blue_sdk.features.extended.raw_controlled.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawCustom(
    @SerialName(value = "output")
    val output: List<RawCustomEntry>
)
