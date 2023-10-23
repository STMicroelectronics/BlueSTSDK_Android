package com.st.blue_sdk.board_catalog.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PowerMode(
    @SerialName(value = "minCustomOdr")
    var minCustomSampleTime: Double?=null,
    @SerialName(value = "mode")
    var mode: Mode = Mode.NONE,
    @SerialName(value = "label")
    var label: String = "",
    @SerialName(value = "odrs")
    var odrs: List<Double> = listOf()
) {
    enum class Mode(val id: Int) {
        NONE(0),
        LOW_NOISE(1),
        LOW_CURRENT(2),
        LOW_POWER(3),
        LOW_POWER_2(4),
        LOW_POWER_3(5),
        LOW_POWER_4(6),
        HIGH_PERFORMANCE(7),
        HIGH_RESOLUTION(8),
        NORMAL_MODE(9)
    }
}
