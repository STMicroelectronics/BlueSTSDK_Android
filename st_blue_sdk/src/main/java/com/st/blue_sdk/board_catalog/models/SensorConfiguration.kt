package com.st.blue_sdk.board_catalog.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorConfiguration(
    @SerialName(value = "odr")
    var odr: Double? = null,
    @SerialName(value = "oneShotTime")
    var oneShotTime: Double? = null,
    @SerialName(value = "powerMode")
    var powerMode: PowerMode.Mode? = null,
    @SerialName(value = "acquisitionTime")
    var acquisitionTime: Double? = null,
    @SerialName(value = "regConfig")
    var regConfig: String? = null,
    @SerialName(value = "ucfFilename")
    var ucfFilename: String? = null,
    @SerialName(value = "mlcLabels")
    var mlcLabels: String? = null,
    @SerialName(value = "fsmLabels")
    var fsmLabels: String? = null,
    @SerialName(value = "filters")
    var filters: FilterConfiguration?=null,
    @SerialName(value = "fullScale")
    var fullScale: Int? = null
) {
    @kotlinx.serialization.Transient
    var lowPassCutoffs: List<CutOff>?=null

    @kotlinx.serialization.Transient
    var highPassCutoffs: List<CutOff>?=null

}

var SensorConfiguration.acquisitionTimeMin:Double?
    get() { return this.acquisitionTime?.div(60) }
    set(value) { this.acquisitionTime = value?.times(60) }
