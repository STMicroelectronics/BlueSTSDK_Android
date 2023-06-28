/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.exported

import java.util.*

interface ExportedFeature {
    val name: String
    val isEnabled: Boolean
    val uuid: UUID

    fun copy(isEnabled: Boolean): ExportedFeature
}