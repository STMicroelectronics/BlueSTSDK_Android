/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.hal

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.st.blue_sdk.bt.advertise.BleAdvertiseInfo
import com.st.blue_sdk.models.BleNotification
import com.st.blue_sdk.models.ChunkProgress
import com.st.blue_sdk.models.Node
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

/**
 * Bluetooth LE hardware abstract layer
 *
 * Contains methods to communicate with bluetooth peripherals
 */
interface BleHal {

    companion object {

        val TAG = BleHal::class.simpleName

        val NOTIFICATION_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

        val SERVICE_CHANGED_SERVICE_UUID: UUID =
            UUID.fromString("00001801-0000-1000-8000-00805f9b34fb")

        val SERVICE_CHANGED_CHAR_UUID: UUID =
            UUID.fromString("00002A05-0000-1000-8000-00805f9b34fb")

        const val DEFAULT_GATT_TIMEOUT: Long = 1000

        const val MAX_REFRESH_DEVICE_CACHE_TRY: Long = 1000
    }

    /**
     * Update the rssi
     *
     * @param rssi new value to set.
     * */
    fun setRssi(rssi: Int)

    /**
     * Update the Advertise Info
     *
     * @param advertiseInfo new value to set.
     * */
    fun setAdvertiseInfo(advertiseInfo: BleAdvertiseInfo)


    /**
     * Update the BLE device info
     *
     * @param device new value to set.
     * */
    fun setDeviceInfo(device: BluetoothDevice)


    /**
     * Connect to BLE peripheral
     *
     * @param autoConnect whether autoconnect to selected node.
     *
     * @return node state flow that describe node status
     * */
    fun connectToDevice(autoConnect: Boolean = false): StateFlow<Node>

    /**
     * @return selected BLE node
     */
    fun getDevice(): Node

    /**
     * State flow returned by this call is the same as #connectToDevice
     *
     * @return BLE node status flow
     */
    fun getDeviceStatus(): StateFlow<Node>

    /**
     *  @return BLE messages arrived on any notifiable characteristic subscribed by the user
     */
    fun getDeviceNotifications(): Flow<BleNotification>

    /**
     *  @return progression of a chunk transmission
     */
    fun getChunkProgressUpdates(): Flow<ChunkProgress>

    /**
     * @return true if current connection status is Connected, ServicesDiscovered or Ready
     */
    fun isConnected(): Boolean

    /**
     * @return true if current connection status is Ready
     */
    fun isReady(): Boolean

    fun getRssi()

    /**
     * Ready state indicate that ST characteristics features have been discovered.
     * Feature discovery must be executed in an upper layer, this method set current
     * connection state to Ready if internal conditions conditions are met.
     */
    fun setNodeStatusToReady()

    /**
     * @return discovered BLE services
     */
    fun getDiscoveredServices(): List<BluetoothGattService>

    /**
     * Read specified characteristic
     *
     * @param characteristic to read
     * @param timeout [ms] to wait for read response
     *
     * @return characteristic data if reed succeed
     */
    suspend fun readCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        timeout: Long
    ): ByteArray?

    suspend fun readCharacteristic(
        serviceUid: String,
        characteristicUid: String,
        timeout: Long
    ): ByteArray?

    suspend fun writeCharacteristic(
        serviceUid: String,
        characteristicUid: String,
        data: ByteArray,
        payloadSize: Int? = null,
        timeout: Long = 1000L,
        awaitFeedback: Boolean = true
    ): Boolean

    /**
     * Write specified characteristic
     *
     * @param characteristic to write
     * @param data to write
     * @param payloadSize split data in packets of this size
     * @param timeout time interval for BLE write acknowledgement
     * @param awaitFeedback true to await write feedback, false otherwise
     *
     * @return true if write succeed
     */
    suspend fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
        payloadSize: Int? = null,
        timeout: Long = 1000L,
        awaitFeedback: Boolean = true
    ): Boolean

    suspend fun setCharacteristicNotification(
        serviceUid: String,
        characteristicUid: String,
        enabled: Boolean,
        timeout: Long = 1000L,
        awaitFeedback: Boolean = true
    ): Boolean

    /**
     * Write characteristic descriptor to enable/disable notifications
     *
     * @param characteristic to write
     * @param enabled whether enable or disable notifications
     * @param timeout [ms] time interval for BLE write acknowledgement
     * @param awaitFeedback true to await write feedback, false otherwise
     */
    suspend fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean,
        timeout: Long = 1000L,
        awaitFeedback: Boolean = true
    ): Boolean

    /**
     * Ask connected BLE peripheral to change maxPayloadSize
     *
     * @param timeout [ms] time interval for BLE write acknowledgement
     * @param maxPayloadSize requested MTU wil be this size + 3
     *
     * @return concorded payload size
     */
    suspend fun requestPayloadSize(
        timeout: Long = 1000L,
        maxPayloadSize: Int
    ): Int

    fun disconnect()

    fun getCharacteristic(
        serviceUid: String,
        characteristicUid: String
    ): BluetoothGattCharacteristic?

    /**
     * set connection interval to BluetoothGatt.CONNECTION_PRIORITY_HIGH
     *
     * @return true if request was initiated successfully
     */
    fun requestLowerConnectionInterval(): Boolean
}