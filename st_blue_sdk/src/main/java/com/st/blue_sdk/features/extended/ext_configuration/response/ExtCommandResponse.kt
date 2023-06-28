/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.ext_configuration.response

import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfigCommandAnswers
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration.Companion.FEATURE_SEND_EXT_COMMAND

class ExtCommandResponse(feature: ExtConfiguration, val response: ExtConfigCommandAnswers) :
    FeatureResponse(feature = feature, commandId = FEATURE_SEND_EXT_COMMAND)