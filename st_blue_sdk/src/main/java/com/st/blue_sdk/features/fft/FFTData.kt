/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.fft

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import com.st.blue_sdk.utils.NumberConversion
import kotlinx.serialization.Serializable
import kotlin.math.min

@Serializable
class FFTData(
    val sampleCount: FeatureField<Int>,
    val components: FeatureField<Short>,
    val freqStep: FeatureField<Float>
) : Loggable {

    override val logHeader: String =
        "${sampleCount.logHeader}, ${components.logHeader}, ${freqStep.logHeader}"

    override val logValue: String =
        "${sampleCount.logValue}, ${components.logValue}, ${freqStep.logValue}"

    private var rawData =
        ByteArray(size = sampleCount.value * components.value * 4) // components * 4 byte each float

    private var dataCount = 0

    var isComplete = false
        get() = dataCount == rawData.size
        private set

    fun appendData(data: ByteArray, offset: Int) {
        val spaceAvailable: Int = rawData.size - dataCount
        val dataAvailable = data.size - offset
        val dataToCopy = min(spaceAvailable, dataAvailable)
        System.arraycopy(data, offset, rawData, dataCount, dataToCopy)
        dataCount += dataToCopy
    }

    fun getDataLoadPercentage(): Int {
        return if (rawData.isEmpty()) 0 else (dataCount * 100 / rawData.size)
    }

    fun getComponent(index: Int): FloatArray {

        require(index < components.value) { "Max component is ${components.value}" }

        val startOffset: Int = index * sampleCount.value * 4

        val out = FloatArray(sampleCount.value)
        for (i in 0 until sampleCount.value) {
            out[i] = NumberConversion.LittleEndian.bytesToFloat(rawData, startOffset + 4 * i)
        }

        return out
    }

    fun getComponents(): List<FloatArray> {
        val componentOut = mutableListOf<FloatArray>()
        for (i in 0 until components.value) {
            componentOut.add(getComponent(i))
        }

        return componentOut.toList()
    }

}