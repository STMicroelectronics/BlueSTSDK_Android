/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.advertise

import com.st.blue_sdk.models.Boards

data class LeBlueStSdkAdvertiseInfo(
    private val name: String,
    private val address: String,
    private val deviceId: Int,
    private val firmwareId: Int,
    private var protocolId: Int,
    private var payloadData: ByteArray,
    private val protocolVersion: Short,
    private val model: Boards.Model
) : LeBleAdvertiseInfo {

    override fun getName() = name
    override fun getAddress() = address
    override fun getProtocolVersion() = protocolVersion
    override fun getBoardType() = model
    override fun getDeviceId() = deviceId
    override fun getFwId() = firmwareId
    override fun getProtocolId() = protocolId
    override fun getPayloadData() = payloadData

    //Standard equals/hashCode functions
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LeBlueStSdkAdvertiseInfo

        if (deviceId != other.deviceId) return false
        if (firmwareId != other.firmwareId) return false
        if (protocolId != other.protocolId) return false
        if (protocolVersion != other.protocolVersion) return false
        if (name != other.name) return false
        if (address != other.address) return false
        if (!payloadData.contentEquals(other.payloadData)) return false
        if (model != other.model) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceId
        result = 31 * result + firmwareId
        result = 31 * result + protocolId
        result = 31 * result + protocolVersion
        result = 31 * result + name.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + payloadData.contentHashCode()
        result = 31 * result + model.hashCode()
        return result
    }
}