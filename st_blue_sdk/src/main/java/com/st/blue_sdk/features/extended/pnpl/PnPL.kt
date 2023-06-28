/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
@file:OptIn(ExperimentalSerializationApi::class)

package com.st.blue_sdk.features.extended.pnpl

import android.util.Log
import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.pnpl.model.PnPLDevice
import com.st.blue_sdk.features.extended.pnpl.model.PnPLResponse
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.blue_sdk.utils.STL2TransportProtocol
import com.st.blue_sdk.utils.logJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*

class PnPL(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<PnPLConfig>(
    name = name,
    type = type,
    maxPayloadSize = 20,
    isEnabled = isEnabled,
    identifier = identifier,
    isDataNotifyFeature = false
) {
    companion object {
        const val NAME = "PnPL"
        const val TAG = "PnPL"
        const val DEVICES_JSON_KEY = "devices"
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
    ): FeatureUpdate<PnPLConfig> {
        var deviceStatus: PnPLDevice? = null

        stl2TransportProtocol.decapsulate(data)
            ?.toString(Charsets.UTF_8)
            ?.dropLast(1)
            ?.let { jsonString ->
                jsonString.logJson(tag = TAG)

                deviceStatus = extractDeviceStatus(jsonString)
            }

        return FeatureUpdate(
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = PnPLConfig(
                deviceStatus = FeatureField(
                    name = "DeviceStatus",
                    value = deviceStatus
                )
            )
        )
    }

    private fun extractDeviceStatus(jsonString: String): PnPLDevice? = try {
        val jsonObject = json.decodeFromString<JsonObject>(jsonString)
        if (jsonObject.containsKey(DEVICES_JSON_KEY)) {
            json.decodeFromJsonElement<PnPLResponse>(jsonObject).devices.firstOrNull()
        } else {
            PnPLDevice(
                boardId = null,
                fwId = null,
                serialNumber = null,
                components = listOf(jsonObject)
            )
        }
    } catch (ex: Exception) {
        Log.w(TAG, ex.message, ex)

        null
    }

    private fun extractComponentStatus(jsonString: String): JsonObject? = try {
        json.decodeFromString<JsonObject>(jsonString)
    } catch (ex: Exception) {
        Log.w(TAG, ex.message, ex)

        null
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? =
        when (command) {
            is PnPLCommand ->
                stl2TransportProtocol.encapsulate(command.cmd.jsonString)
            else -> null
        }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}
