package com.st.blue_sdk.features.extended.json_nfc.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class JsonVCard(
    @SerialName("Name")
    var Name: String? = null,
    @SerialName("FormattedName")
    var FormattedName: String? = null,
    @SerialName("Title")
    var Title: String? = null,
    @SerialName("Org")
    var Org: String? = null,
    @SerialName("HomeAddress")
    var HomeAddress: String? = null,
    @SerialName("WorkAddress")
    var WorkAddress: String? = null,
    @SerialName("Address")
    var Address: String? = null,
    @SerialName("HomeTel")
    var HomeTel: String? = null,
    @SerialName("WorkTel")
    var WorkTel: String? = null,
    @SerialName("CellTel")
    var CellTel: String? = null,
    @SerialName("HomeEmail")
    var HomeEmail: String? = null,
    @SerialName("WorkEmail")
    var WorkEmail: String? = null,
    @SerialName("Url")
    var Url: String? = null
)