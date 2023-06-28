/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.remote.humidity

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
class RemoteHumidityInfo(
    val remoteNodeId: FeatureField<Int>,
    val remoteTimestamp: FeatureField<Long>,
    val humidity: FeatureField<Float>
) : Loggable {

    override val logHeader: String =
        "${remoteNodeId.logHeader}, ${remoteTimestamp.logHeader}, ${humidity.logHeader}"

    override val logValue: String =
        "${remoteNodeId.logValue}, ${remoteTimestamp.logValue}, ${humidity.logValue}"

    override fun toString(): String {
        return "Remote node with ID: $remoteNodeId collected humidity value: $humidity"
    }
}