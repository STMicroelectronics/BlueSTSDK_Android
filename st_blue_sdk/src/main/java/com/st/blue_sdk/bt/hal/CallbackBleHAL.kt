/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.hal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.st.blue_sdk.bt.BleBondingBroadcastReceiver
import com.st.blue_sdk.bt.BleOperation
import com.st.blue_sdk.bt.BleOperationQueue
import com.st.blue_sdk.bt.advertise.BleAdvertiseInfo
import com.st.blue_sdk.bt.gatt.CallbackGattBridge
import com.st.blue_sdk.bt.hal.BleHal.Companion.NOTIFICATION_UUID
import com.st.blue_sdk.bt.hal.BleHal.Companion.SERVICE_CHANGED_CHAR_UUID
import com.st.blue_sdk.bt.hal.BleHal.Companion.SERVICE_CHANGED_SERVICE_UUID
import com.st.blue_sdk.bt.hal.BleHal.Companion.TAG
import com.st.blue_sdk.models.BleNotification
import com.st.blue_sdk.models.ChunkProgress
import com.st.blue_sdk.models.ConnectionStatus
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.models.NodeState
import com.st.blue_sdk.models.RssiData
import com.st.blue_sdk.utils.hasBluetoothPermission
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.util.Date
import java.util.UUID

