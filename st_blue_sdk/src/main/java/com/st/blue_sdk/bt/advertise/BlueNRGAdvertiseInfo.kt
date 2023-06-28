/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.advertise

import com.st.blue_sdk.models.Boards

class BlueNRGAdvertiseInfo(private val name: String) : BleAdvertiseInfo {

    override fun getName() = name

    override fun getTxPower() = 0.toByte()

    override fun getAddress(): String? = null

    override fun getFeatureMap() = 0L

    override fun getOptionBytes() = 0L

    override fun getDeviceId() = 4.toByte()

    override fun getProtocolVersion() = 1.toShort()

    override fun getBoardType() = Boards.Model.STEVAL_IDB008VX

    override fun isBoardSleeping() = false

    override fun hasGeneralPurpose() = false
}