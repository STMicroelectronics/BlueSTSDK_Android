/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

package com.st.blue_sdk.features.extended.robotics_movement.request

enum class TopologyBit {
    REMOTE_CONTROL_DIFFERENTIAL,
    REMOTE_CONTROL_STEERING,
    REMOTE_CONTROL_MECHANUM,
    REMOTE_CONTROL_RESERVED,
    ODOMETRY,
    IMU_AVAILABLE,
    ABSOLUTE_SPEED_CONTROL,
    ARM_SUPPORT,
    ATTACHMENT_SUPPORT,
    AUTO_DOCKING ,
    WIRELESS_CHARGING,
    OBSTACLE_DETECTION_FORWARD,
    OBSTACLE_DETECTION_MULTIDIRECTIONAL,
    ALARM,
    HEADLIGHTS,
    WARNING_LIGHTS,
    REMOTE_CONTROL,
    FREE_NAVIGATION,
    FOLLOW_ME,
    RFU,
}