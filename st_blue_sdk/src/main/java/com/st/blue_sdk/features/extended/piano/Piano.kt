/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.piano

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.piano.request.CommandPianoSound

class Piano(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<PianoInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    isDataNotifyFeature = false
) {

    companion object {
        const val NAME = "Simple Piano"
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<PianoInfo> {

        val numberBytes: Int

        val piano =
            if ((data.size - dataOffset) == 1) {
                numberBytes = 1
                PianoInfo(
                    command = FeatureField(
                        name = "command",
                        value = getCommandType(data[dataOffset])
                    ),
                    keyNote = FeatureField(
                        name = "key value",
                        value = null
                    )
                )
            } else {
                numberBytes = 2
                PianoInfo(
                    command = FeatureField(
                        name = "command",
                        value = getCommandType(data[dataOffset])
                    ),
                    keyNote = FeatureField(
                        name = "key value",
                        value = data[dataOffset + 1]
                    )
                )
            }

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = numberBytes,
            data = piano
        )
    }

    private fun getCommandType(value: Byte): PianoCommand =
        if (value == 0.toByte())
            PianoCommand.Stop
        else
            PianoCommand.Start

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is CommandPianoSound -> {
                if (command.command == PianoCommand.Stop) {
                    packCommandRequest(
                        featureBit,
                        CommandPianoSound.PIANO_COMMAND_TYPE_STOP,
                        byteArrayOf(0) //value not important
                    )
                } else {
                    command.key?.let {
                        packCommandRequest(
                            featureBit,
                            CommandPianoSound.PIANO_COMMAND_TYPE_START,
                            byteArrayOf(command.key.toByte())
                        )
                    }
                }
            }
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}