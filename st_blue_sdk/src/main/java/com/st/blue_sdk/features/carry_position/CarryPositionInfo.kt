/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.carry_position

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class CarryPositionInfo(
    val position: FeatureField<CarryPositionType>
) : Loggable {
    override val logHeader: String = position.logHeader

    override val logValue: String = position.logValue

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${position.name} = ${position.value}\n")
        return sampleValue.toString()
    }
}

enum class CarryPositionType {
    Unknown,
    OnDesk,
    InHand,
    NearHead,
    ShirtPocket,
    TrousersPocket,
    ArmSwing,
    Error
}
