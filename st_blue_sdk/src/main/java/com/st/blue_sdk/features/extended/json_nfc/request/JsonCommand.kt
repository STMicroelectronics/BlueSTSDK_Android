package com.st.blue_sdk.features.extended.json_nfc.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class JsonCommand(
    @SerialName("Command")
    val Command: String? = null,
    @SerialName("GenericText")
    val GenericText: String? = null,
    @SerialName("NFCWiFi")
    val NFCWiFi: JsonWIFI? = null,
    @SerialName("NFCVCard")
    var NFCVCard: JsonVCard? = null,
    @SerialName("NFCURL")
    var NFCURL: String? = null
) {
    companion object {
        const val ReadModes = "ReadModes"
        const val NFCWifi = "NFCWiFi"
        const val NFCVCard = "NFCVCard"
        const val NFCURL = "NFCURL"
        const val NFCText = "GenericText"

        val WifiEncrString = mapOf("NONE" to 1, "WEP" to 2, "TKIP" to 4, "AES" to 8)

        /*
         NDEF_WIFI_ENCRYPTION_NONE = 0x0001, /**< WPS No Encryption (set to 0 for Android native support / should be 1) */
         NDEF_WIFI_ENCRYPTION_WEP  = 0x0002, /**< WPS Encryption based on WEP  */
         NDEF_WIFI_ENCRYPTION_TKIP = 0x0004, /**< WPS Encryption based on TKIP  */
         NDEF_WIFI_ENCRYPTION_AES  = 0x0008 /**< WPS Encryption based on AES  */
        */

        val WifiAuthString = mapOf(
            "NONE" to 1,
            "WPAPSK" to 2,
            "SHARED" to 4,
            "WPA" to 8,
            "WPA2" to 16,
            "WPA2PSK" to 32
        )

        /*
         NDEF_WIFI_AUTHENTICATION_NONE     = 0x0001, /**< WPS No Authentication (set to 0 for Android native support / should be 1)  */
         NDEF_WIFI_AUTHENTICATION_WPAPSK   = 0x0002, /**< WPS Authentication based on WPAPSK  */
         NDEF_WIFI_AUTHENTICATION_SHARED   = 0x0004, /**< WPS Authentication based on ??  */
         NDEF_WIFI_AUTHENTICATION_WPA      = 0x0008, /**< WPS Authentication based on WPA  */
         NDEF_WIFI_AUTHENTICATION_WPA2     = 0x0010, /**< WPS Authentication based on WPA2  */
         NDEF_WIFI_AUTHENTICATION_WPA2PSK  = 0x0020 /**< WPS Authentication based on WPA2PSK  */
        */

        val UrlTypeString = mapOf("http://www." to 1, "https://www." to 2)
    }
}
