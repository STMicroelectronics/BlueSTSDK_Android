/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.motion_algorithm

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.motion_algorithm.request.EnableMotionAlgorithm
import com.st.blue_sdk.utils.NumberConversion

class MotionAlgorithm(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<MotionAlgorithmInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Motion Algorithm"
        const val NUMBER_BYTES = 2
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<MotionAlgorithmInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }
        val algorithmType =
            AlgorithmType.getAlgorithmType(NumberConversion.byteToUInt8(data, dataOffset))
        val motionAlgorithm =
            when (algorithmType) {
                AlgorithmType.PoseEstimation -> MotionAlgorithmInfo(
                    algorithmType = FeatureField(
                        value = algorithmType,
                        name = "algorithm Type"
                    ),
                    statusType = FeatureField(
                        value = NumberConversion.byteToUInt8(data, dataOffset + 1),
                        name = "Pose"
                    )
                )
                AlgorithmType.DesktopTypeDetection -> MotionAlgorithmInfo(
                    algorithmType = FeatureField(
                        value = algorithmType,
                        name = "algorithm Type"
                    ),
                    statusType = FeatureField(
                        value = NumberConversion.byteToUInt8(data, dataOffset + 1),
                        name = "desktop position"
                    )
                )
                AlgorithmType.VerticalContext -> MotionAlgorithmInfo(
                    algorithmType = FeatureField(
                        value = algorithmType,
                        name = "algorithm Type"
                    ),
                    statusType = FeatureField(
                        value = NumberConversion.byteToUInt8(data, dataOffset + 1),
                        name = "vertical Context"
                    )
                )
                //AlgorithmType.Unknown -> error
                else -> MotionAlgorithmInfo(
                    algorithmType = FeatureField(
                        value = algorithmType,
                        name = "algorithm type"
                    ),
                    statusType = FeatureField(
                        value = NumberConversion.byteToUInt8(data, dataOffset + 1),
                        name = "Unknown"
                    )
                )
            }

        return FeatureUpdate(
            timeStamp = timeStamp, rawData = data, readByte = NUMBER_BYTES, data = motionAlgorithm
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is EnableMotionAlgorithm -> {
                byteArrayOf(AlgorithmType.getAlgorithmCode(command.algorithmType))
            }
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}