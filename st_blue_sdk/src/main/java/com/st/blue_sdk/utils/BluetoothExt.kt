/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.utils

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.models.Boards
import java.util.*

fun BluetoothGattCharacteristic.isStandardFeatureCharacteristics() =
    uuid.isStandardFeatureCharacteristics()

fun BluetoothGattCharacteristic.isGeneralPurposeFeatureCharacteristics() =
    uuid.isGeneralPurposeFeatureCharacteristics()

fun BluetoothGattCharacteristic.isExtendedOrExternalFeatureCharacteristics() =
    uuid.isExtendedOrExternalFeatureCharacteristics()

fun UUID.isStandardFeatureCharacteristics() =
    toString().endsWith(Feature.Type.STANDARD.suffix)

fun UUID.isGeneralPurposeFeatureCharacteristics() =
    toString().endsWith(Feature.Type.GENERAL_PURPOSE.suffix)

fun UUID.isExtendedOrExternalFeatureCharacteristics() =
    toString().endsWith(Feature.Type.EXTENDED.suffix) ||
            toString().endsWith(Feature.Type.EXTERNAL_STM32.suffix) ||
            toString().endsWith(Feature.Type.EXTERNAL_BLUE_NRG_OTA.suffix) ||
            toString().endsWith(Feature.Type.EXTERNAL_STD_CHART.suffix)

const val SHR_MASK = 32

fun BluetoothGattCharacteristic.getFeature(): Feature<*> = uuid.getFeature()

fun UUID.getFeature(): Feature<*> {
    val header = toString().substring(0..7)
    val suffix = toString().substring(8)

    return Feature.createFeature(
        identifier = Integer.decode("0x$header"),
        type = Feature.Type.fromSuffix(suffix = suffix)
    )
}

fun BluetoothGattCharacteristic.getGPFeature(): Feature<*> = uuid.getGPFeature()

fun UUID.getGPFeature(): Feature<*> {
    val header = toString().substring(0..3)
    val suffix = toString().substring(4)

    return Feature.createFeature(
        identifier = Integer.decode("0x$header"),
        type = Feature.Type.fromSuffix(suffix = suffix)
    )
}

fun BluetoothGattCharacteristic.buildFeatures(
    advertiseMask: Long,
    protocolVersion: Short,
    boardModel: Boards.Model
): List<Feature<*>> = uuid.buildFeatures(
    advertiseMask = advertiseMask,
    protocolVersion = protocolVersion,
    boardModel = boardModel
)

fun UUID.buildFeatures(
    advertiseMask: Long,
    protocolVersion: Short,
    boardModel: Boards.Model
): List<Feature<*>> {
    val featureMask = (mostSignificantBits shr SHR_MASK).toInt()
    val features = mutableListOf<Feature<*>>()

    var mask = 1L shl 31
    for (i in 0..31) {
        if ((featureMask and mask.toInt()) != 0) {
            features.add(
                Feature.createFeature(
                    boardModel = boardModel,
                    type = Feature.Type.STANDARD,
                    identifier = mask.toInt(),
                    isEnabled =
                    if (protocolVersion.toInt() == 1) {
                        advertiseMask and mask != 0L
                    } else {
                        true
                    }
                )
            )
        }
        mask = mask shr 1
    }

    return features
}

fun UUID.buildFeatures(boardModel: Boards.Model): List<Feature<*>> {
    val featureMask = (mostSignificantBits shr SHR_MASK).toInt()
    val features = mutableListOf<Feature<*>>()

    var mask = 1L shl 31
    for (i in 0..31) {
        if ((featureMask and mask.toInt()) != 0) {
            features.add(
                Feature.createFeature(
                    boardModel = boardModel,
                    type = Feature.Type.STANDARD,
                    identifier = mask.toInt(),
                    isEnabled = true
                )
            )
        }
        mask = mask shr 1
    }

    return features
}

fun UUID.buildNotifiableChar(uuid: UUID): BluetoothGattCharacteristic {
    val gattChar =
        BluetoothGattCharacteristic(this, BluetoothGattCharacteristic.PROPERTY_NOTIFY, 0)
    val aDesc =
        BluetoothGattDescriptor(uuid, BluetoothGattDescriptor.PERMISSION_WRITE)
    aDesc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
    gattChar.addDescriptor(aDesc)
    return gattChar
}
