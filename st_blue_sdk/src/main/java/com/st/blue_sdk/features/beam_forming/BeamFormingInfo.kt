/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.beam_forming

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.beam_forming.BeamForming.Companion.getBeamDirectionCode
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
class BeamFormingInfo(
    val beamDirection: FeatureField<BeamDirectionType>
) : Loggable {
    override val logHeader: String = beamDirection.logHeader

    override val logValue: String = beamDirection.logValue
    override val logDoubleValues: List<Double> =
        listOf(getBeamDirectionCode(beamDirection.value).toDouble())

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${beamDirection.name} = ${beamDirection.value}\n")
        return sampleValue.toString()
    }
}

enum class BeamDirectionType {
    Unknown,
    Top,
    TopRight,
    Right,
    BottomRight,
    Bottom,
    BottomLeft,
    Left,
    TopLeft
}