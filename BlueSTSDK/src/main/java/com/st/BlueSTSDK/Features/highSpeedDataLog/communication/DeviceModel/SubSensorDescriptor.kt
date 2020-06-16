package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import com.google.gson.annotations.SerializedName

data class SubSensorDescriptor(
    @SerializedName("id") val id : Int,
    @SerializedName("sensorType") val sensorType : SensorType,
    @SerializedName("dimensions") val dimensions : Int,
    @SerializedName("dimensionsLabel") val dimensionsLabel : List<String>,
    @SerializedName("unit") val unit : String?,
    @SerializedName("dataType") val dataType : String?,
    @SerializedName("FS") val fs : List<Double>?,
    @SerializedName("ODR") val odr : List<Double>?,
    @SerializedName("samplesPerTs") val samplesPerTs : SamplesPerTs
)
{

    val hasIntegerValue:Boolean = dataType?.contains("int", true) ?: false
    val hasFloatValue:Boolean = dataType?.contains("float", true) ?: false
    val hasTextValue:Boolean = dataType?.contains("string", true) ?: false
    val hasNumericValue:Boolean = hasFloatValue or hasIntegerValue
}

data class SamplesPerTs (
    @SerializedName("min") val min : Int,
    @SerializedName("max") val max : Int,
    @SerializedName("dataType") val dataType : String
)