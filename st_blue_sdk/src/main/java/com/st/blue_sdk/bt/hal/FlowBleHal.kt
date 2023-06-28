/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.hal

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.st.blue_sdk.bt.BleBondingBroadcastReceiver
import com.st.blue_sdk.bt.advertise.BleAdvertiseInfo
import com.st.blue_sdk.bt.gatt.BleEvent
import com.st.blue_sdk.bt.gatt.GattBridgeFlow
import com.st.blue_sdk.bt.hal.BleHal.Companion.DEFAULT_GATT_TIMEOUT
import com.st.blue_sdk.bt.hal.BleHal.Companion.MAX_REFRESH_DEVICE_CACHE_TRY
import com.st.blue_sdk.bt.hal.BleHal.Companion.NOTIFICATION_UUID
import com.st.blue_sdk.bt.hal.BleHal.Companion.SERVICE_CHANGED_CHAR_UUID
import com.st.blue_sdk.bt.hal.BleHal.Companion.SERVICE_CHANGED_SERVICE_UUID
import com.st.blue_sdk.bt.hal.BleHal.Companion.TAG
import com.st.blue_sdk.models.*
import com.st.blue_sdk.utils.hasBluetoothPermission
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class FlowBleHal(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val device: BluetoothDevice,
    private val rssi: Int,
    advertiseInfo: BleAdvertiseInfo? = null,
    private val gattBridge: GattBridgeFlow = GattBridgeFlow()
) : BleHal {

    /**
     * Flow to notify node status changes
     * */
    private val deviceStateFlow =
        MutableStateFlow(
            Node(
                device = device,
                advertiseInfo = advertiseInfo,
                rssi = RssiData(rssi, Date())
            )
        )

    private var discoverServicesJob: Job? = null

    private var userAskToDisconnect: Boolean = false

    /**
     * Broadcast receiver to handle pairing (bonding) requests
     * */
    private var bondingBroadcastReceiver: BleBondingBroadcastReceiver? = null

    /**
     * Mutex used to synchronize BLE operations
     * */
    private var mutex = Mutex()

    /**
     * Coroutine job used to collect gatt data
     * */
    private var gattBridgeJob: Job? = null

    override fun setRssi(rssi: Int) {
        deviceStateFlow.update {
            it.copy(rssi = RssiData(rssi = rssi, timestamp = Date()))
        }
    }

    override fun setAdvertiseInfo(advertiseInfo: BleAdvertiseInfo) {
        deviceStateFlow.update {
            it.copy(advertiseInfo = advertiseInfo)
        }
    }

    override fun setDeviceInfo(device: BluetoothDevice) {
        deviceStateFlow.update {
            it.copy(device = device)
        }
    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(autoConnect: Boolean): StateFlow<Node> {

        if (context.hasBluetoothPermission().not()) {
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        val node = deviceStateFlow.value
        val isConnectedOrConnecting =
            isConnected() || node.connectionStatus.current == NodeState.Connecting

        if (isConnectedOrConnecting) {
            Log.d(TAG, "Connection attempt already in progress or node already connected")
            return deviceStateFlow
        }

        userAskToDisconnect = false
        attachGattBridgeListeners()

        val gatt = device.connectGatt(
            context,
            autoConnect,
            gattBridge.getBleGattCallback()
        )
        val btDevice = node.copy(
            device = device,
            rssi = RssiData(rssi, Date()),
            deviceGatt = gatt,
            connectionStatus = buildNodeState(NodeState.Connecting)
        )

        Log.d(TAG, "Connecting to node with address: ${btDevice.device.address}")
        deviceStateFlow.update { btDevice }

        return deviceStateFlow
    }

    override fun getDevice() = deviceStateFlow.value

    override fun getDeviceStatus() = deviceStateFlow

    @SuppressLint("MissingPermission")
    override fun getRssi() {
        val node = deviceStateFlow.value
        node.deviceGatt?.readRemoteRssi()
    }

    override fun getDeviceNotifications(): Flow<BleNotification> = gattBridge.notificationFlow

    override fun isConnected(): Boolean =
        deviceStateFlow.value.connectionStatus.current in NodeState.Connected..NodeState.Ready

    override fun isReady(): Boolean =
        deviceStateFlow.value.connectionStatus.current == NodeState.Ready

    override fun setNodeStatusToReady() {
        if (deviceStateFlow.value.connectionStatus.current == NodeState.ServicesDiscovered) {
            deviceStateFlow.update {
                deviceStateFlow.value.copy(
                    connectionStatus = buildNodeState(
                        NodeState.Ready
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun isPairing(): Boolean {

        if (context.hasBluetoothPermission().not()) { // TODO: check bluetooth CONNECT permission
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        return deviceStateFlow.value.device.bondState == BluetoothDevice.BOND_BONDING
    }

    @SuppressLint("MissingPermission")
    private suspend fun discoverServices(): List<BluetoothGattService> {

        if (context.hasBluetoothPermission().not()) {
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        val node = deviceStateFlow.value
        if (isConnected().not() || node.deviceGatt == null) {
            throw IllegalStateException()
        }

        Log.d(TAG, "Node Services discovery started")

        val defaultValue = BleEvent.ServicesDiscovered(false, listOf())
        val discoveryResult =
            mutex.enqueueWithTimeout(delay = 100, timeout = 10000L, defaultValue = defaultValue) {
                gattBridge.bleEventsFlow.onSubscription {
                    Log.d(TAG, "Service discovery stared")
                    val isDiscoverStated = node.deviceGatt.discoverServices()
                    if (isDiscoverStated.not()) {
                        emit(defaultValue)
                    }
                }.firstOrNull {
                    it is BleEvent.ServicesDiscovered
                } as BleEvent.ServicesDiscovered
            }

        return if (discoveryResult.successful) discoveryResult.discoveredServices else listOf()
    }

    override fun getDiscoveredServices(): List<BluetoothGattService> {
        return deviceStateFlow.value.deviceGatt?.services ?: emptyList()
    }

    /**
     * test if a characteristics can be read
     * @param characteristic characteristic to read
     * @return true if we can read it
     */
    private fun canReadCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {

        val node = deviceStateFlow.value
        if (isConnected().not() || node.deviceGatt == null) {
            return false
        }

        if (characteristic == null)
            return false

        return (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0
    }

    @SuppressLint("MissingPermission")
    override suspend fun readCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        timeout: Long
    ): ByteArray? {

        if (context.hasBluetoothPermission().not()) {
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        val node = deviceStateFlow.value
        if (isConnected().not() || node.deviceGatt == null) {
            throw IllegalStateException()
        }

        if (canReadCharacteristic(characteristic).not()) {
            Log.i(TAG, "Cannot read characteristic ${characteristic.uuid}")
            return null
        }

        val defaultValue = BleEvent.CharacteristicRead(false, characteristic, byteArrayOf())
        val event = mutex.enqueueWithTimeout(timeout = timeout, defaultValue = defaultValue) {
            gattBridge.bleEventsFlow.onSubscription {
                Log.d(TAG, "Reading characteristic with uuid ${characteristic.uuid}")
                if (node.deviceGatt.readCharacteristic(characteristic).not()) {
                    emit(defaultValue)
                }
            }.firstOrNull {
                it is BleEvent.CharacteristicRead && (it.characteristic.uuid == characteristic.uuid)
            } as BleEvent.CharacteristicRead
        }

        return if (event.successful) event.data else null
    }

    override suspend fun readCharacteristic(
        serviceUid: String,
        characteristicUid: String,
        timeout: Long
    ): ByteArray? {
        val characteristic = getCharacteristic(serviceUid, characteristicUid) ?: return null
        return readCharacteristic(characteristic, timeout)
    }

    private fun canWriteCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {

        val node = deviceStateFlow.value
        if (isConnected().not() || node.deviceGatt == null) {
            return false
        }

        if (characteristic == null)
            return false

        return characteristic.properties and
                (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_WRITE) != 0
    }

    override suspend fun writeCharacteristic(
        serviceUid: String,
        characteristicUid: String,
        data: ByteArray,
        payloadSize: Int?,
        timeout: Long,
        awaitFeedback: Boolean
    ): Boolean {
        return deviceStateFlow.value.deviceGatt?.let {
            val characteristic =
                getCharacteristic(serviceUid, characteristicUid) ?: return false
            writeCharacteristic(
                characteristic = characteristic,
                data = data,
                payloadSize = payloadSize,
                timeout = timeout,
                awaitFeedback = awaitFeedback
            )
        } ?: false
    }

    @SuppressLint("MissingPermission")
    override suspend fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
        payloadSize: Int?,
        timeout: Long,
        awaitFeedback: Boolean
    ): Boolean {

        if (context.hasBluetoothPermission().not()) {
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        val node = deviceStateFlow.value
        if (isConnected().not() || node.deviceGatt == null) {
            throw IllegalStateException()
        }

        if (canWriteCharacteristic(characteristic).not()) {
            Log.i(TAG, "Cannot write characteristic ${characteristic.uuid}")
            return false
        }

        var successful = true
        val chunks = data
            .asIterable()
            .chunked(payloadSize ?: deviceStateFlow.value.maxPayloadSize)
            .map { it.toByteArray() }

        val defaultValue = BleEvent.CharacteristicWrite(false, characteristic)

        chunks.forEach {

            val writeResult =
                mutex.enqueueWithTimeout(
                    timeout = timeout,
                    defaultValue = defaultValue
                ) {
                    gattBridge.bleEventsFlow.onSubscription {
                        Log.d(
                            TAG,
                            "Writing characteristic with uuid ${characteristic.uuid}, value ${it.contentToString()}"
                        )

                        characteristic.value = it

                        val hasWriteData = node.deviceGatt.writeCharacteristic(characteristic)

                        if (awaitFeedback.not()) {
                            emit(BleEvent.CharacteristicWrite(hasWriteData, characteristic))
                        }

                        if (hasWriteData.not()) {
                            emit(defaultValue)
                        }
                    }.firstOrNull {
                        it is BleEvent.CharacteristicWrite && (it.characteristic?.uuid == characteristic.uuid)
                    } as BleEvent.CharacteristicWrite
                }

            successful = successful && writeResult.successful

            if (successful.not())
                return false
        }

        return successful
    }

    /**
     * test if a characteristics can be notify
     * @param characteristic characteristic to notify
     * @return true if we can receive notification from it
     */
    private fun canEnableCharacteristicNotification(characteristic: BluetoothGattCharacteristic?): Boolean {

        val node = deviceStateFlow.value
        if (isConnected().not() || node.deviceGatt == null) {
            return false
        }

        if (characteristic == null)
            return false

        return characteristic.properties and
                (BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
    }

    override suspend fun setCharacteristicNotification(
        serviceUid: String,
        characteristicUid: String,
        enabled: Boolean,
        timeout: Long,
        awaitFeedback: Boolean
    ): Boolean {
        val characteristic = getCharacteristic(serviceUid, characteristicUid) ?: return false
        return setCharacteristicNotification(characteristic, enabled, timeout, awaitFeedback)
    }

    @SuppressLint("MissingPermission")
    override suspend fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean,
        timeout: Long,
        awaitFeedback: Boolean
    ): Boolean {

        if (context.hasBluetoothPermission().not()) {
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        val node = deviceStateFlow.value
        if (isConnected().not() || node.deviceGatt == null) {
            throw IllegalStateException()
        }

        if (canEnableCharacteristicNotification(characteristic).not()) {
            return false
        }

        val defaultValue = BleEvent.DescriptorWrite(false, characteristic)

        val writeDescriptorResult =
            mutex.enqueueWithTimeout(delay = 100, timeout = timeout, defaultValue = defaultValue) {

                gattBridge.bleEventsFlow.onSubscription {

                    Log.d(
                        TAG,
                        "Set characteristic (${characteristic.uuid}) descriptor enable=$enabled"
                    )

                    val hasWriteDescriptor = writeCharacteristicNotificationDescriptor(
                        node.deviceGatt,
                        enabled,
                        characteristic
                    )

                    if (awaitFeedback.not()) {
                        emit(BleEvent.DescriptorWrite(hasWriteDescriptor, characteristic))
                    }

                    if (hasWriteDescriptor.not()) {
                        emit(defaultValue)
                    }

                }.firstOrNull {
                    it is BleEvent.DescriptorWrite && (it.characteristic?.uuid == characteristic.uuid)
                } as BleEvent.DescriptorWrite
            }

        return writeDescriptorResult.successful
    }

    @SuppressLint("MissingPermission")
    override suspend fun requestPayloadSize(timeout: Long, maxPayloadSize: Int): Int {

        if (context.hasBluetoothPermission().not()) {
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        val node = deviceStateFlow.value
        if (isConnected().not() || node.deviceGatt == null) {
            throw IllegalStateException()
        }

        return mutex.enqueueWithTimeout(timeout = timeout, defaultValue = node.maxPayloadSize) {

            val mtu = maxPayloadSize + 3
            Log.d(TAG, "Request to set MTU to $mtu")

            val mtuResponse = gattBridge.bleEventsFlow.onSubscription {
                node.deviceGatt.requestMtu(mtu)
            }.firstOrNull {
                it is BleEvent.MtuChanged
            } as BleEvent.MtuChanged

            //FixLP
            node.maxPayloadSize = mtuResponse.mtu - 3
            mtuResponse.mtu - 3
        }
    }

    @SuppressLint("MissingPermission")
    private fun writeCharacteristicNotificationDescriptor(
        gatt: BluetoothGatt,
        enabled: Boolean,
        characteristic: BluetoothGattCharacteristic
    ): Boolean {

        if (gatt.setCharacteristicNotification(characteristic, enabled).not()) {
            return false
        }

        val descriptor = characteristic.getDescriptor(NOTIFICATION_UUID) ?: return false

        when {
            (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 -> {
                descriptor.value =
                    if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            }

            (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0 -> {
                descriptor.value =
                    if (enabled) BluetoothGattDescriptor.ENABLE_INDICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            }

            else -> return false
        }

        return gatt.writeDescriptor(descriptor)
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {

        userAskToDisconnect = true

        val node = deviceStateFlow.value

        cancelServiceDiscovery()
        clearCache()
        gattBridgeJob?.cancel()

        runCatching { context.unregisterReceiver(bondingBroadcastReceiver) }
        bondingBroadcastReceiver = null

        if (isConnected()) {
            deviceStateFlow.update {
                node.copy(
                    connectionStatus = buildNodeState(
                        NodeState.Disconnecting
                    )
                )
            }
            node.apply {
                deviceGatt?.disconnect()
                deviceGatt?.close()
            }
        }

        deviceStateFlow.update {
            node.copy(
                connectionStatus = buildNodeState(NodeState.Disconnected),
                deviceGatt = null
            )
        }
    }

    private fun attachGattBridgeListeners() {

        gattBridgeJob = coroutineScope.launch {

            launch {
                gattBridge.connectionStatusFlow.collect {
                    val newState = when {
                        it.isConnected && isPairing().not() -> NodeState.Connected // connected to ble device, can discover services
                        it.isConnected && isPairing() -> NodeState.Connecting // in pairing, cannot discover services
                        else -> NodeState.Disconnected
                    }

                    updateNodeConnectionState(newState)
                }
            }

            launch {
                gattBridge.bleEventsFlow.collect { event ->
                    when (event) {
                        is BleEvent.ServicesDiscovered -> {
                            val hasDiscoveredServices =
                                event.successful && event.discoveredServices.isNotEmpty()

                            if (hasDiscoveredServices) {
                                deviceStateFlow.update {
                                    deviceStateFlow.value.copy(
                                        connectionStatus = buildNodeState(
                                            NodeState.ServicesDiscovered
                                        )
                                    )
                                }
                                enableBLEServiceChangedIndications()
                            } else {
                                disconnect()
                            }
                        }

                        is BleEvent.MtuChanged -> {
                            val node = deviceStateFlow.value
                            deviceStateFlow.update { node.copy(mtu = event.mtu) }
                        }

                        is BleEvent.RssiChanged -> {
                            val node = deviceStateFlow.value
                            deviceStateFlow.update {
                                node.copy(rssi = RssiData(event.rssi, Date()))
                            }
                        }

                        is BleEvent.CharacteristicRead -> Unit
                        is BleEvent.CharacteristicWrite -> Unit
                        is BleEvent.DescriptorWrite -> Unit
                    }
                }
            }
        }

        bondingBroadcastReceiver = BleBondingBroadcastReceiver { bondState, deviceAddress ->
            when (bondState) {
                BluetoothDevice.BOND_BONDED -> {
                    if (deviceAddress == deviceStateFlow.value.device.address) {
                        updateNodeConnectionState(NodeState.Connected) // can discover services
                    }
                }

                else -> Unit
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(bondingBroadcastReceiver, filter)
    }

    private fun updateNodeConnectionState(newState: NodeState) {

        val updatedNode = deviceStateFlow.value.copy(connectionStatus = buildNodeState(newState))
        deviceStateFlow.update { updatedNode }

        val mustDiscoverServices = newState == NodeState.Connected
        if (mustDiscoverServices) {
            cancelServiceDiscovery()
            discoverServicesJob = coroutineScope.launch { discoverServices() }
        }
    }

    private fun cancelServiceDiscovery() {
        discoverServicesJob?.cancel()
    }

    private fun clearCache(){
        runCatching {
            deviceStateFlow.value.deviceGatt?.let { gatt ->
                gatt.javaClass.getMethod("refresh")?.let { localMethod ->
                    var done = false
                    var nTry = 0
                    while (!done && nTry < MAX_REFRESH_DEVICE_CACHE_TRY) {
                        done = localMethod.invoke(gatt) as Boolean
                        nTry++
                    }
                    Log.d(TAG, "Refreshing Device Cache: $done")
                }
            }
        }
    }

    override fun getCharacteristic(
        serviceUid: String,
        characteristicUid: String
    ): BluetoothGattCharacteristic? {

        val gatt = deviceStateFlow.value.deviceGatt ?: return null
        val service = gatt.getService(UUID.fromString(serviceUid)) ?: return null
        return service.getCharacteristic(UUID.fromString(characteristicUid))
    }

    @SuppressLint("MissingPermission")
    override fun requestLowerConnectionInterval(): Boolean {
        val node = deviceStateFlow.value
        return node.deviceGatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
            ?: return false
    }

    private fun buildNodeState(newState: NodeState): ConnectionStatus {

        val node = deviceStateFlow.value
        if (node.connectionStatus.current != newState) {
            val prevState = node.connectionStatus.current
            return ConnectionStatus(prevState, newState)
        }

        return ConnectionStatus(node.connectionStatus.prev, node.connectionStatus.current)
    }

    private suspend fun enableBLEServiceChangedIndications(): Boolean {
        return setCharacteristicNotification(
            SERVICE_CHANGED_SERVICE_UUID.toString(),
            SERVICE_CHANGED_CHAR_UUID.toString(),
            true
        )
    }

    private suspend fun <T> Mutex.enqueueWithTimeout(
        delay: Long = 5,
        timeout: Long = DEFAULT_GATT_TIMEOUT,
        defaultValue: T,
        block: suspend CoroutineScope.() -> T
    ): T {
        delay(delay)
        withLock {
            return try {
                withTimeout(timeout, block)
            } catch (e: Exception) {
                Log.e(TAG, e.stackTraceToString())
                defaultValue
            }
        }
    }
}