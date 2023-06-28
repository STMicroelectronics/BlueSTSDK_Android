/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.switchfeature.response

import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.switchfeature.SwitchFeature
import com.st.blue_sdk.features.switchfeature.SwitchStatusType

class SwitchOnOffResponse(feature: SwitchFeature, commandId: Byte, val status: SwitchStatusType) :
    FeatureResponse(feature = feature, commandId = commandId)