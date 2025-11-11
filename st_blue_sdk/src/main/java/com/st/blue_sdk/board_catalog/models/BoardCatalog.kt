/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

@file:UseSerializers(DateSerializer::class)

package com.st.blue_sdk.board_catalog.models

import com.st.blue_sdk.board_catalog.api.serializers.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.*

@Serializable
data class BoardCatalog(
    @SerialName(value = "date")
    val date: Date? = null,
    @SerialName(value = "version")
    val version: String? = null,
    @SerialName(value = "stable_release")
    val stableRelease: Boolean? = true,
    @SerialName(value = "checksum")
    val checksum: String? = null,
    @SerialName(value = "bluestsdk_v2")
    val bleListBoardFirmwareV2: List<BoardFirmware>? = null,
    @SerialName(value = "bluestsdk_v1")
    val bleListBoardFirmwareV1: List<BoardFirmware>? = null,
    @SerialName(value = "characteristics")
    val characteristics: List<BleCharacteristic>? = null,
    @SerialName(value = "boards")
    val boards: List<BoardDescription>? = null,
    @SerialName(value = "sensor_adapters")
    val sensorAdapters: List<Sensor>? = null,
)
