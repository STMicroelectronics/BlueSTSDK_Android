/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.advertise

import com.st.blue_sdk.models.Boards

data class BlueStSdkAdvertiseInfo(
    private val name: String,
    private val txPower: Byte,
    private val address: String?,
    /**
     * - for BlueSTSDK V1 -> bit map that tell us the available features
     * - for BlueSTSDK V2 -> Option bytes
     */
    private val optByteFeatureMap: Long,
    private val deviceId: Byte,
    private val protocolVersion: Short,
    private val model: Boards.Model,
    private val isSleeping: Boolean,
    private val hasGeneralPurpose: Boolean
) : BleAdvertiseInfo {

    override fun getName() = name

    override fun getTxPower() = txPower

    override fun getAddress() = address

    override fun getFeatureMap() = if (protocolVersion.toInt() == 1) optByteFeatureMap else 0

    override fun getOptionBytes() = if (protocolVersion.toInt() == 1) 0 else optByteFeatureMap

    override fun getDeviceId() = deviceId

    override fun getProtocolVersion() = protocolVersion

    override fun getBoardType() = model

    override fun isBoardSleeping() = isSleeping

    override fun hasGeneralPurpose() = hasGeneralPurpose
}