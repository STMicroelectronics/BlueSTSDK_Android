package com.st.blue_sdk.features.extended.ispu_control

import android.util.Log
import com.st.blue_sdk.NodeService
import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.STL2TransportProtocol
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class ISPUControlFeature(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int,
    maxPayloadSize: Int
) : Feature<ISPURawData>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    maxPayloadSize = maxPayloadSize,
    hasTimeStamp = false,
    isDataNotifyFeature = false
) {
    companion object {
        const val NAME = "ISPU Control"
        private val TAG = ISPUControlFeature::class.simpleName
        const val FEATURE_SEND_ISPU_CONTROL_UCF_COMMAND: Byte = 0x00
        const val FEATURE_SEND_ISPU_CONTROL_JSON_COMMAND: Byte = 0x01
        const val FEATURE_ISPU_CONTROL_UCF_VERSION: Byte = 0x01 //Version 1
        const val ESCAPE_REG_NUM_STRING: String = "FF"
        const val ESCAPE_REG_NUM: Short = 0xFF.toShort()
    }

    private val stl2TransportProtocol = STL2TransportProtocol(maxPayloadSize = maxPayloadSize)

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<ISPURawData> {
        val ispuControlContent = stl2TransportProtocol.decapsulate(data)

        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = ISPURawData(
                data = FeatureField(
                    name = "ISPUControl",
                    value = ispuControlContent
                ),
                bytesRec = FeatureField(
                    name = "bytesRec",
                    value = stl2TransportProtocol.getBytesReceived()
                ),
                numberPackets = FeatureField(
                    name = "numberPackets",
                    value = stl2TransportProtocol.getNumberPackets()
                )
            )
        )
    }

    fun setMaxPayLoadSize(payLoadSize: Int) {
        maxPayloadSize = payLoadSize
        stl2TransportProtocol.setMaxPayLoadSize(payLoadSize)
    }

    fun getMaxPayLoadSize() = maxPayloadSize

    //Convert UCF file in couple of bytes
    fun minifyUCFFile(inStream: InputStream): ByteArray? {
        val myReader = BufferedReader(InputStreamReader(inStream))
        var myDataRow: String?

        var resultString = ""
        var result: ByteArray? = null
        try {
            while (myReader.readLine().also { myDataRow = it } != null) {
                if (myDataRow!!.contains("Ac")) {
                    val data = myDataRow!!.removePrefix("Ac ").split("".toRegex())
                    if (data.size != 2) {
                        result = null
                        throw IllegalArgumentException("one Ac line need 2 Values: Reg + Value")
                    }
                    resultString += data[0] + data[1]
                } else if (myDataRow!!.contains("WAIT")) {
                    val data = myDataRow!!.removePrefix("WAIT ").split("".toRegex())
                    if (data.size != 1) {
                        result = null
                        throw IllegalArgumentException("one WAIT line need 1 Value: Millisecond")
                    }
                    resultString += ESCAPE_REG_NUM_STRING + data[0]
                }
            }
            result = resultString.decodeHex()
        } catch (ex: Exception) {
            Log.e(TAG, ex.message, ex)
        }

        return result
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    //Remove not necessary chars from json
    fun minifyJsonFile(inStream: InputStream): String? {
        val text = inStream.bufferedReader(StandardCharsets.ISO_8859_1).readText()
        var result: String? = null
        try {
            val jsonObj = Json.decodeFromString<Any>(text)
            result = jsonObj.toString()
        } catch (ex: Exception) {
            Log.e(TAG, ex.message, ex)
        }
        return result
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? =
        when (command) {
            is ISPUControlSendUCFCommand ->
                stl2TransportProtocol.encapsulate(
                    byteArrayOf(FEATURE_SEND_ISPU_CONTROL_UCF_COMMAND) + byteArrayOf(
                        FEATURE_ISPU_CONTROL_UCF_VERSION
                    ) + command.data
                )

            is ISPUControlSendJSONCommand ->
                stl2TransportProtocol.encapsulate(
                    byteArrayOf(FEATURE_SEND_ISPU_CONTROL_JSON_COMMAND)
                            + command.data
                )

            else -> null
        }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}