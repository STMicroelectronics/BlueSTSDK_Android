/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.robotics_movement

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.robotics_movement.request.RoboticsActionBits
import com.st.blue_sdk.features.extended.robotics_movement.request.TopologyBit
import com.st.blue_sdk.logger.Loggable

data class RoboticsMovementInfo(
    val commandId : Short,
    val action: List<RoboticsActionBits>?,
    val data : List<FeatureField<TopologyBit>>?
): Loggable {
    override val logHeader: String = ""

    override val logValue: String = ""

    override fun toString(): String = "To Be Implemented"

    override val logDoubleValues: List<Double> = listOf()

}