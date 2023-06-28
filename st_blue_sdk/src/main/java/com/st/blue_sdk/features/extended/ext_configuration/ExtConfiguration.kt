/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.ext_configuration

import android.util.Log
import com.st.blue_sdk.LoggableUnit
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtendedFeatureCommand
import com.st.blue_sdk.utils.STL2TransportProtocol
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ExtConfiguration(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<LoggableUnit>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    maxPayloadSize = 20,
    hasTimeStamp = false,
    isDataNotifyFeature = false
) {

    companion object {
        const val NAME = "ExtConfiguration"
        private val TAG = ExtConfiguration::class.simpleName
        const val FEATURE_SEND_EXT_COMMAND: Byte = 0x00
    }

    private val stl2TransportProtocol = STL2TransportProtocol()

    private val json = Json { ignoreUnknownKeys = true }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<LoggableUnit> {
        return FeatureUpdate(
            timeStamp = timeStamp,
            rawData = data,
            readByte = 0,
            data = LoggableUnit()
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {

        val configCommand = when (command) {
            is ExtendedFeatureCommand -> command.extendedCommand
            else -> throw IllegalAccessException()
        }

        return stl2TransportProtocol.encapsulate(json.encodeToString(configCommand))
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        val commandFrame = stl2TransportProtocol.decapsulate(data)
        commandFrame?.let {
            val text = it.toString(Charsets.UTF_8).dropLast(1)
            Log.i(TAG, text)
            val result = try {
                json.decodeFromString<ExtConfigCommandAnswers>(text)
            } catch (e: Exception) {
                Log.d(TAG, e.stackTraceToString())
                null
            }

            return result?.let { response ->
                ExtendedFeatureResponse(feature = this, response = response)
            }
        }

        return null
    }
}