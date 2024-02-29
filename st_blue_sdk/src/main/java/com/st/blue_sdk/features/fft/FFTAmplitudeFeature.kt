/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.fft

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class FFTAmplitudeFeature(
    isEnabled: Boolean,
    type: Type = Type.EXTENDED,
    identifier: Int,
    name: String = NAME,
    hasTimeStamp: Boolean = false
) : Feature<FFTData>(
    isEnabled = isEnabled,
    type = type,
    identifier = identifier,
    name = name,
    hasTimeStamp = hasTimeStamp,
    isDataNotifyFeature = false
) {

    private var previousData: FFTData? = null

    companion object {
        const val NAME = "FFT Amplitude Feature"
    }

    private fun readHeader(data: ByteArray, dataOffset: Int): FFTData {

        require(data.size - dataOffset >= 7) { "There are no 7 bytes available to read" }

        val samplesCount: Int = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset)
        val samplesCountField =
            FeatureField(name = "N Sample", min = 0, max = (1 shl 16) - 1, value = samplesCount)

        val componentsCount: Short = NumberConversion.byteToUInt8(data, dataOffset + 2)
        val componentsField = FeatureField(
            name = "N Components",
            min = 0,
            max = ((1 shl 8) - 1).toShort(),
            value = componentsCount
        )

        val freqStep: Float = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 3)
        val frequencyStepField = FeatureField(
            name = "Frequency Step",
            min = 0F,
            max = Float.MAX_VALUE,
            value = freqStep
        )

        return FFTData(samplesCountField, componentsField, frequencyStepField).apply {
            appendData(data, dataOffset + 7)
        }
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<FFTData> {

        val fftData = if (previousData == null) {
            previousData = readHeader(data, dataOffset)
            previousData
        } else {
            previousData!!.appendData(data, dataOffset)
            previousData
        }

        if (fftData != null) {
            if (fftData.isComplete) {
                previousData = null
            }
        }

        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = fftData!!
        )
    }

    fun resetFeature() {
        previousData=null
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return null
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? {
        return null
    }
}