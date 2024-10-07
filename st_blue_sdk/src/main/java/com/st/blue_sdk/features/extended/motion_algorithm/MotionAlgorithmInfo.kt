/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.motion_algorithm

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable
import kotlin.experimental.and

@Serializable
data class MotionAlgorithmInfo(
    val algorithmType: FeatureField<AlgorithmType>,
    val statusType: FeatureField<Short>
) : Loggable {
    override val logHeader: String = "${algorithmType.logHeader}, ${statusType.logHeader}"

    override val logValue: String = "${algorithmType.logValue}, ${statusType.logValue}"

    override val logDoubleValues: List<Double> = when (algorithmType.value) {
        AlgorithmType.Unknown -> listOf()
        AlgorithmType.PoseEstimation, AlgorithmType.DesktopTypeDetection,
        AlgorithmType.VerticalContext -> listOf(
            AlgorithmType.getAlgorithmCode(algorithmType.value).toDouble(),
            statusType.value.toDouble()
        )
    }

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${algorithmType.name} = ${algorithmType.value}\n")
        when (algorithmType.value) {
            AlgorithmType.PoseEstimation -> sampleValue.append(
                "\t${statusType.name} = ${
                    PoseType.getPoseType(
                        statusType.value
                    )
                }\n"
            )

            AlgorithmType.DesktopTypeDetection -> sampleValue.append(
                "\t${statusType.name} = ${
                    DesktopType.getDesktopType(
                        statusType.value
                    )
                }\n"
            )

            AlgorithmType.VerticalContext -> sampleValue.append(
                "\t${statusType.name} = ${
                    VerticalContextType.getVerticalContextType(
                        statusType.value
                    )
                }\n"
            )
            //AlgorithmType.Unknown -> error
            else -> sampleValue.append("\tAlgorithm Not recognized\n")
        }
        return sampleValue.toString()
    }
}

enum class PoseType {
    Unknown,
    Sitting,
    Standing,
    LyingDown;

    companion object {
        fun getPoseType(pose: Short) = when ((pose and 0x0F).toInt()) {
            0x01 -> Sitting
            0x02 -> Standing
            0x03 -> LyingDown
            else -> Unknown
        }
    }
}

enum class AlgorithmType {
    Unknown,
    PoseEstimation,
    DesktopTypeDetection,
    VerticalContext;

    companion object {
        fun getAlgorithmType(algorithm: Short) = when ((algorithm and 0x0F).toInt()) {
            0x01 -> PoseEstimation
            0x02 -> DesktopTypeDetection
            0x03 -> VerticalContext
            else -> Unknown
        }

        fun getAlgorithmCode(algorithm: AlgorithmType) = when (algorithm) {
            PoseEstimation -> 0x01.toByte()
            DesktopTypeDetection -> 0x02.toByte()
            VerticalContext -> 0x03.toByte()
            Unknown -> 0x00.toByte()
        }

        @JvmStatic
        fun fromString(name: String): AlgorithmType = when (name) {
            "Pose Estimation" -> PoseEstimation
            "Desktop Type" -> DesktopTypeDetection
            "Vertical Context" -> VerticalContext
            "None" -> Unknown
            else -> Unknown
        }
    }

    override fun toString(): String = when (this) {
        Unknown -> "None"
        PoseEstimation -> "Pose Estimation"
        DesktopTypeDetection -> "Desktop Type"
        VerticalContext -> "Vertical Context"
    }
}

enum class VerticalContextType {
    Unknown,
    Floor,
    UpDown,
    Stairs,
    Elevator,
    Escalator;

    companion object {
        fun getVerticalContextType(vContext: Short) = when ((vContext and 0x0F).toInt()) {
            0x01 -> Floor
            0x02 -> UpDown
            0x03 -> Stairs
            0x04 -> Elevator
            0x05 -> Escalator
            else -> Unknown
        }
    }
}

enum class DesktopType {
    Unknown,
    Sitting,
    Standing;

    companion object {
        fun getDesktopType(vContext: Short) = when ((vContext and 0x0F).toInt()) {
            0x01 -> Sitting
            0x02 -> Standing
            else -> Unknown
        }
    }
}
