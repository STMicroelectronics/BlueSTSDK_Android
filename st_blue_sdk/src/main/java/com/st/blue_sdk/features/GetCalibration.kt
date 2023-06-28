/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

package com.st.blue_sdk.features

class GetCalibration(feature: Feature<*>) :
    FeatureCommand(feature = feature, commandId = FEATURE_COMMAND_GET_CONFIGURATION_STATUS) {
}

const val FEATURE_COMMAND_GET_CONFIGURATION_STATUS: Byte = 0xFF.toByte()