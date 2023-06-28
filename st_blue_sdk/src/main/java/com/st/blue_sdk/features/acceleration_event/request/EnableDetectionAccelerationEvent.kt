/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.acceleration_event.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.acceleration_event.AccelerationEvent
import com.st.blue_sdk.features.acceleration_event.AccelerationEvent.Companion.ACC_EVENT_ENABLE_COMMAND
import com.st.blue_sdk.features.acceleration_event.DetectableEventType

class EnableDetectionAccelerationEvent(
    feature: AccelerationEvent,
    val event: DetectableEventType,
    val enable: Boolean
) : FeatureCommand(feature = feature, commandId = ACC_EVENT_ENABLE_COMMAND)