/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.tof_multi_object.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObject

class CommandPresenceRecognition(feature: ToFMultiObject, val enable: Boolean) :
    FeatureCommand(feature = feature, commandId = TOF_PRESENCE_COMMAND_ID) {
    companion object {
        const val TOF_PRESENCE_DISABLE = 0x00.toByte()
        const val TOF_PRESENCE_ENABLE = 0x01.toByte()
        const val TOF_PRESENCE_COMMAND_ID = 0x00.toByte()
    }
}