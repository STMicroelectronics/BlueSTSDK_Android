/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.free_fall

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class FreeFallInfo(
    val freeFall: FeatureField<Short>
) : Loggable {
    override val logHeader: String = freeFall.logHeader

    override val logValue: String = freeFall.logValue

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${freeFall.name} = ${freeFall.value}\n")
        return sampleValue.toString()
    }
}
