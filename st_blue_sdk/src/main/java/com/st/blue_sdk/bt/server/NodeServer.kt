/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.server

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.util.Log
import com.st.blue_sdk.features.exported.ExportedAudioOpusConfFeature
import com.st.blue_sdk.features.exported.ExportedAudioOpusMusicFeature
import com.st.blue_sdk.features.exported.ExportedFeature
import com.st.blue_sdk.utils.buildNotifiableChar
import com.st.blue_sdk.utils.hasBluetoothPermission
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*

/**
 * TODO: Split in NodeServer to BleServer (with ctx, bleManager and device) and NodeServer (with bleServer and features)
 */
class NodeServer(
    private val context: Context,
    private val bleManager: BluetoothManager,
    private val device: BluetoothDevice,
    featuresByService: Map<UUID, List<ExportedFeature>>
) {

    private val exportedServicesWithFeatures = featuresByService.toMutableMap()

    companion object {

        //Client Characteristic Config Descriptor
        val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val TAG = NodeServer::class.java.canonicalName
    }

    private var gattServer: BluetoothGattServer? = null

    private val services = mutableMapOf<UUID, BluetoothGattService>()

    private val nodeEventsSharedFlow = MutableSharedFlow<NodeEvents>(
        replay = 0,
        extraBufferCapacity = 30,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /// current mtu used during the connection (max notification length will be currentMtu-3)
    var currentMtu: Int = 23
        private set

    /// number of payload bytes that can be sent in a single notification package
    val maxPayloadSize
        get() = currentMtu - 3

    /// true if the client required to enable the 2M phy (BLE 5)
    var isLe2MPhySupported = false
        private set

    var isDeviceConnected: Boolean = false
        private set

    init {
        exportedServicesWithFeatures.forEach { serviceWithFeatures ->
            val serviceUuid = serviceWithFeatures.key
            val features = serviceWithFeatures.value

            val service =
                BluetoothGattService(serviceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)

            features.forEach { exportedFeature ->
                exportedFeature.uuid.buildNotifiableChar(CCCD_UUID).let { characteristic ->
                    service.addCharacteristic(characteristic)
                }
            }

            services[service.uuid] = service
        }

        initGattServer()
    }

    @SuppressLint("MissingPermission")
    private fun initGattServer() {

        if (context.hasBluetoothPermission().not()) {
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        gattServer = bleManager.openGattServer(context, ServerGattCallback())
        //services.forEach { gattServer?.addService(it.value) }
        services.values.forEach { it->
            gattServer?.addService(it) }
    }

    @SuppressLint("MissingPermission")
    fun connectToPeripheral(): Boolean {

        if (isDeviceConnected) {
            return true
        }

        if (context.hasBluetoothPermission().not()) {
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        return gattServer?.connect(device, true) ?: false
    }

    @SuppressLint("MissingPermission")
    fun disconnectFromPeripheral() {

        if (isDeviceConnected.not()) {
            return
        }

        isDeviceConnected = false

        if (context.hasBluetoothPermission().not()) {
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        gattServer?.clearServices()
        bleManager.getConnectedDevices(BluetoothProfile.GATT_SERVER)?.forEach {
            gattServer?.cancelConnection(it)
        }
        gattServer?.close()
    }

    /**
     * Send a BLE notification to a specific BLE Characteristic identified by the passed feature class
     * @param feature Feature Class identifying the chosen BLE Characteristic
     * @param data data to write updating the characteristic
     */
    @SuppressLint("MissingPermission")
    fun notifyData(
        feature: String,
        dataChunks: ByteArray
    ): Boolean {
        exportedServicesWithFeatures.forEach { entry ->
            val (service, features) = entry

            features.find { it.name == feature && it.isEnabled }?.uuid?.let { featureUuid ->
                services[service]?.getCharacteristic(featureUuid)?.let { characteristic ->
                    characteristic.value = dataChunks

                    val hasWriteSuccessfully =
                        gattServer?.notifyCharacteristicChanged(device, characteristic, false)
                            ?: false

                    if (hasWriteSuccessfully.not())
                        return false

                    return true
                }
            }
        }
        return false
    }

    fun isEnabled() =
        exportedServicesWithFeatures.flatMap { it.value }.find { it.isEnabled } != null

    fun isMusicEnable(): Boolean {
        val feature = exportedServicesWithFeatures.flatMap { it.value }.find {it.name == ExportedAudioOpusMusicFeature.NAME}
        return feature?.isEnabled ?: false
    }

    fun isFullDuplexEnable(): Boolean {
        val feature = exportedServicesWithFeatures.flatMap { it.value }.find {it.name == ExportedAudioOpusConfFeature.NAME}
        return feature?.isEnabled ?: false
    }
    private inner class ServerGattCallback : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(
            device: BluetoothDevice?,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(device, status, newState)
            Log.d(TAG, "Gatt server connection state changed: $newState")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                isDeviceConnected = newState == BluetoothGattServer.STATE_CONNECTED
                nodeEventsSharedFlow.tryEmit(
                    if (isDeviceConnected)
                        NodeEvents.NodeConnectionEvent(this@NodeServer)
                    else
                        NodeEvents.NodeDisconnectionEvent(this@NodeServer)
                )
            }
        }

        override fun onPhyUpdate(
            device: BluetoothDevice?,
            txPhy: Int,
            rxPhy: Int,
            status: Int
        ) {
            super.onPhyUpdate(device, txPhy, rxPhy, status)
            Log.e(TAG, "New txPhy is: $txPhy")
            Log.e(TAG, "New rxPhy is: $rxPhy")
            isLe2MPhySupported = (txPhy == 2 && rxPhy == 2)
            nodeEventsSharedFlow.tryEmit(
                NodeEvents.PhyUpdateEvent(
                    this@NodeServer,
                    isLe2MPhySupported
                )
            )
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
            Log.e(TAG, "New Server mtu is: $mtu")
            currentMtu = mtu
            nodeEventsSharedFlow.tryEmit(NodeEvents.MtuUpdateEvent(this@NodeServer, currentMtu))
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )

            val bleGattChar = descriptor.characteristic

                Log.d(TAG, "Descriptor write request on characteristic: ${bleGattChar.uuid}")

                val bleService = bleGattChar.service.uuid
                val bleCharacteristic = bleGattChar.uuid
                val isEnabled =
                    value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)

                if (exportedServicesWithFeatures.containsKey(bleService)) {
                    val features = exportedServicesWithFeatures[bleService] ?: emptyList()

                    if (features.mapNotNull { it.uuid }.contains(bleCharacteristic)) {
                        val oldOne =
                            exportedServicesWithFeatures.remove(bleService) ?: emptyList()
                        val newOne = mutableListOf<ExportedFeature>()
                        oldOne.forEach {
                            if (it.uuid == bleCharacteristic) {
                                newOne.add(it.copy(isEnabled))
                            } else {
                                newOne.add(it)
                            }
                        }
                        exportedServicesWithFeatures[bleService] = newOne
                    }
                }

                nodeEventsSharedFlow.tryEmit(
                    NodeEvents.NotificationStateChange(
                        bleGattChar.uuid,
                        isEnabled
                    )
                )


            if (responseNeeded) {
                gattServer!!.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    value
                )
            }
        }
    }
}