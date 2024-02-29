/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
@file:OptIn(ExperimentalSerializationApi::class)

package com.st.blue_sdk.features.extended.hs_datalog_config

import android.util.Log
import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.hs_datalog_config.model.Device
import com.st.blue_sdk.features.extended.hs_datalog_config.model.DeviceStatus
import com.st.blue_sdk.features.extended.hs_datalog_config.model.Response
import com.st.blue_sdk.features.extended.hs_datalog_config.request.HSDataLogCommand
import com.st.blue_sdk.utils.STL2TransportProtocol
import com.st.blue_sdk.utils.logJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class HSDataLogConfig(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<LogConfig>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    isDataNotifyFeature = false
) {
    companion object {
        const val NAME = "HSDataLogConfig"
        const val TAG = "HSDataLogConfig"
        const val DEVICE_JSON_KEY = "device"
        const val DEVICE_INFO_JSON_KEY = "deviceInfo"
        const val TAG_CONFIG_JSON_KEY = "tagConfig"
    }

    private val stl2TransportProtocol = STL2TransportProtocol()

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<LogConfig> {
        var device: Device? = null
        var deviceStatus: DeviceStatus? = null

        stl2TransportProtocol.decapsulate(data)
            ?.toString(Charsets.UTF_8)
            ?.dropLast(1)
            ?.let { jsonString ->
                deviceStatus = extractDeviceStatus(jsonString)
                device = extractDevice(jsonString)
            }

        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = LogConfig(
                device = FeatureField(
                    name = "device",
                    value = device
                ),
                deviceStatus = FeatureField(
                    name = "deviceStatus",
                    value = deviceStatus
                )
            )
        )
    }

    private fun extractDeviceStatus(jsonString: String): DeviceStatus? = try {
        json.decodeFromString<DeviceStatus>(jsonString)
    } catch (ex: Exception) {
        Log.w(TAG, ex.message, ex)

        null
    }

    private fun extractDevice(jsonString: String): Device? = try {
        jsonString.logJson(tag = TAG)
        val jsonObject = json.decodeFromString<JsonObject>(jsonString)
        when {
            jsonObject.contains(DEVICE_JSON_KEY) -> json.decodeFromJsonElement<Response>(jsonObject).device
            jsonObject.contains(DEVICE_INFO_JSON_KEY) -> json.decodeFromJsonElement<Device>(
                jsonObject
            )
            jsonObject.contains(TAG_CONFIG_JSON_KEY) -> json.decodeFromJsonElement<Device>(
                jsonObject
            )
            else -> null
        }
    } catch (ex: Exception) {
        Log.w(TAG, ex.message, ex)

        null
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? =
        if (command is HSDataLogCommand) {
            val request = json.encodeToString(command.cmd)

            stl2TransportProtocol.encapsulate(request)
        } else {
            null
        }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}
