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
import kotlin.math.min

@Serializable
class FwVersionBoard : FwVersion, Comparable<FwVersion> {

    val boardName: String
    val mcuType: String

    @Throws(IllegalArgumentException::class)
    constructor(version: String) {
        val matcher = PARSE_FW_VERSION.matcher(version)
        if (!matcher.matches()) throw IllegalArgumentException()


        val type = matcher.group(1)
        val name = matcher.group(2)

        if((type!=null) && (name!= null)) {
            mcuType = type
            boardName = name
        } else {
            throw IllegalArgumentException()
        }

        val majorVersion =  matcher.group(3)
        val minorVersion =  matcher.group(4)
        val patchVersion =  matcher.group(5)

        if((majorVersion!=null) && (minorVersion!= null) && (patchVersion!= null)) {
            super.majorVersion = Integer.decode(majorVersion)
            super.minorVersion = Integer.decode(minorVersion)
            super.patchVersion = Integer.decode(patchVersion)
        } else {
            throw IllegalArgumentException()
        }
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