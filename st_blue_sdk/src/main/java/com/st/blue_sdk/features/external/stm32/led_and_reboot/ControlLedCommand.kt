/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.external.stm32.led_and_reboot

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand

class ControlLedCommand(
    feature: Feature<*>,
    commandId: Byte,
    val deviceId: Byte,
    val turnOn: Boolean
) : FeatureCommand(feature, commandId)