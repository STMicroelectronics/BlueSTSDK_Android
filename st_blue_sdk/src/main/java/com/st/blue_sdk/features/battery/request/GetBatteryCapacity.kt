/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.battery.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.battery.Battery
import com.st.blue_sdk.features.battery.Battery.Companion.COMMAND_GET_BATTERY_CAPACITY

class GetBatteryCapacity(feature: Battery) :
    FeatureCommand(feature = feature, commandId = COMMAND_GET_BATTERY_CAPACITY)