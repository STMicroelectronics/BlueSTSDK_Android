/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.audio.adpcm

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class AudioADPCMSync(
    val index: FeatureField<Short>,
    val predSample: FeatureField<Int>
) : Loggable {
    override val logHeader = "${index.logHeader}, ${predSample.logHeader}"
    override val logValue = "${index.logValue}, ${predSample.logValue}"
}
