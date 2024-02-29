/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.models

import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(
    primaryKeys = ["uuid"],
    tableName = "bleCharacteristic"
)

@Serializable
data class BleCharacteristic(
    @SerialName(value = "name")
    val name: String,
    @SerialName(value = "uuid")
    val uuid: String,
    @SerialName(value = "uuid_type")
    val uuidType: Int? = null,
    @SerialName(value = "dtmi_name")
    val dtmiName: String? = null,
    @SerialName(value = "format_notify")
    val formatNotify: List<BleCharacteristicFormat>? = null,
    @SerialName(value = "format_write")
    val formatWrite: List<BleCharacteristicFormat>? = null
)
