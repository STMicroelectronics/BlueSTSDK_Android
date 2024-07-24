package com.st.blue_sdk.features.extended.pnpl.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonObject


@kotlinx.serialization.Serializable
data class PnPLResponse(
    @SerialName("schema_version")
    val schemaVersion: String,
    @SerialName("uuid")
    val uuid: String,
    @SerialName("devices")
    val devices: List<PnPLDevice>
)

@kotlinx.serialization.Serializable
data class PnPLDevice(
    @SerialName("board_id")
    val boardId: Int?,
    @SerialName("fw_id")
    val fwId: Int?,
    @SerialName("sn")
    val serialNumber: String?,
    @SerialName("pnpl_ble_responses")
    val pnplBleResponses: Boolean?,
    @SerialName("components")
    val components: List<JsonObject>
)