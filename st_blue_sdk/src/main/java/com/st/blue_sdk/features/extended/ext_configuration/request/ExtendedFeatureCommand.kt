/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.ext_configuration.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration

class ExtendedFeatureCommand(
    feature: ExtConfiguration,
    val extendedCommand: ExtConfigCommands,
    val hasResponse: Boolean = true
) :
    FeatureCommand(
        feature = feature,
        commandId = ExtConfiguration.FEATURE_SEND_EXT_COMMAND
    )