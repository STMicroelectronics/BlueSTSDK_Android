/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.audio_classification

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class AudioClassificationInfo(
    val classification: FeatureField<AudioClassType>,
    val algorithm: FeatureField<Short>
) : Loggable {
    override val logHeader: String = "${classification.logHeader}, ${algorithm.logHeader}"

    override val logValue: String = "${classification.logValue}, ${algorithm.logValue}"

    companion object {
        const val ALGORITHM_NOT_DEFINED: Short = 0xFF
    }

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${classification.name} = ${classification.value}\n")
        if (algorithm.value != ALGORITHM_NOT_DEFINED) {
            sampleValue.append("\t${algorithm.name} = ${algorithm.value}\n")
        }
        return sampleValue.toString()
    }
}

enum class AudioClassType {
    Unknown,
    Indoor,
    Outdoor,
    InVehicle,
    BabyIsCrying,
    AscOff,
    AscOn,
    Error
}
