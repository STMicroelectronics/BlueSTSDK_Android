/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.piano.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.piano.Piano
import com.st.blue_sdk.features.extended.piano.PianoCommand

class CommandPianoSound(feature: Piano, val command: PianoCommand, val key: Byte? = null) :
    FeatureCommand(feature = feature, commandId = PIANO_COMMAND_ID) {
    companion object {
        const val PIANO_COMMAND_TYPE_STOP = 0x00.toByte()
        const val PIANO_COMMAND_TYPE_START = 0x01.toByte()
        const val PIANO_COMMAND_ID = 0x00.toByte()
    }
}