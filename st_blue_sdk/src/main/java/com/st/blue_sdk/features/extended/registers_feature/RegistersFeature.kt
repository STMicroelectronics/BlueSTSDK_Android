/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.registers_feature

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class RegistersFeature(
    name: String,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int,
    private val regName: String
) : Feature<RegistersFeatureInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
) {

    companion object {
        const val ML_CORE_NAME = "Machine Learning Core"
        const val FSM_NAME = "Finite State Machine"
        const val STRED_NAME = "STRed-ISPU"
        const val DATA_MAX: Short = 255
        const val DATA_MIN: Short = 0
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<RegistersFeatureInfo> {
        require(data.size - dataOffset > 0) { "There are no bytes available to read for $name feature" }

        val numberOfBytes = data.size - dataOffset
        val numberOfStatusPages = when (numberOfBytes) {
            in 1..9 -> 1
            in 10..18 -> 2
            else -> throw IllegalArgumentException("Unsupported Number of Registers")
        }

        val numberOfRegisters = numberOfBytes - numberOfStatusPages

        val registers = mutableListOf<FeatureField<Short>>()

        for (regNum in 0 until numberOfRegisters) {
            registers.add(
                FeatureField(
                    max = DATA_MAX,
                    min = DATA_MIN,
                    name = "${regName}_${regNum}",
                    value = NumberConversion.byteToUInt8(data, dataOffset + regNum)
                )
            )
        }

        val status = mutableListOf<FeatureField<Short>>()
        //Fill the Status Pages
        for (statusPageNum in 0 until numberOfStatusPages) {
            status.add(
                FeatureField(
                    max = DATA_MAX,
                    min = DATA_MIN,
                    name = "Status_${statusPageNum}",
                    value = NumberConversion.byteToUInt8(
                        data,
                        dataOffset + numberOfRegisters + statusPageNum
                    )
                )
            )
        }

        val mlcInfo = RegistersFeatureInfo(
            registers = registers,
            statusPages = status
        )

        return FeatureUpdate(
            timeStamp = timeStamp, rawData = data, readByte = numberOfBytes, data = mlcInfo
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}