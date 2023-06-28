/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.fw_version

import kotlinx.serialization.Serializable

@Serializable
abstract class FwVersion(
    var majorVersion: Int = 1,
    var minorVersion: Int = 0,
    var patchVersion: Int = 0
) {
    override fun toString(): String = "$majorVersion.$minorVersion.$patchVersion"
}