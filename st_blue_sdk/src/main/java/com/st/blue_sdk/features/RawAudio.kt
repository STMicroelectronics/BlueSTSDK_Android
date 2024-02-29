/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features

import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class RawAudio(
    val data: FeatureField<ByteArray>
) : Loggable {
    override val logHeader = data.logHeader
    override val logValue = data.logValue
    override val logDoubleValues: List<Double> = listOf()
}
