/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.st.blue_sdk.bt.advertise.BleAdvertiseInfo
import com.st.blue_sdk.bt.hal.BleHal
import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtendedFeatureCommand
import com.st.blue_sdk.logger.Loggable
import com.st.blue_sdk.logger.Logger
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.models.NodeState
import com.st.blue_sdk.services.config.ConfigControlService
import com.st.blue_sdk.services.debug.DebugMessage
import com.st.blue_sdk.services.debug.DebugService
import com.st.blue_sdk.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

class NodeService(
    private val coroutineScope: CoroutineScope,
    private val advertiseInfo: BleAdvertiseInfo,
    val bleHal: BleHal,
    private val unwrapTimestamp: UnwrapTimestamp = UnwrapTimestamp(),
    val debugService: DebugService,
    private val configControlService: ConfigControlService,
    private val loggers: MutableSet<Logger>
) {
    companion object {

        private val TAG = NodeService::class.simpleName

        val DEBUG_SERVICE_UUID: UUID = UUID.fromString("00000000-000E-11e1-9ab4-0002a5d5c51b")
        val DEBUG_RW_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00000001-000E-11e1-ac36-0002a5d5c51b")
        val DEBUG_ERROR_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00000002-000E-11e1-ac36-0002a5d5c51b")

        val CONFIGURATION_SERVICE_UUID: UUID =
            UUID.fromString("00000000-000F-11e1-9ab4-0002a5d5c51b")
        val BOARD_CONFIGURATION_RW_CHARACTERISTIC_UID: UUID =
            UUID.fromString("00000001-000F-11e1-ac36-0002a5d5c51b")
        val FEATURE_CONFIGURATION_RW_CHARACTERISTIC_UID: UUID =
            UUID.fromString("00000002-000F-11e1-ac36-0002a5d5c51b")
    }

    private val characteristicWithFeatures = mutableListOf<CharacteristicWithFeatures>()

    private var deviceStatusJob: Job? = null

    @SuppressLint("MissingPermission")
    fun connectToNode(autoConnect: Boolean = false, maxPayloadSize: Int = 248): Flow<Node> {
        deviceStatusJob?.cancel()
        deviceStatusJob = bleHal.getDeviceStatus().onEach {
            Log.d(
                "connectToNode",
                it.connectionStatus.prev.name + "->" + it.connectionStatus.current.name
            )
            when (it.connectionStatus.current) {
                NodeState.ServicesDiscovered -> discoverFeatures()
                NodeState.Ready -> {
                    debugService.init()
                    configControlService.init()
                    //Set the Max possible MTU for WB (251) / BlueNRG-2 (220) / BlueNRG-1 (158)
                    //maxPayloadSize = MTU-3
                    //Log.i("WorkAround","Move it")
                    delay(300)
                    bleHal.requestPayloadSize(maxPayloadSize = maxPayloadSize)
                }
                else -> Unit
            }
        }.launchIn(CoroutineScope(coroutineScope.coroutineContext + Dispatchers.IO))

        return bleHal.connectToDevice(autoConnect = autoConnect)
    }

    fun getConfigControlUpdates(): Flow<FeatureResponse> {
        return configControlService.configControlUpdates.transform { rawData ->
            val mask: Int = NumberConversion.BigEndian.bytesToInt32(rawData, 2)
            characteristicWithFeatures.filter{ it.hasEnabledNotifications}.flatMap { it.features }
                .firstOrNull { f -> mask == f.mask }
                ?.let { feature ->
                    feature.parseCommandResponse(rawData)?.let { response ->
                        emit(response)
                    }
                }
        }
    }

    @SuppressLint("MissingPermission")
    fun getNode(): Node = bleHal.getDevice()

    fun getDeviceStatus() = bleHal.getDeviceStatus()

    fun getChunkProgressUpdates() = bleHal.getChunkProgressUpdates()

    suspend fun resetChunkProgressUpdates()  { bleHal.resetChunkProgressUpdates() }

    fun getRssi() = bleHal.getRssi()

    fun isConnected(): Boolean = bleHal.isConnected()

    fun isReady(): Boolean = bleHal.isReady()

    fun disconnect() {
        deviceStatusJob?.cancel()
        bleHal.disconnect()
    }

    suspend fun writeDebugMessage(message: String): Boolean {
        return debugService.write(message) > 0
    }

    fun getDebugMessages(): Flow<DebugMessage> = debugService.getDebugMessages()

    suspend fun setFeaturesNotifications(
        features: List<Feature<*>>,
        enabled: Boolean,
        onFeaturesEnabled: CoroutineScope.() -> Unit = { /** NOOP **/ }
    ): Boolean {

        var result = true

        val characteristicsWithFeatures = characteristicWithFeatures.filter {
            it.features.any { feature ->
                features.contains(feature)
            }
        }.toSet()

        characteristicsWithFeatures.forEach {
            if(enabled) {
                it.numberEnables++
            } else {
                if(it.numberEnables>0) {
                    it.numberEnables--
                }
            }
            //For Avoiding to do this operation when it's not necessary
            if(it.hasEnabledNotifications!=enabled) {
                //For avoiding to remove the notification on Aggregate Features
                if((!enabled && (it.numberEnables==0)) || enabled) {
                    val operationResult = bleHal.setCharacteristicNotification(
                        serviceUid = it.characteristic.service.uuid.toString(),
                        characteristicUid = it.characteristic.uuid.toString(),
                        enabled = enabled
                    )

                    if (operationResult) {
                        it.hasEnabledNotifications = enabled
                        if(enabled) {
                            coroutineScope.launch {
                                onFeaturesEnabled.invoke(this)
                            }
                        }
                    }

                    result = result && operationResult
                }
            } else {
                if(enabled) {
                    coroutineScope.launch {
                        onFeaturesEnabled.invoke(this)
                    }
                }
            }
        }

        return result
    }

    fun getFeatureUpdates(
        features: List<Feature<*>>,
        autoEnable: Boolean = true,
        onFeaturesEnabled: CoroutineScope.() -> Unit = { /** NOOP **/ }
    ): Flow<FeatureUpdate<*>> {
        val characteristicWithFeatures = characteristicWithFeatures.filter {
            it.features.any { feature ->
                features.contains(feature)
            }
        }.toSet()

        if (autoEnable) {
            coroutineScope.launch {
                characteristicWithFeatures.forEach {
                    if (it.hasEnabledNotifications.not()) {
                        setFeaturesNotifications(it.features, true)
                    }
                }
                onFeaturesEnabled.invoke(this)
            }
        }
        return bleHal.getDeviceNotifications().transform {
            characteristicWithFeatures.find { characteristicWithFeature ->
                characteristicWithFeature.characteristic.uuid == it.characteristic.uuid
            }?.let { characteristicWithFeature ->
                val featureUpdates =
                    extractFeatureUpdates(characteristicWithFeature.features, features, it.data)
                featureUpdates.forEach { emit(it) }
            }
        }.catch { exception ->
            Log.d(TAG, "Exception on featureUpdate", exception)
        }
    }

    fun getNodeFeatures(): List<Feature<*>> = characteristicWithFeatures.flatMap { it.features }

    private fun discoverFeatures(): List<Feature<*>> {

        val featureMap = advertiseInfo.getFeatureMap()
        val protocolVersion = advertiseInfo.getProtocolVersion()

        characteristicWithFeatures.clear()
        val deviceId = advertiseInfo.getDeviceId().toInt()
        val sdkVersion = advertiseInfo.getProtocolVersion().toInt()
        val boardModel: Boards.Model = advertiseInfo.getBoardType()

        val containsRemoteFeatures = Boards.containsRemoteFeatures(deviceId,sdkVersion)

        bleHal.getDiscoveredServices().map { service ->
            service.characteristics.forEach { characteristic ->
                if (characteristic.isExtendedOrExternalFeatureCharacteristics()) {
                    runCatching {
                        characteristicWithFeatures.add(
                            CharacteristicWithFeatures(
                                characteristic = characteristic,
                                features = listOf(characteristic.getFeature())
                            )
                        )
                    }
                }

                if (characteristic.isGeneralPurposeFeatureCharacteristics()) {
                    runCatching {
                        characteristicWithFeatures.add(
                            CharacteristicWithFeatures(
                                characteristic = characteristic,
                                features = listOf(characteristic.getGPFeature())
                            )
                        )
                    }
                }

                if (characteristic.isStandardFeatureCharacteristics()) {
                    runCatching {
                        characteristicWithFeatures.add(
                            CharacteristicWithFeatures(
                                characteristic = characteristic,
                                features = characteristic.buildFeatures(
                                    advertiseMask = featureMap,
                                    protocolVersion = protocolVersion,
                                    boardModel = boardModel,
                                    containsRemoteFeatures = containsRemoteFeatures
                                )
                            )
                        )
                    }
                }
            }
        }

        bleHal.setNodeStatusToReady()

        return getNodeFeatures()
    }

    private fun isGeneralPurpose(uuid: UUID): Boolean {
        return uuid.toString().endsWith("0003-11e1-ac36-0002a5d5c51b")
    }

    suspend fun readFeature(
        feature: Feature<*>,
        responseTimeout: Long = 2000
    ): List<FeatureUpdate<*>> {

        val featureBundle = getFeatureCharacteristic(feature)
            ?: return emptyList() // feature not found, abort write operation

        val data = bleHal.readCharacteristic(featureBundle.characteristic, responseTimeout)
        data?.let {
            return extractFeatureUpdates(
                featureBundle.features,
                listOf(feature),
                featureBundle.characteristic.value
            )
        }

        return emptyList()
    }

    private fun extractFeatureUpdates(
        featureOnCharacteristic: List<Feature<*>>,
        userRequiredFeatures: List<Feature<*>> = emptyList(),
        data: ByteArray
    ): List<FeatureUpdate<*>> {
        val out = mutableListOf<FeatureUpdate<*>>()
        var timestamp: Long = if (data.size >= 2) {
            val ts: Int = NumberConversion.LittleEndian.bytesToUInt16(data)
            unwrapTimestamp.unwrap(ts.toLong())
        } else {
            unwrapTimestamp.next()
        }

        var dataOffset = 2
        featureOnCharacteristic.forEach { feature ->

            if (feature.hasTimeStamp.not()) {
                //if the Features is a Extended one, could not contain the TimeStamp
                dataOffset = 0
                timestamp = System.currentTimeMillis()/10
            }

            val update: FeatureUpdate<out Loggable>
            try {

                update = feature.extractData(
                    timeStamp = timestamp,
                    data = data,
                    dataOffset = dataOffset
                )

                if (feature.type == Feature.Type.STANDARD) {
                    //Only the Standard Features could be packed on a Single BLE Char
                    dataOffset += update.readByte
                }

                loggers.forEach { it.log(node = getNode(), feature = feature, update = update) }

                if (userRequiredFeatures.contains(feature)) {
                    out.add(update)
                }
            } catch (ex: Exception) {
                Log.e(TAG, ex.message, ex)
            }
        }
        return out
    }

    /**
     * Send a command to a feature, the command will be write into the
     * {@link com.st.BlueSTSDK.Utils.BLENodeDefines.Services.Config#FEATURE_COMMAND_UUID}
     * If not present the data will be write into the characteristic that export the feature data.
     * sending command to a general purpose feature is not supported
     * The command format is [feature mask (4byte) + type + data], if the command is send directly
     * to the feature the feature mask is omitted.
     *
     * for the extended feature use the {@link Node#writeFeatureData(Feature, byte[], Runnable)} method
     *
     * @param feature destination feature
     * @param type command type
     * @param data command parameters
     * @return true if the message is correctly send, false otherwise
     */
    suspend fun writeFeatureCommand(
        featureCommand: FeatureCommand,
        writeTimeout: Long = 1000,
        responseTimeout: Long = 2000,
        retry: Int = 0,
        retryDelay: Long = 250
    ): FeatureResponse? {

        val featureBundle = getFeatureCharacteristic(featureCommand.feature)
            ?: return null // feature not found, abort write operation

        if (isGeneralPurpose(featureBundle.characteristic.uuid)) {
            throw IllegalArgumentException("General purpose features cannot send commands")
        }

        val isExtendedFeatureCommand = featureCommand.feature.type == Feature.Type.EXTENDED ||
                featureCommand.feature.type == Feature.Type.EXTERNAL_STM32 ||
                featureCommand.feature.type == Feature.Type.EXTERNAL_BLUE_NRG_OTA

        val feature = featureBundle.features.first { it == featureCommand.feature }

        if (isExtendedFeatureCommand.not()) {
            configControlService.let { configService ->
                return configService.writeFeatureCommand(
                    featureCommand = featureCommand,
                    feature = feature,
                    writeTimeout = writeTimeout,
                    responseTimeout = responseTimeout,
                    retry = retry,
                    retryDelay = retryDelay
                )
            }
        }

        val data = feature.packCommandData(null, featureCommand) ?: return null // cannot pack data
        val hasWriteData = bleHal.writeCharacteristic(
            characteristic = featureBundle.characteristic,
            data = data,
            payloadSize = feature.maxPayloadSize,
            timeout = writeTimeout
        )
        if (hasWriteData.not()) {
            return if (retry > 0) {
                delay(retryDelay)
                writeFeatureCommand(
                    featureCommand = featureCommand,
                    writeTimeout = writeTimeout,
                    responseTimeout = responseTimeout,
                    retry = retry - 1,
                    retryDelay = retryDelay
                )
            } else WriteError(feature, featureCommand.commandId) // write error
        }

        if (isExtendedFeatureCommand.not() || (featureCommand is ExtendedFeatureCommand && featureCommand.hasResponse.not())) { // if is not an extended feature no response will be generated from connected board
            return EmptyResponse(
                feature,
                featureCommand.commandId
            )
        }

        if (responseTimeout <= 0) {
            return null
        }

        var job: Job? = null
        val withTimeoutOrNull = withTimeoutOrNull(responseTimeout) {
            var response: FeatureResponse? = null
            job = coroutineScope.launch {
                bleHal.getDeviceNotifications().collect { bleNotification ->
                    if (bleNotification.characteristic.uuid == featureBundle.characteristic.uuid) {
                        response = feature.parseCommandResponse(bleNotification.data)
                        if (response != null)
                            cancel()
                    }
                }
            }

            job?.join()

            return@withTimeoutOrNull response
        }
        job?.cancel()

        return withTimeoutOrNull
    }

    private fun getFeatureCharacteristic(feature: Feature<*>): CharacteristicWithFeatures? {
        return characteristicWithFeatures
            .filter { characteristicWithFeatures ->
                characteristicWithFeatures.features
                    .any { it == feature }
            }.maxByOrNull { it.features.size }
    }

    fun getAllLoggers(): List<Logger> = loggers.toList()

    fun addLoggers(loggers: List<Logger>) {
        loggers.forEach { logger ->
            this.loggers.find { it.id == logger.id }?.let { oldOneWithSameTag ->
                this.loggers.remove(oldOneWithSameTag)
            }
            this.loggers.add(logger)
        }
    }

    fun disableAllLoggers(loggersTags: List<String>) {
        loggers.filter { loggersTags.contains(it.id) }.forEach { it.isEnabled = false }
    }

    fun enableAllLoggers(loggersTags: List<String>) {
        loggers.filter { loggersTags.contains(it.id) }.forEach { it.isEnabled = true }
    }

    fun clearAllLoggers(loggersTags: List<String>) {
        loggers.filter { loggersTags.contains(it.id) }.forEach { it.clear()}
    }
}

data class CharacteristicWithFeatures(
    val characteristic: BluetoothGattCharacteristic,
    var hasEnabledNotifications: Boolean = false,
    var numberEnables: Int = 0,
    val features: List<Feature<*>> = emptyList()
)
