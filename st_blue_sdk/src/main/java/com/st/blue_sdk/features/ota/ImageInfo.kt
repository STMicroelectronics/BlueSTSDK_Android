/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.ota

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class ImageInfo(
    val flashLB: FeatureField<Long>,
    val flashUB: FeatureField<Long>,
    val protocolVersionMajor: FeatureField<Int>,
    val protocolVersionMinor: FeatureField<Int>
) : Loggable {
    override val logHeader: String =
        "${flashLB.logHeader}, ${flashUB.logHeader}, ${protocolVersionMajor.logHeader}, ${protocolVersionMinor.logHeader}"

    override val logValue: String =
        "${flashLB.logValue}, ${flashUB.logValue}, ${protocolVersionMajor.logValue}, ${protocolVersionMinor.logValue}"

}