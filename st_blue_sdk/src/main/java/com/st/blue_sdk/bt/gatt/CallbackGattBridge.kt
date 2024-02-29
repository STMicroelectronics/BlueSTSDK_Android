/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.gatt

import android.bluetooth.*
import android.util.Log

class CallbackGattBridge : BluetoothGattCallback(), GattBridge {

    companion object {
        private val TAG = CallbackGattBridge::class.java.simpleName
    }

    private var connectionListener: ((connected: Boolean) -> Unit)? = null

    private var servicesDiscoveryListener: ((gattServices: List<BluetoothGattService>?) -> Unit)? =
        null

    private var writeListener: ((characteristic: BluetoothGattCharacteristic?, successful: Boolean) -> Unit)? =
        null

    private var readListener: ((characteristic: BluetoothGattCharacteristic, successful: Boolean) -> Unit)? =
        null

    private var notificationListener: ((characteristic: BluetoothGattCharacteristic?) -> Unit)? =
        null

    private var descriptorWriteListener: ((characteristic: BluetoothGattCharacteristic?, successful: Boolean) -> Unit)? =
        null

    private var rssiUpdateListener: ((rssi: Int) -> Unit)? = null

    private var mtuUpdateListener: ((mtu: Int) -> Unit)? = null

    fun setConnectionListener(connectionListener: (connected: Boolean) -> Unit) {
        this.connectionListener = connectionListener
    }

    fun setServiceDiscoveryListener(discoveryListener: (gattServices: List<BluetoothGattService>?) -> Unit) {
        this.servicesDiscoveryListener = discoveryListener
    }

    fun setWriteListener(writeListener: (characteristic: BluetoothGattCharacteristic?, successful: Boolean) -> Unit) {
        this.writeListener = writeListener
    }

    fun setReadListener(readListener: (characteristic: BluetoothGattCharacteristic, successful: Boolean) -> Unit) {
        this.readListener = readListener
    }

    fun setNotificationListener(notificationListener: (characteristic: BluetoothGattCharacteristic?) -> Unit) {
        this.notificationListener = notificationListener
    }

    fun setDescriptorWriteListener(descriptorListener: (characteristic: BluetoothGattCharacteristic?, successful: Boolean) -> Unit) {
        this.descriptorWriteListener = descriptorListener
    }

    fun setRssiListener(rssiListener: (rssi: Int) -> Unit) {
        this.rssiUpdateListener = rssiListener
    }

    fun setMtuListener(mtuListener: (mtu: Int) -> Unit) {
        this.mtuUpdateListener = mtuListener
    }

    override fun getBleGattCallback() = this

    fun detachListeners() {
        connectionListener = null
        servicesDiscoveryListener = null
        writeListener = null
        readListener = null
        notificationListener = null
        descriptorWriteListener = null
        rssiUpdateListener = null
        mtuUpdateListener = null
    }

    override fun onConnectionStateChange(
        gatt: BluetoothGatt,
        status: Int,
        newState: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectionListener?.invoke(true)
                    Log.i(TAG, "Connected to GATT server.")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectionListener?.invoke(false)
                    Log.i(TAG, "Disconnected from GATT server.")
                }
            }
        } else {
            Log.i(TAG, "Disconnected from GATT server.")
            connectionListener?.invoke(false)
        }
    }

    // New services discovered
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                servicesDiscoveryListener?.invoke(gatt.services)
                Log.i(TAG, "Services discovered")
            }
            else -> {
                servicesDiscoveryListener?.invoke(null)
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        readListener?.invoke(characteristic, status == BluetoothGatt.GATT_SUCCESS)
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        writeListener?.invoke(characteristic, status == BluetoothGatt.GATT_SUCCESS)
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        notificationListener?.invoke(characteristic)
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        descriptorWriteListener?.invoke(
            descriptor?.characteristic,
            status == BluetoothGatt.GATT_SUCCESS
        )
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            rssiUpdateListener?.invoke(rssi)
        }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mtuUpdateListener?.invoke(mtu)
        }
    }
}