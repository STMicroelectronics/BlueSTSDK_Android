/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.logger

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.models.Node

interface Logger {

    var isEnabled: Boolean

    val id: String

    fun log(node: Node, feature: Feature<*>, update: FeatureUpdate<*>): Boolean

    fun clear()
}
