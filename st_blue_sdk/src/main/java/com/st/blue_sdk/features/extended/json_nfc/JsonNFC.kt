package com.st.blue_sdk.features.extended.json_nfc

import android.util.Log
import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.json_nfc.answer.JsonNFCResponse
import com.st.blue_sdk.features.extended.json_nfc.answer.JsonReadModesResult
import com.st.blue_sdk.features.extended.json_nfc.request.JsonNFCFeatureWriteCommand
import com.st.blue_sdk.utils.STL2TransportProtocol
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonNFC(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<JsonNFCResponse>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = false,
    isDataNotifyFeature = false
) {
    companion object {
        const val NAME = "JsonNFC"
        private val TAG = JsonNFC::class.simpleName
        const val FEATURE_SEND_NFC_COMMAND: Byte = 0x00
    }

    private val stl2TransportProtocol = STL2TransportProtocol()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<JsonNFCResponse> {
        var response: JsonReadModesResult? = null

        val commandFrame = stl2TransportProtocol.decapsulate(data)
        commandFrame?.let {
            val text = it.toString(Charsets.UTF_8).dropLast(1)
            Log.i(TAG, text)
            response = try {
                json.decodeFromString<JsonReadModesResult>(text)
            } catch (e: Exception) {
                Log.d(TAG, e.stackTraceToString())
                null
            }
        }

        return FeatureUpdate(
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = JsonNFCResponse(
                supportedModes = FeatureField(
                    name = "SupportedModes",
                    value = response
                )
            )
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? =
        when (command) {
            is JsonNFCFeatureWriteCommand ->
                stl2TransportProtocol.encapsulate(json.encodeToString(command.nfcCommand))
            else -> null
        }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}