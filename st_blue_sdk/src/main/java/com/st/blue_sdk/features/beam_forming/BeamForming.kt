/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.beam_forming

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.beam_forming.request.ChangeBeamFormingDirection
import com.st.blue_sdk.features.beam_forming.request.EnableDisableBeamForming
import com.st.blue_sdk.features.beam_forming.request.UseStrongBeamFormingAlgorithm
import com.st.blue_sdk.utils.NumberConversion

class BeamForming(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<BeamFormingInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Beam Forming"
        const val BF_COMMAND_TYPE_ONOFF = 0xAA.toByte()
        const val BF_COMMAND_TYPE_CHANGEDIR = 0xBB.toByte()
        const val BF_COMMAND_TYPE_CHANGE_TYPE = 0xCC.toByte()
        val COMMAND_ASR_READY_BF = byteArrayOf(0x00)
        val COMMAND_STRONG_BF = byteArrayOf(0x01)

        fun getBeamDirectionType(beamForming: Short) = when (beamForming.toInt()) {
            0x1 -> BeamDirectionType.Top
            0x2 -> BeamDirectionType.TopRight
            0x3 -> BeamDirectionType.Right
            0x4 -> BeamDirectionType.BottomRight
            0x5 -> BeamDirectionType.Bottom
            0x6 -> BeamDirectionType.BottomLeft
            0x7 -> BeamDirectionType.Left
            0x8 -> BeamDirectionType.TopLeft
            else -> BeamDirectionType.Unknown
        }

        fun getBeamDirectionCode(direction: BeamDirectionType) = when (direction) {
            BeamDirectionType.Top -> 0x1
            BeamDirectionType.TopRight -> 0x2
            BeamDirectionType.Right -> 0x3
            BeamDirectionType.BottomRight -> 0x4
            BeamDirectionType.Bottom -> 0x5
            BeamDirectionType.BottomLeft -> 0x6
            BeamDirectionType.Left -> 0x7
            BeamDirectionType.TopLeft -> 0x8
            else -> 0xFF.toByte()
        }
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<BeamFormingInfo> {
        require(data.size - dataOffset > 0) { "There are no bytes available to read for $name feature" }

        val beamDirection = BeamFormingInfo(
            beamDirection = FeatureField(
                name = "BeamForming",
                value = getBeamDirectionType(NumberConversion.byteToUInt8(data, dataOffset))
            )
        )
        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp, rawData = data, readByte = 1, data = beamDirection
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        when (command) {
            is ChangeBeamFormingDirection -> {
                val argument = command.direction
                return packCommandRequest(
                    featureBit,
                    BF_COMMAND_TYPE_CHANGEDIR,
                    byteArrayOf(getBeamDirectionCode(argument))
                )
            }
            is EnableDisableBeamForming -> {
                val argument = command.enable
                val enableBeam = if (argument)
                    byteArrayOf(0x1)
                else
                    byteArrayOf(0x0)
                return packCommandRequest(
                    featureBit,
                    BF_COMMAND_TYPE_ONOFF,
                    enableBeam
                )
            }
            is UseStrongBeamFormingAlgorithm -> {
                return if (command.enable)
                    packCommandRequest(
                        featureBit,
                        BF_COMMAND_TYPE_CHANGE_TYPE,
                        COMMAND_STRONG_BF
                    )
                else
                    packCommandRequest(
                        featureBit,
                        BF_COMMAND_TYPE_CHANGE_TYPE,
                        COMMAND_ASR_READY_BF
                    )
            }
            else -> return null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}