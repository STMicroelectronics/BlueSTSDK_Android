/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.piano

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class PianoInfo(
    val command: FeatureField<PianoCommand>,
    val keyNote: FeatureField<Byte?>
) : Loggable {
    override val logHeader: String =
        "${keyNote.logHeader}, ${command.logHeader}"

    override val logValue: String =
        "${keyNote.logValue}, ${command.logValue}"

    override val logDoubleValues: List<Double> = listOf()

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${command.name} = ${command.value}\n")
        if (command.value == PianoCommand.Start) {
            sampleValue.append("\t\t${keyNote.name} = ${keyNote.value}\n")
        }
        return sampleValue.toString()
    }
}

enum class PianoCommand {
    Stop,
    Start
}
