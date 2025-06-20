/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.robotics_movement.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.robotics_movement.RoboticsMovement

class SetNavigationMode(
    feature : RoboticsMovement,
    val action : List<RoboticsActionBits>,
    val navigationMode: UByte,
    val armed: UByte,
    val res: Long
) : FeatureCommand(feature = feature , commandId = RoboticsMovement.SET_NAVIGATION_MODE)