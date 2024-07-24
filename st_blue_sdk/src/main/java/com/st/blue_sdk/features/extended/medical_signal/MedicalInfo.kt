package com.st.blue_sdk.features.extended.medical_signal

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable

data class MedicalInfo(
    val internalTimeStamp: FeatureField<Int>,
    val sigType: FeatureField<MedicalSignalType>,
    val values: FeatureField<List<Int>>,
) : Loggable {
    override val logHeader: String = "${internalTimeStamp.logHeader}, ${sigType.logHeader}"

    override val logValue: String = "${internalTimeStamp.logValue}, ${sigType.logValue}"

    override val logDoubleValues: List<Double> = listOf()

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${internalTimeStamp.name} = ${internalTimeStamp.value}\n")

        sampleValue.append("\tDescription = ${sigType.value.description}\n")

        sampleValue.append("\tNum Signals = ${sigType.value.numberOfSignals}\n")

        sampleValue.append("\tPrecision   = ${sigType.value.precision.name}\n")

        sampleValue.append("\tSamples[${values.value.size / sigType.value.numberOfSignals}]=\n")
        val splitSamples = values.value.chunked(sigType.value.numberOfSignals)
        splitSamples.forEach { sample -> sampleValue.append("\t\t${sample}\n") }

        sampleValue.append("\n")
        return sampleValue.toString()
    }
}