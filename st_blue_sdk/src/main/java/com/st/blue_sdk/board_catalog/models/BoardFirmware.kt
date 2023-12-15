/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
@file:UseSerializers(FirmwareMaturityTypeSerializer::class)

package com.st.blue_sdk.board_catalog.models

import com.st.blue_sdk.board_catalog.api.serializers.FirmwareMaturityTypeSerializer
import kotlinx.serialization.UseSerializers

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.st.blue_sdk.models.Boards
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Entity(
    primaryKeys = ["ble_dev_id", "ble_fw_id"],
    tableName = "board_firmware"
)
@Serializable
data class BoardFirmware(
    @ColumnInfo(name = "ble_dev_id")
    @SerialName(value = "ble_dev_id")
    val bleDevId: String,
    @ColumnInfo(name = "ble_fw_id")
    @SerialName(value = "ble_fw_id")
    val bleFwId: String,
    @ColumnInfo(name = "board_name")
    @SerialName(value = "brd_name")
    val brdName: String,
    @ColumnInfo(name = "fw_version")
    @SerialName(value = "fw_version")
    val fwVersion: String,
    @ColumnInfo(name = "fw_name")
    @SerialName(value = "fw_name")
    val fwName: String,
    @ColumnInfo(name = "dtmi")
    @SerialName("dtmi")
    val dtmi: String? = null,
    @ColumnInfo(name = "cloud_apps")
    @SerialName(value = "cloud_apps")
    val cloudApps: List<CloudApp>,
    @ColumnInfo(name = "characteristics")
    @SerialName(value = "characteristics")
    val characteristics: List<BleCharacteristic>,
    @ColumnInfo(name = "option_bytes")
    @SerialName(value = "option_bytes")
    val optionBytes: List<OptionByte>,
    @ColumnInfo(name = "fw_desc")
    @SerialName("fw_desc")
    val fwDesc: String,
    @ColumnInfo(name = "changelog")
    @SerialName("changelog")
    val changelog: String? = null,
    @ColumnInfo(name = "fota")
    @SerialName("fota")
    var fota: FotaDetails,
    @ColumnInfo(name = "compatible_sensor_adapters")
    @SerialName("compatible_sensor_adapters")
    var compatibleSensorAdapters: List<Int>?=null,
    @SerialName("demo_decorator")
    var demoDecorator: DemoDecorator?=null,
    @SerialName("maturity")
    var maturity: FirmwareMaturity?=null
) {

    fun friendlyName(): String =
        fwName + "V" + fwVersion


    fun boardModel(): Boards.Model =
        Boards.getModelFromIdentifier(Integer.decode(bleDevId), sDkVersion = 2)

    companion object {
        fun mock() = BoardFirmware(
            bleDevId = "0x07",
            bleFwId = "0xE",
            brdName = "STEVAL-BCNKT01V1",
            fwVersion = "1.0.1",
            fwName = "FP-SNS-FLIGHT1",
            cloudApps = emptyList(),
            characteristics = emptyList(),
            optionBytes = emptyList(),
            fwDesc = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer tempor posuere enim, et imperdiet quam mattis at.",
            fota = FotaDetails(),
            maturity = FirmwareMaturity.RELEASE
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BoardFirmware) return false

        if (bleDevId != other.bleDevId) return false
        if (bleFwId != other.bleFwId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bleDevId.hashCode()
        result = 31 * result + bleFwId.hashCode()
        result = 31 * result + brdName.hashCode()
        result = 31 * result + fwVersion.hashCode()
        result = 31 * result + fwName.hashCode()
        result = 31 * result + (dtmi?.hashCode() ?: 0)
        result = 31 * result + cloudApps.hashCode()
        result = 31 * result + characteristics.hashCode()
        result = 31 * result + optionBytes.hashCode()
        result = 31 * result + fwDesc.hashCode()
        result = 31 * result + (changelog?.hashCode() ?: 0)
        result = 31 * result + fota.hashCode()
        result = 31 * result + (compatibleSensorAdapters?.hashCode() ?: 0)
        result = 31 * result + (demoDecorator?.hashCode() ?: 0)
        result = 31 * result + (maturity?.hashCode() ?: 0)
        return result
    }
}
