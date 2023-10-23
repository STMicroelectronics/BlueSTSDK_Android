package com.st.blue_sdk.board_catalog.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FilterConfiguration(
    @SerialName(value = "highPass")
    var highPass: CutOff?=null,
    @SerialName(value = "lowPass")
    var lowPass: CutOff?=null
)
