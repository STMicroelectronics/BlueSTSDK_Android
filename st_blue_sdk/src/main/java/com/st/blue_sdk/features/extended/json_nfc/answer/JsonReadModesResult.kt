package com.st.blue_sdk.features.extended.json_nfc.answer

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class JsonReadModesResult(
    @SerialName("Answer")
    val Answer: String?
)