class CallbackBleHAL(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val device: BluetoothDevice,
    advertiseInfo: BleAdvertiseInfo? = null,
    private val gattBridge: CallbackGattBridge = CallbackGattBridge()
) : BleHal {

    /**
     * Queue used to synchronize ble gatt read/write operations
     * */
    private val bleOperationsQueue = BleOperationQueue(coroutineScope)

    /**
     * Flow to notify node status changes
     * */
    private val deviceStateFlow =
        MutableStateFlow(Node(device = device, advertiseInfo = advertiseInfo))

    /**
     * Flow to notify BLE characteristic changes
     * */
    private val notificationsFlow = MutableSharedFlow<BleNotification>(
        replay = 0,
        extraBufferCapacity = 30,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Flow to notify chunk progression
     * */
    private val chunkStateFlow = MutableStateFlow(ChunkProgress())

    private var discoverServicesJob: Job? = null

    private var serviceDiscoveryContinuation: CancellableContinuation<List<BluetoothGattService>>? =
        null

    private var characteristicReadContinuation: CancellableContinuation<ByteArray?>? = null

    private var characteristicWriteContinuation: CancellableContinuation<Boolean>? = null

    private var characteristicDescriptorWriteContinuation: CancellableContinuation<Boolean>? = null

    private var changeMTUContinuation: CancellableContinuation<Int>? = null

    private var userAskToDisconnect: Boolean = false

    private var bondingBroadcastReceiver: BleBondingBroadcastReceiver? = null

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
        bleOperationsQueue.start()

        val gatt = device.connectGatt(
            context,
            autoConnect,
            gattBridge.getBleGattCallback()
        )
        val btDevice = node.copy(
            device = device,
            deviceGatt = gatt,
            connectionStatus = buildNodeState(NodeState.Connecting)
        )

        Log.d(TAG, "Connecting to node with address: ${btDevice.device.address}")
        deviceStateFlow.update { btDevice }

        return deviceStateFlow
    }

    override fun getDevice() = deviceStateFlow.value

    override fun getDeviceStatus() = deviceStateFlow

    override fun getDeviceNotifications(): SharedFlow<BleNotification> = notificationsFlow
    override fun getChunkProgressUpdates(): Flow<ChunkProgress> = chunkStateFlow

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
        return bleOperationsQueue.enqueueOperation<List<BluetoothGattService>>(BleOperation.DiscoverServices {
            executeAsyncBleOperation(
                defaultValue = emptyList(),
                timeout = 15000L,
                operation = {
                    serviceDiscoveryContinuation?.cancel()
                    serviceDiscoveryContinuation = it
                    node.deviceGatt.discoverServices()
                },
                onOperationTimeOut = {
                    serviceDiscoveryContinuation?.cancel()
                    disconnect()
                }
            ) ?: emptyList()
        }).await() ?: emptyList()
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

        return bleOperationsQueue.enqueueOperation(BleOperation.ReadCharacteristic {

            Log.d(TAG, "Characteristic ${characteristic.uuid} read in progress")

            executeAsyncBleOperation<ByteArray?>(
                defaultValue = null,
                timeout = timeout,
                operation = {
                    characteristicReadContinuation?.cancel()
                    characteristicReadContinuation = it
                    node.deviceGatt.readCharacteristic(characteristic)
                },
                onOperationTimeOut = { characteristicReadContinuation?.cancel() }
            )
        }).await()
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
            val characteristic = getCharacteristic(serviceUid, characteristicUid) ?: return false
            writeCharacteristic(
                characteristic,
                data,
                payloadSize,
                timeout,
                awaitFeedback
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

        chunkStateFlow.emit(
            value = ChunkProgress(
                total = chunks.size,
                current = 0
            )
        )

        chunks.forEachIndexed { index, chunk ->
            successful =
                successful && bleOperationsQueue.enqueueOperation(BleOperation.WriteCharacteristic {

                    characteristic.value = chunk
                    Log.d(
                        TAG,
                        "Writing characteristic ${characteristic.uuid} with data ${chunk.contentToString()}"
                    )

                    if (awaitFeedback.not()) {
                        return@WriteCharacteristic node.deviceGatt.writeCharacteristic(
                            characteristic
                        )
                    }

                    executeAsyncBleOperation(
                        defaultValue = false,
                        timeout = timeout,
                        operation = {
                            characteristicWriteContinuation?.cancel()
                            characteristicWriteContinuation = it
                            node.deviceGatt.writeCharacteristic(characteristic)

                            chunkStateFlow.tryEmit(
                                value = ChunkProgress(
                                    total = chunks.size,
                                    current = index + 1
                                )
                            )
                        },
                        onOperationTimeOut = { characteristicWriteContinuation?.cancel() }
                    ) ?: false
                }).await() ?: false

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

        return bleOperationsQueue.enqueueOperation(BleOperation.WriteDescriptor {

            Log.d(TAG, "Writing characteristic ${characteristic.uuid} descriptor")

            if (awaitFeedback.not()) {
                return@WriteDescriptor writeCharacteristicNotificationDescriptor(
                    node.deviceGatt,
                    enabled,
                    characteristic
                )
            }

            executeAsyncBleOperation(false, timeout, operation = {
                characteristicDescriptorWriteContinuation?.cancel()
                characteristicDescriptorWriteContinuation = it
                writeCharacteristicNotificationDescriptor(node.deviceGatt, enabled, characteristic)
            }, onOperationTimeOut = {
                characteristicDescriptorWriteContinuation?.cancel()
            })!!
        }).await() ?: false
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

        return bleOperationsQueue.enqueueOperation(BleOperation.ChangeMTU {

            val mtu = maxPayloadSize + 3
            Log.d(TAG, "Request to set MTU to $mtu")

            executeAsyncBleOperation(
                defaultValue = node.mtu,
                timeout = timeout,
                operation = {
                    changeMTUContinuation?.cancel()
                    changeMTUContinuation = it
                    node.deviceGatt.requestMtu(mtu)
                }, onOperationTimeOut = {
                    changeMTUContinuation?.cancel()
                })
        }).await() ?: node.maxPayloadSize
    }

    @SuppressLint("MissingPermission")
    override fun getRssi() {
        val node = deviceStateFlow.value
        node.deviceGatt?.readRemoteRssi()
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

        bleOperationsQueue.stop()

        cancelServiceDiscovery()
        detachGattBridgeListeners()
        clearCache()

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

        gattBridge.setConnectionListener { connected ->
            val newState = when {
                connected && isPairing().not() -> NodeState.Connected // connected to ble device, can discover services
                connected && isPairing() -> NodeState.Connecting // in pairing, cannot discover services
                else -> NodeState.Disconnected
            }

            updateNodeConnectionState(newState)
        }

        gattBridge.setServiceDiscoveryListener { services ->
            deviceStateFlow.update {
                deviceStateFlow.value.copy(
                    connectionStatus = buildNodeState(
                        NodeState.ServicesDiscovered
                    )
                )
            }
            enableBLEServiceChangedIndications()
            serviceDiscoveryContinuation?.resume(services ?: emptyList(), null)
        }

        gattBridge.setReadListener { characteristic, successful ->
            val data = if (successful) characteristic.value else byteArrayOf()
            characteristicReadContinuation?.resume(data, null)
        }

        gattBridge.setWriteListener { _, successful ->
            characteristicWriteContinuation?.resume(successful, null)
        }

        gattBridge.setNotificationListener {
            it?.let {
                Log.d(
                    TAG,
                    "received notification on characteristic ${it.uuid} with data ${it.value.contentToString()}"
                )
                notificationsFlow.tryEmit(BleNotification(it, it.value))
            }
        }

        gattBridge.setDescriptorWriteListener { _, successful ->
            characteristicDescriptorWriteContinuation?.resume(successful, null)
        }

        gattBridge.setRssiListener { rssi ->
            val node = deviceStateFlow.value
            deviceStateFlow.update {
                node.copy(rssi = RssiData(rssi, Date()))
            }
        }

        gattBridge.setMtuListener { mtu ->
            val updatedNode = deviceStateFlow.value.copy(mtu = mtu)
            //FixLP
            updatedNode.maxPayloadSize = mtu-3
            deviceStateFlow.update { updatedNode }
            changeMTUContinuation?.resume(updatedNode.maxPayloadSize, null)
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

    private fun clearCache(){
        runCatching {
            deviceStateFlow.value.deviceGatt?.let { gatt ->
                gatt.javaClass.getMethod("refresh")?.let { localMethod ->
                    var done = false
                    var nTry = 0
                    while (!done && nTry < BleHal.MAX_REFRESH_DEVICE_CACHE_TRY) {
                        done = localMethod.invoke(gatt) as Boolean
                        nTry++
                    }
                    Log.d(TAG, "Refreshing Device Cache: $done")
                }
            }
        }
    }

    private fun detachGattBridgeListeners() {

        gattBridge.detachListeners()

        serviceDiscoveryContinuation?.cancel()
        serviceDiscoveryContinuation = null

        characteristicReadContinuation?.cancel()
        characteristicReadContinuation = null

        characteristicWriteContinuation?.cancel()
        characteristicWriteContinuation = null

        characteristicDescriptorWriteContinuation?.cancel()
        characteristicDescriptorWriteContinuation = null

        context.unregisterReceiver(bondingBroadcastReceiver)
        bondingBroadcastReceiver = null
    }

    private fun cancelServiceDiscovery() {
        discoverServicesJob?.cancel()
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

    private fun enableBLEServiceChangedIndications() {

        val gatt = deviceStateFlow.value.deviceGatt ?: return
        val service = gatt.getService(SERVICE_CHANGED_SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(SERVICE_CHANGED_CHAR_UUID) ?: return
        writeCharacteristicNotificationDescriptor(gatt, true, characteristic)
    }

    private suspend inline fun <T> executeAsyncBleOperation(
        defaultValue: T?,
        timeout: Long = 5000L,
        crossinline operation: (CancellableContinuation<T>) -> Unit,
        crossinline onOperationTimeOut: () -> Unit,
    ): T? {

        if (isConnected().not()) {
            return defaultValue
        }

        return try {
            withTimeout(timeout) {
                suspendCancellableCoroutine { continuation ->
                    operation(continuation)
                }
            }
        } catch (e: TimeoutCancellationException) {
            onOperationTimeOut()
            defaultValue
        }
    }
}
