package com.st.blue_sdk.board_catalog.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DemoDecorator(
    @SerialName(value = "add")
    val add: List<String>,
    @SerialName(value = "remove")
    val remove: List<String>,
    @SerialName(value = "rename")
    val rename: List<DemoRename>
)

@Serializable
data class DemoRename(
    @SerialName(value = "old")
    val old: String,
    @SerialName(value = "new")
    val new: String
)