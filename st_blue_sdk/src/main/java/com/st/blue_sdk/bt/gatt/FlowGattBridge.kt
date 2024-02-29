/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.st.blue_sdk.bt.hal.BleHal
import com.st.blue_sdk.models.BleNotification
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

data class ConnectionEvent(val isConnected: Boolean)

sealed class BleEvent {

    data class ServicesDiscovered(
        val successful: Boolean,
        val discoveredServices: List<BluetoothGattService>
    ) : BleEvent()

    data class CharacteristicRead(
        val successful: Boolean,
        val characteristic: BluetoothGattCharacteristic,
        val data: ByteArray
    ) : BleEvent()

    data class CharacteristicWrite(
        val successful: Boolean,
        val characteristic: BluetoothGattCharacteristic?
    ) : BleEvent()

    data class DescriptorWrite(
        val successful: Boolean,
        val characteristic: BluetoothGattCharacteristic?
    ) : BleEvent()

    data class MtuChanged(val mtu: Int) : BleEvent()

    data class RssiChanged(val rssi: Int) : BleEvent()
}

class GattBridgeFlow : BluetoothGattCallback(), GattBridge {

    companion object {
        private val TAG = GattBridgeFlow::class.java.simpleName
    }

    val notificationFlow = MutableSharedFlow<BleNotification>(
        replay = 0,
        extraBufferCapacity = 500,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val connectionStatusFlow = MutableSharedFlow<ConnectionEvent>(
        replay = 0,
        extraBufferCapacity = 30,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val bleEventsFlow = MutableSharedFlow<BleEvent>(
        replay = 0,
        extraBufferCapacity = 30,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun onConnectionStateChange(
        gatt: BluetoothGatt,
        status: Int,
        newState: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected to GATT server.")
                    connectionStatusFlow.tryEmit(ConnectionEvent(true))
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server.")
                    connectionStatusFlow.tryEmit(ConnectionEvent(false))
                }
            }
        } else {
            Log.i(TAG, "Disconnected from GATT server.")
            connectionStatusFlow.tryEmit(ConnectionEvent(false))
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                Log.i(TAG, "Services has been discovered")
                bleEventsFlow.tryEmit(BleEvent.ServicesDiscovered(true, gatt.services))
            }
            else -> {
                Log.w(TAG, "onServicesDiscovered received: $status")
                bleEventsFlow.tryEmit(BleEvent.ServicesDiscovered(false, emptyList()))
            }
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        Log.i(
            TAG,
            "Characteristic with uid ${characteristic.uuid} has been red with data ${characteristic.value}"
        )
        bleEventsFlow.tryEmit(
            BleEvent.CharacteristicRead(
                status == BluetoothGatt.GATT_SUCCESS,
                characteristic,
                characteristic.value
            )
        )
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        characteristic?.let {
            Log.i(
                TAG,
                "Characteristic with uid ${it.uuid} has been written with data ${it.value.contentToString()}"
            )
        }

        bleEventsFlow.tryEmit(
            BleEvent.CharacteristicWrite(
                status == BluetoothGatt.GATT_SUCCESS,
                characteristic
            )
        )
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?) {
        characteristic?.let {
            Log.d(
                TAG,
                "Characteristic with uid ${it.uuid} has changed with data ${it.value.contentToString()}"
            )
            notificationFlow.tryEmit(BleNotification(it, it.value))
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        descriptor?.let {
            Log.d(
                TAG,
                "Descriptor with uid ${it.uuid} has been written"
            )
            bleEventsFlow.tryEmit(
                BleEvent.DescriptorWrite(
                    status == BluetoothGatt.GATT_SUCCESS,
                    it.characteristic
                )
            )
        }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "Rssi changed with value: $rssi")
            bleEventsFlow.tryEmit(BleEvent.RssiChanged(rssi))
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "mtu changed with value: $mtu")
            bleEventsFlow.tryEmit(BleEvent.MtuChanged(mtu))
        }
    }

    override fun getBleGattCallback(): BluetoothGattCallback {
        return this
    }
}