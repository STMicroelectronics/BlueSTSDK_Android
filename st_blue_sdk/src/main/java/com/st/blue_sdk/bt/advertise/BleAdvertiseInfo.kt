/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.advertise

import com.st.blue_sdk.models.Boards

interface BleAdvertiseInfo {

    fun getName(): String

    fun getTxPower(): Byte

    fun getAddress(): String?

    fun getFeatureMap(): Long

    fun getOptionBytes(): Long

    fun getDeviceId(): Byte

    fun getProtocolVersion(): Short

    fun getBoardType(): Boards.Model

    fun isBoardSleeping(): Boolean

    fun hasGeneralPurpose(): Boolean
}