package com.st.blue_sdk.features.extended.scene_description

import android.util.Log
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.extended.scene_description.model.SceneDescriptorData
import com.st.blue_sdk.utils.STL2TransportProtocol
import kotlinx.serialization.json.Json

class SceneDescription(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<SceneDescriptionInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Scene Description"
        const val NUMBER_BYTES = 2
        const val TAG = "SceneSDK"
        private const val TOF_DATA : Byte = 0x02

        fun getDataTypeIdentifier(commandCode: Short) = when (commandCode) {

            0x02.toShort() -> TOF_DATA

            else -> throw IllegalArgumentException("Unknown command type: $commandCode")
        }
    }

    private val stl2TransportProtocol = STL2TransportProtocol(maxPayloadSize = maxPayloadSize)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun ByteArray.toHexString(): String {
        return joinToString(separator = " ") { byte -> "%02x".format(byte) }
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<SceneDescriptionInfo> {

        var tofValues : SceneDescriptorData? = null
        val byteList = data.joinToString(", ", "[", "]") { it.toString() }
        //Log.d(TAG, "Data: $byteList")

        val commandFrame = stl2TransportProtocol.decapsulate(data)

        commandFrame?.let {
            val text = it.toString(Charsets.UTF_8)
            //Log.d(TAG,"text: $text")


            tofValues = try {
                json.decodeFromString<SceneDescriptorData>(text)
            } catch (e: Exception) {
                Log.e(TAG, "Error decoding SceneDescriptorData: ${e.message}")
                null
            }
        }

        //Log.d(TAG,"CommandFrame : $commandFrame")
        //Log.d(TAG,"tofValues : $tofValues")

        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = SceneDescriptionInfo(
                payload = FeatureField(
                    name = "tof_zones",
                    value = tofValues
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command.commandId) {
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        // TODO: Implement parsing for responses if needed
        val commandFrame = stl2TransportProtocol.decapsulate(data)
        commandFrame?.let {
            val text = it.toString(Charsets.UTF_8)
            Log.d(TAG, "Command Response: $text")
        }
        return null
    }
}