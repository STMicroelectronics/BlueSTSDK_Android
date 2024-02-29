/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features

import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class FeatureField<T>(
    val name: String,
    val max: T? = null,
    val min: T? = null,
    val value: T,
    val unit: String? = null
) : Loggable {

    override val logHeader: String = if (unit.isNullOrEmpty()) {
        name
    } else {
        "$name ($unit)"
    }

    override val logValue: String = "$value"

    override val logDoubleValues: List<Double> = listOf()
}
