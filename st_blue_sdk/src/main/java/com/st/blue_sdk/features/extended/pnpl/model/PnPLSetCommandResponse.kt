package com.st.blue_sdk.features.extended.pnpl.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


@Serializable
data class PnPLSetCommandResponse(
    @SerialName(value = "PnPL_Response")
    val response: PnpLSetCommandResponseEntry?
)

@Serializable
data class PnpLSetCommandResponseEntry(
    @SerialName(value = "message")
    val message: String?= null,
    @SerialName(value = "status")
    val status: Boolean,
//    @SerialName(value = "value")
    // Not Correct because it could be also a scalar value
//    val value: JsonObject?=null
)

