package com.st.blue_sdk.features.extended.raw_pnpl_controlled.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawPnPLCustom(
    @SerialName(value = "output")
    val output: List<RawPnPLCustomEntry>
)
