/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.remote.switch

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.remote.switch.RemoteSwitch.Companion.SET_REMOTE_SWITCH_COMMAND

class ChangeRemoteSwitchStatusCommand(
    feature: Feature<*>,
    commandId: Byte = SET_REMOTE_SWITCH_COMMAND,
    val newStatus: Byte,
    val nodeId: Int
) : FeatureCommand(feature, commandId)