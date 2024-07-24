package com.st.blue_sdk.features.extended.medical_signal

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.utils.NumberConversion

class MedicalSignal16BitFeature(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<MedicalInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = false
) {
    companion object {
        const val NAME = "Medical Signal 16"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<MedicalInfo> {
        val internalTimeStamp = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset)
        val signalType = data[dataOffset + 4].MedicalSignalType()

        require((signalType.precision == MedicalPrecision.BIT16) || (signalType.precision == MedicalPrecision.UBIT16)) { "${signalType.precision.name} Signal Precision not supported by Medical Signal 16 Bits Feature" }

        val values = mutableListOf<Int>()

        val numberOfSamples = (data.size - dataOffset - 5) / 2

        if(signalType.precision == MedicalPrecision.UBIT16) {
            for (i in 0 until numberOfSamples) {
                values.add(
                    NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset + 5 + i * 2).toInt()
                )
            }
        } else {
            for (i in 0 until numberOfSamples) {
                values.add(
                    NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 5 + i * 2).toInt()
                )
            }
        }

        val medicalInfo = MedicalInfo(
            internalTimeStamp = FeatureField(name = "Internal TimeStamp", value = internalTimeStamp),
            sigType = FeatureField(name = "Signal Type", value = signalType),
            values = FeatureField(name = "samples", value = values.toList())
        )

        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = medicalInfo
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}