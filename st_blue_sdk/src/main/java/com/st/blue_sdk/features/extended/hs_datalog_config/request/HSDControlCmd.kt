/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.hs_datalog_config.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubSensorStatusParam private constructor(
    @SerialName("isActive") val isActive: Boolean? = null,
    @SerialName("ODR") val odr: Double? = null,
    @SerialName("FS") val fs: Double? = null,
    @SerialName("samplesPerTs") val samplesPerTs: Int? = null,
    @SerialName("mlcConfigSize") val mlcConfigSize: Int? = null,
    @SerialName("mlcConfigData") val mlcConfigData: String? = null,
    @SerialName("stredlConfigSize") val stredlConfigSize: Int? = null,
    @SerialName("stredlConfigData") val stredlConfigData: String? = null
) {
    companion object {
        fun buildIsActiveParam(isActive: Boolean) = SubSensorStatusParam(isActive = isActive)

        fun buildODRParam(odr: Double) = SubSensorStatusParam(odr = odr)

        fun buildFSParam(fs: Double) = SubSensorStatusParam(fs = fs)

        fun buildSamplePerTSParam(samplesPerTs: Int) =
            SubSensorStatusParam(samplesPerTs = samplesPerTs)

        fun buildMLCConfigParam(mlcConfigSize: Int, mlcConfigData: String) =
            SubSensorStatusParam(mlcConfigSize = mlcConfigSize, mlcConfigData = mlcConfigData)

        private fun isCommentLine(line: String): Boolean {
            return !line.startsWith("--")
        }

        fun buildMLCConfigParam(ucfContent: String): SubSensorStatusParam {
            val isSpace = "\\s+".toRegex()
            val compactString = ucfContent.lineSequence()
                .filter { isCommentLine(it) }
                .map { it.replace(isSpace, "").drop(2) }
                .joinToString("")
            return buildMLCConfigParam(compactString.length, compactString)
        }

        fun buildSTREDLConfigParam(stredlConfigSize: Int, stredlConfigData: String) =
            SubSensorStatusParam(
                stredlConfigSize = stredlConfigSize,
                stredlConfigData = stredlConfigData
            )

        fun buildSTREDLConfigParam(ucfContent: String): SubSensorStatusParam {
            val isSpace = "\\s+".toRegex()
            val isAc = "Ac".toRegex()
            val isWAIT = "WAIT[0-9]+".toRegex()
            val compactString = ucfContent.lineSequence()
                .filter { isCommentLine(it) }
                .map { it.replace(isSpace, "") }
                .map { it.replace(isAc, "") }
                .map { it.replace(isWAIT, "W" + it.drop(4) + "W") }
                .joinToString("")
            return buildSTREDLConfigParam(compactString.length, compactString)
        }
    }
}

@ConsistentCopyVisibility
@Serializable
data class HSDCmd private constructor(
    @SerialName("command")
    val command: String,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("end_time")
    val endTime: String? = null,
    @SerialName("request")
    val request: String? = null,
    @SerialName("alias")
    val alias: String? = null,
    @SerialName("ssid")
    val ssid: String? = null,
    @SerialName("password")
    val password: String? = null,
    @SerialName("enable")
    val enable: Boolean? = null,
    @SerialName("ID")
    val id: Int? = null,
    @SerialName("sensorId")
    val sensorId: Int? = null,
    @SerialName("subSensorStatus")
    val subSensorStatus: List<SubSensorStatusParam>? = null,
    @SerialName("isActive")
    val isActive: Boolean? = null,
    @SerialName("ODR")
    val odr: Double? = null,
    @SerialName("FS")
    val fs: Double? = null,
    @SerialName("samplesPerTs")
    val samplesPerTs: Int? = null,
    @SerialName("label")
    val label: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("notes")
    val notes: String? = null
) {

    companion object {
        fun buildHSDControlCmdStartLogging(startTime: String?) = HSDCmd(
            command = "START",
            startTime = startTime
        )

        fun buildHSDControlCmdStopLogging(endTime: String?) = HSDCmd(
            command = "STOP",
            endTime = endTime
        )

        fun buildHSDControlCmdSave() = HSDCmd(command = "SAVE")

        fun buildHSDGetCmdDevice() = HSDCmd(
            command = "GET",
            request = "device"
        )

        fun buildHSDGetCmdDeviceInfo() = HSDCmd(
            command = "GET",
            request = "deviceInfo"
        )

        fun buildHSDGetCmdDescriptor() = HSDCmd(
            command = "GET",
            request = "descriptor"
        )

        fun buildHSDGetCmdStatus() = HSDCmd(
            command = "GET",
            request = "status"
        )

        fun buildHSDGetCmdNetworkInfo() = HSDCmd(
            command = "GET",
            request = "network"
        )

        fun buildHSDGetCmdTagConfig() = HSDCmd(
            command = "GET",
            request = "tag_config"
        )

        fun buildHSDGetCmdLogStatus() = HSDCmd(
            command = "GET",
            request = "log_status"
        )

        fun buildHSDGetCmdRegister() = HSDCmd(
            command = "GET",
            request = "register"
        )

        fun buildHSDSetCmdDevice(request: String) = HSDCmd(
            command = "SET",
            request = request
        )

        fun buildHSDSetCmdDeviceAlias(alias: String) = HSDCmd(
            command = "SET",
            request = "deviceInfo",
            alias = alias
        )

        fun buildHSDSetCmdWifi(ssid: String, password: String, enable: Boolean) = HSDCmd(
            command = "SET",
            request = "network",
            ssid = ssid,
            password = password,
            enable = enable
        )

        fun buildHSDSetCmdSwTag(id: Int, enable: Boolean) = HSDCmd(
            command = "SET",
            request = "sw_tag",
            id = id,
            enable = enable
        )

        fun buildHSDSetCmdSwTagLabel(id: Int, label: String) = HSDCmd(
            command = "SET",
            request = "sw_tag_label",
            id = id,
            label = label
        )

        fun buildHSDSetCmdHwTag(id: Int, enable: Boolean) = HSDCmd(
            command = "SET",
            request = "hw_tag",
            id = id,
            enable = enable
        )

        fun buildHSDSetCmdHwTagLabel(id: Int, label: String) = HSDCmd(
            command = "SET",
            request = "hw_tag",
            id = id,
            label = label
        )

        fun buildHSDSetCmdAcquisitionInfo(name: String, notes: String) = HSDCmd(
            command = "SET",
            request = "acq_info",
            name = name,
            notes = notes
        )

        fun buildHSDSetCmdSensor(sensorId: Int, subSensorStatus: List<SubSensorStatusParam>) =
            HSDCmd(
                command = "SET",
                request = null,
                sensorId = sensorId,
                subSensorStatus = subSensorStatus
            )

        fun buildHSDSetCmdMLCSensor(sensorId: Int, subSensorStatus: List<SubSensorStatusParam>) =
            HSDCmd(
                command = "SET",
                request = "mlc_config",
                sensorId = sensorId,
                subSensorStatus = subSensorStatus
            )

        fun buildHSDSetCmdSTREDLSensor(sensorId: Int, subSensorStatus: List<SubSensorStatusParam>) =
            HSDCmd(
                command = "SET",
                request = "stredl_config",
                sensorId = sensorId,
                subSensorStatus = subSensorStatus
            )
    }
}