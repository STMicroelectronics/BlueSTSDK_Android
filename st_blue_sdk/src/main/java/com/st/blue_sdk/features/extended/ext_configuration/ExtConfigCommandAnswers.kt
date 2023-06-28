/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.ext_configuration

import com.st.blue_sdk.models.Sensor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * data class for reading the Configuration Commands result
 * */
@Serializable
data class ExtConfigCommandAnswers(
    @SerialName("Commands")
    val commandList: String? = null,
    @SerialName("Info")
    val info: String? = null,
    @SerialName("Help")
    val help: String? = null,
    @SerialName("Certificate")
    val certificate: String? = null,
    @SerialName("VersionFw")
    val versionFw: String? = null,
    @SerialName("UID")
    val stm32UID: String? = null,
    @SerialName("PowerStatus")
    val powerStatus: String? = null,
    @SerialName("CustomCommands")
    val customCommandList: List<CustomCommand>? = null,
    @SerialName("sensor")
    val sensor: List<Sensor>? = null,
    @SerialName("Error")
    val error: String? = null,
    @SerialName("BankStatus")
    val banksStatus: BanksStatus? = null
)