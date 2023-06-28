/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.exported

import java.util.*

class ExportedAudioOpusConfFeature(
    override val name: String = NAME,
    override val isEnabled: Boolean = false,
    override val uuid: UUID = UUID.fromString(AUDIO_OPUS_CONF_UUID)
) : ExportedFeature {
    companion object {
        const val NAME = "ExportedAudioOpusConfFeature"
        const val AUDIO_OPUS_CONF_UUID = "00000002-0002-11e1-ac36-0002a5d5c51b"

        const val ENABLE_NOTIFICATION_COMMAND = 0x10.toByte()
        const val DISABLE_NOTIFICATION_COMMAND = 0x11.toByte()
        const val NOTIFICATION_COMMAND = 0x0A.toByte()

        fun enableNotification() =
            byteArrayOf(
                NOTIFICATION_COMMAND,
                ENABLE_NOTIFICATION_COMMAND
            )

        fun disableNotification() =
            byteArrayOf(
                NOTIFICATION_COMMAND,
                DISABLE_NOTIFICATION_COMMAND
            )
    }

    override fun copy(isEnabled: Boolean): ExportedFeature =
        ExportedAudioOpusConfFeature(isEnabled = isEnabled)
}