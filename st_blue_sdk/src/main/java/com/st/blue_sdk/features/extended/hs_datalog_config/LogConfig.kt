/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.hs_datalog_config

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.hs_datalog_config.model.Device
import com.st.blue_sdk.features.extended.hs_datalog_config.model.DeviceStatus
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class LogConfig(
    val device: FeatureField<Device?>,
    val deviceStatus: FeatureField<DeviceStatus?>
) : Loggable {
    override val logHeader: String = "${device.logHeader}, ${deviceStatus.logHeader}"

    override val logValue: String = "${device.logValue}, ${deviceStatus.logValue}"
}
