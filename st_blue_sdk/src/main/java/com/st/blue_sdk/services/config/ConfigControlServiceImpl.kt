/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.config

import android.bluetooth.BluetoothGattCharacteristic
import com.st.blue_sdk.NodeService
import com.st.blue_sdk.bt.hal.BleHal
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.WriteError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withTimeoutOrNull

class ConfigControlServiceImpl(
    private val bleHAL: BleHal
) : ConfigControlService {

    companion object {

        private val TAG = ConfigControlService::class.simpleName

        private const val MIN_FEATURE_RESPONSE_LEN = 7
    }

    private var hasEnabledFeatureResponseNotifications = false

    private var featureConfigurationCharacteristic: BluetoothGattCharacteristic? = null

    private var registerConfigurationCharacteristic: BluetoothGattCharacteristic? = null

    override val configControlUpdates: Flow<ByteArray>
        get() = bleHAL.getDeviceNotifications().transform { bleNotification ->
            if (bleNotification.characteristic.uuid == featureConfigurationCharacteristic?.uuid) {
                if (bleNotification.data.size > MIN_FEATURE_RESPONSE_LEN) {
                    emit(bleNotification.data)
                }
            }
        }

    override suspend fun init() {

        if (hasBleConfigService().not())
            return

        featureConfigurationCharacteristic = bleHAL.getCharacteristic(
            NodeService.CONFIGURATION_SERVICE_UUID.toString(),
            NodeService.FEATURE_CONFIGURATION_RW_CHARACTERISTIC_UID.toString()
        )

        registerConfigurationCharacteristic = bleHAL.getCharacteristic(
            NodeService.CONFIGURATION_SERVICE_UUID.toString(),
            NodeService.BOARD_CONFIGURATION_RW_CHARACTERISTIC_UID.toString()
        )
    }

    override fun hasBleConfigService(): Boolean {
        return bleHAL.getDiscoveredServices()
            .any { service -> service.uuid == NodeService.CONFIGURATION_SERVICE_UUID }
    }

    override suspend fun writeFeatureCommand(
        featureCommand: FeatureCommand,
        feature: Feature<*>,
        writeTimeout: Long,
        responseTimeout: Long,
        retry: Int,
        retryDelay: Long
    ): FeatureResponse? {

        if (hasEnabledFeatureResponseNotifications.not()) {
            enableFeatureCommandsNotifications()
        }

        val data =
            feature.packCommandData(feature.mask, featureCommand) ?: return null // cannot pack data

        val outCharacteristic = featureConfigurationCharacteristic ?: return null
        val hasWriteData = bleHAL.writeCharacteristic(
            characteristic = outCharacteristic,
            data = data,
            payloadSize = feature.maxPayloadSize,
            timeout = writeTimeout
        )
        if (hasWriteData.not()) {
            return if (retry > 0) {
                delay(retryDelay)
                writeFeatureCommand(
                    featureCommand = featureCommand,
                    feature = feature,
                    writeTimeout = writeTimeout,
                    responseTimeout = responseTimeout,
                    retry = retry - 1,
                    retryDelay = retryDelay
                )
            } else WriteError(feature, featureCommand.commandId) // write error
        }

        return withTimeoutOrNull(responseTimeout) {
            var featureResponse: FeatureResponse? = null
            configControlUpdates.firstOrNull()?.let { rawData ->
                featureResponse = feature.parseCommandResponse(rawData)
            }
            return@withTimeoutOrNull featureResponse
        }
    }

    private suspend fun enableFeatureCommandsNotifications() {
        hasEnabledFeatureResponseNotifications =
            bleHAL.setCharacteristicNotification(
                NodeService.CONFIGURATION_SERVICE_UUID.toString(),
                NodeService.FEATURE_CONFIGURATION_RW_CHARACTERISTIC_UID.toString(),
                true,
                awaitFeedback = true
            )
    }
}