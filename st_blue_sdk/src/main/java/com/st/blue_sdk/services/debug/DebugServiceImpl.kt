/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.debug

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.st.blue_sdk.NodeService
import com.st.blue_sdk.bt.hal.BleHal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import java.nio.charset.StandardCharsets

class DebugServiceImpl(
    private val bleHAL: BleHal
) : DebugService {

    companion object {
        private val CHARSET = StandardCharsets.ISO_8859_1 //ASCII
        private val TAG = DebugService::class.simpleName
    }

    private var rwDebugCharacteristic: BluetoothGattCharacteristic? = null
    private var hasEnabledReadWriteNotifications = false

    private var errorDebugCharacteristic: BluetoothGattCharacteristic? = null
    private var hasEnabledErrorNotifications = false

    override suspend fun init() {

        if (hasBleDebugService().not())
            return

        rwDebugCharacteristic = bleHAL.getCharacteristic(
            NodeService.DEBUG_SERVICE_UUID.toString(),
            NodeService.DEBUG_RW_CHARACTERISTIC_UUID.toString()
        )

        errorDebugCharacteristic = bleHAL.getCharacteristic(
            NodeService.DEBUG_SERVICE_UUID.toString(),
            NodeService.DEBUG_ERROR_CHARACTERISTIC_UUID.toString()
        )

        enableDebugConsoleNotifications()
    }

    override fun hasBleDebugService(): Boolean {
        return bleHAL.getDiscoveredServices()
            .any { service -> service.uuid == NodeService.DEBUG_SERVICE_UUID }
    }

    override suspend fun write(message: String): Int {
       // Log.i("DebugDebugConsole","write on Debug <$message>")
        return write(message.toByteArray(CHARSET))
    }

    override suspend fun write(data: ByteArray, mtu: Int?): Int {

        rwDebugCharacteristic ?: return 0

        enableDebugConsoleNotifications()

        val payloadSize = mtu ?: bleHAL.getDevice().maxPayloadSize

        val hasWriteData = bleHAL.writeCharacteristic(
            characteristic = rwDebugCharacteristic!!,
            data = data,
            payloadSize = payloadSize,
            timeout = 2000L
        )
        return if (hasWriteData) data.size else 0
    }

    override fun getDebugMessages(): Flow<DebugMessage> {

        return bleHAL.getDeviceNotifications().transform {
            if (it.characteristic.uuid == rwDebugCharacteristic?.uuid ||
                it.characteristic.uuid == errorDebugCharacteristic?.uuid
            ) {
                val isError = it.characteristic.uuid == errorDebugCharacteristic?.uuid
                //Log.i("DebugDebugConsole","Get getDebugMessages <${String(it.data, CHARSET)}>")
                emit(DebugMessage(String(it.data, CHARSET), isError))
            }
        }
    }

    override suspend fun read(timeout: Long): DebugMessage? {
        return rwDebugCharacteristic?.let { recipient ->
            val data = bleHAL.readCharacteristic(recipient, timeout)
            //data?.let { payload -> Log.i("Pezz","Read on Debug <${String(payload, CHARSET)}>") }
            return data?.let { payload -> DebugMessage(String(payload, CHARSET), false) }
        }
    }

    override fun getMaxPayloadSize(): Int {
        return bleHAL.getDevice().maxPayloadSize
    }

    private suspend fun enableDebugConsoleNotifications() {

        if (hasEnabledReadWriteNotifications.not()) {
            rwDebugCharacteristic?.let {
                hasEnabledReadWriteNotifications = bleHAL.setCharacteristicNotification(it, true)
                Log.d(TAG, "RW result $hasEnabledReadWriteNotifications")
            }
        }

        if (hasEnabledErrorNotifications.not()) {
            errorDebugCharacteristic?.let {
                hasEnabledErrorNotifications = bleHAL.setCharacteristicNotification(it, true)
                Log.d(TAG, "ERROR result $hasEnabledErrorNotifications")
            }
        }
    }
}