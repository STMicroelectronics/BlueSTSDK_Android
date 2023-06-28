/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.acceleration_event.response

import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.acceleration_event.AccelerationEvent
import com.st.blue_sdk.features.acceleration_event.DetectableEventType

class EnableAccelerationEventResponse(feature: AccelerationEvent, event: DetectableEventType) :
    FeatureResponse(feature = feature, commandId = AccelerationEvent.ACC_EVENT_ENABLE_COMMAND)