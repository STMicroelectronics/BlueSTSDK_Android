package com.st.blue_sdk.features.extended.binary_content

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.STL2TransportProtocol

class BinaryContent(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int,
    maxPayloadSize: Int
) : Feature<RawData>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    maxPayloadSize = maxPayloadSize,
    hasTimeStamp = false,
    isDataNotifyFeature = false
) {
    companion object {
        const val NAME = "Binary Content"
        private val TAG = BinaryContent::class.simpleName
        const val FEATURE_SEND_BINARY_CONTENT_COMMAND: Byte = 0x00
    }

    private val stl2TransportProtocol = STL2TransportProtocol(maxPayloadSize = maxPayloadSize)

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<RawData> {
        val binaryContent = stl2TransportProtocol.decapsulate(data)

        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = RawData(
                data = FeatureField(
                    name = "BinaryContent",
                    value = binaryContent
                ),
                bytesRec = FeatureField(
                    name = "bytesRec",
                    value = stl2TransportProtocol.getBytesReceived()
                ),
                numberPackets =  FeatureField(
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

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? =
        when (command) {
            is BinaryContentCommand ->
                stl2TransportProtocol.encapsulate(command.data)
            else -> null
        }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}