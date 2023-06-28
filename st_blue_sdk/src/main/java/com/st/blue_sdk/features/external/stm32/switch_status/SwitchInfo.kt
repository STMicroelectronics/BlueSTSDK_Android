/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.external.stm32.switch_status

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
class SwitchInfo(
    val deviceId: FeatureField<Byte>,
    val isSwitchPressed: FeatureField<Boolean>
) : Loggable {

    override val logHeader: String = "${deviceId.logHeader}, ${isSwitchPressed.logHeader}"

    override val logValue: String = "${deviceId.logValue}, ${isSwitchPressed.logValue}"

    override fun toString(): String {
        return "Device Id is: ${deviceId.value}, " +
                "switchStatus is: ${if (isSwitchPressed.value) "PRESSED" else "NOT PRESSED"}"
    }
}