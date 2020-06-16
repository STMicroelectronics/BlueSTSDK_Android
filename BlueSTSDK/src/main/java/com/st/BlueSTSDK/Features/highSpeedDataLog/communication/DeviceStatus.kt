package com.st.BlueSTSDK.Features.highSpeedDataLog.communication

import com.google.gson.annotations.SerializedName

data class DeviceStatus (
    @SerializedName("type") val type: String?,
    @SerializedName("isLogging") val isSDLogging: Boolean?,
    @SerializedName("isSDInserted") val isSDCardInserted: Boolean?,
    @SerializedName("cpuUsage") val cpuUsage: Double?,
    @SerializedName("batteryVoltage") val batteryVoltage: Double?,
    @SerializedName("batteryLevel") val batteryLevel: Double?,
    @SerializedName("ssid") val ssid: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("ip") val ip: String?
)