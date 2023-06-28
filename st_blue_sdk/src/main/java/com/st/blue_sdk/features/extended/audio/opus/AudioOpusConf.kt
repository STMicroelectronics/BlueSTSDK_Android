/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.audio.opus

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusConfFeature.Companion.BV_OPUS_CONF_CMD
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusConfFeature.Companion.BV_OPUS_CONTROL
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class AudioOpusConf(
    val cmd: FeatureField<Byte?>,
    val frameSize: FeatureField<Float?>,
    val samplingFreq: FeatureField<Int?>,
    val channels: FeatureField<Int?>,
    val onOff: FeatureField<Boolean?>
) : Loggable {
    override val logHeader =
        "${cmd.logHeader}, ${frameSize.logHeader}, ${samplingFreq.logHeader}, ${channels.logHeader}, ${onOff.logHeader}"
    override val logValue =
        "${cmd.logValue}, ${frameSize.logValue}, ${samplingFreq.logValue}, ${channels.logValue}, ${onOff.logValue}"

    fun isConfCommand() = when (cmd.value) {
        BV_OPUS_CONF_CMD -> false
        BV_OPUS_CONTROL -> true
        else -> false
    }
}
