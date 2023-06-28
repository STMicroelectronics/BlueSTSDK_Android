/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.fw_version

import kotlinx.serialization.Serializable
import java.util.regex.Pattern

@Serializable
class FwVersionBoard : FwVersion, Comparable<FwVersion> {

    val boardName: String
    val mcuType: String

    @Throws(IllegalArgumentException::class)
    constructor(version: String) {
        val matcher = PARSE_FW_VERSION.matcher(version)
        if (!matcher.matches()) throw IllegalArgumentException()
        mcuType = matcher.group(1)
        boardName = matcher.group(2)
        super.majorVersion = Integer.decode(matcher.group(3))
        super.minorVersion = Integer.decode(matcher.group(4))
        super.patchVersion = Integer.decode(matcher.group(5))
    }

    constructor(
        boardName: String = "",
        mcuType: String = "",
        majorVersion: Int = 1,
        minorVersion: Int = 0,
        patchVersion: Int = 0
    ) : super(majorVersion, minorVersion, patchVersion) {
        this.boardName = boardName
        this.mcuType = mcuType
    }

    companion object {
        private val PARSE_FW_VERSION = Pattern.compile("(.*)_(.*)_(\\d+)\\.(\\d+)\\.(\\d+)")
    }

    override fun compareTo(other: FwVersion): Int {
        var diff: Int = majorVersion - other.majorVersion
        if (diff != 0) return diff
        diff = minorVersion - other.minorVersion
        return if (diff != 0) diff else patchVersion - other.patchVersion
    }
}