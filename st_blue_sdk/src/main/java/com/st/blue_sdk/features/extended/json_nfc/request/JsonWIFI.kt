package com.st.blue_sdk.features.extended.json_nfc.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class JsonWIFI(
    @SerialName("NetworkSSID")
    val NetworkSSID: String? = null,
    @SerialName("NetworkKey")
    val NetworkKey: String? = null,
    @SerialName("AuthenticationType")
    val AuthenticationType: Int = 0,
    @SerialName("EncryptionType")
    val EncryptionType: Int = 0
)