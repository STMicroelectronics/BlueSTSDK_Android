/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.ext_configuration.request

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@JvmInline
value class CommandName(val value: String)

@Serializable
class ExtConfigCommands private constructor(
    val command: String,
    val argString: String? = null,
    val argNumber: Int? = null,
    val argJsonElement: JsonElement? = null
) {
    companion object {

        val READ_COMMANDS = CommandName("ReadCommand")
        val READ_CERTIFICATE = CommandName("ReadCert")
        val READ_UID = CommandName("UID")
        val READ_VERSION_FW = CommandName("VersionFw")
        val READ_INFO = CommandName("Info")
        val READ_HELP = CommandName("Help")
        val READ_POWER_STATUS = CommandName("PowerStatus")
        val CHANGE_PIN = CommandName("ChangePIN")
        val CLEAR_DB = CommandName("ClearDB")
        val SET_DFU = CommandName("DFU")
        val POWER_OFF = CommandName("Off")
        val BANKS_STATUS = CommandName("ReadBanksFwId")
        val BANKS_SWAP = CommandName("BanksSwap")
        val SET_TIME = CommandName("SetTime")
        val SET_DATE = CommandName("SetDate")
        val SET_WIFI = CommandName("SetWiFi")
        val READ_SENSORS = CommandName("ReadSensorsConfig")
        val SET_SENSORS = CommandName("SetSensorsConfig")
        val SET_NAME = CommandName("SetName")
        val SET_CERTIFICATE = CommandName("SetCert")
        val READ_CUSTOM_COMMANDS = CommandName("ReadCustomCommand")

        fun buildConfigCommand(
            command: CommandName,
            argString: String? = null,
            argNumber: Int? = null,
            jsonArgs: JsonElement? = null
        ): ExtConfigCommands {
            return ExtConfigCommands(
                command.value,
                argString,
                argNumber,
                jsonArgs
            )
        }
    }
}