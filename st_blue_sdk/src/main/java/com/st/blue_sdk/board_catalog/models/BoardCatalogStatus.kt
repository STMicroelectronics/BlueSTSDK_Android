@file:UseSerializers(DateSerializer::class)
package com.st.blue_sdk.board_catalog.models


import com.st.blue_sdk.board_catalog.api.serializers.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.Date

@Serializable
data class BoardCatalogStatus(
    @SerialName(value = "deprecationDate")
    val deprecationDate: Date?=null,
    @SerialName(value = "dismissionDate")
    val dismissionDate: Date?=null,
)

fun BoardCatalogStatus.isDeprecated(): Boolean {
    val currentDate = Date()
    return currentDate.after(deprecationDate)
}

fun BoardCatalogStatus.isDismissed(): Boolean {
    val currentDate = Date()
    return currentDate.after(dismissionDate)
}

