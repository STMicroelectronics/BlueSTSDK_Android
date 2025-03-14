package com.st.blue_sdk.board_catalog.models

import androidx.room.ColumnInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ExtraExamplesFlow(
    @ColumnInfo(name = "model")
    @SerialName(value = "model")
    val model: String?=null,
    @ColumnInfo(name = "examples_flow")
    @SerialName(value = "examples_flow")
    val examplesFlow: List<String> = listOf()
)
