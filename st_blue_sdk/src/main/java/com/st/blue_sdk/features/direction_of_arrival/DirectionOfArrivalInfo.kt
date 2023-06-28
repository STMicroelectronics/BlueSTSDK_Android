/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.direction_of_arrival

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class DirectionOfArrivalInfo(
    val angle: FeatureField<Int>
) : Loggable {
    override val logHeader: String = angle.logHeader

    override val logValue: String = angle.logValue


    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${angle.name} = ${angle.value} ${angle.unit}\n")
        return sampleValue.toString()
    }
}

