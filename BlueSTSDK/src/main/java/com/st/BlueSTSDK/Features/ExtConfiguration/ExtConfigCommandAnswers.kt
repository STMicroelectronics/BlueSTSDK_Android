package com.st.BlueSTSDK.Features.ExtConfiguration

import com.google.gson.annotations.SerializedName

// data class for reading the Configuration Commands result
data class ExtConfigCommandAnswers (
        @SerializedName("Commands") val CommandList: String?,
        @SerializedName("Info") val info: String?,
        @SerializedName("Help") val help: String?,
        @SerializedName("Certificate") val certificate: String?,
        @SerializedName("VersionFW") val versionFw: String?,
        @SerializedName("UID") val stm32UID: String?,
        @SerializedName("PowerStatus") val powerStatus: String?,
        @SerializedName("CustomCommands") val CustomCommandList: List<CustomCommand>?
)