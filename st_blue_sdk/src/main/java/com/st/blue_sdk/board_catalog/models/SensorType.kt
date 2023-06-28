package com.st.blue_sdk.board_catalog.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SensorType(val value: String = "") {
    @SerialName("ACC")
    Accelerometer("acc"),

    @SerialName("MAG")
    Magnetometer("mag"),

    @SerialName("GYRO")
    Gyroscope("gyro"),

    @SerialName("TEMP")
    Temperature("temp"),

    @SerialName("HUM")
    Humidity("hum"),

    @SerialName("PRESS")
    Pressure("press"),

    @SerialName("MIC")
    Microphone("mic"),

    @SerialName("MLC")
    MLC("mlc"),

    @SerialName("CLASS")
    CLASS("class"),

    @SerialName("STREDL")
    STREDL("stredl"),

    @SerialName("UNK")
    Unknown;

    companion object {
        fun fromName(name: String) =
            values().find { it.value == name.split("_").last() } ?: Unknown
    }
}
