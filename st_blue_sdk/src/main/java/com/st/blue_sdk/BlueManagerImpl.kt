/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.st.blue_sdk.board_catalog.BoardCatalogRepo
import com.st.blue_sdk.board_catalog.models.BleCharacteristic
import com.st.blue_sdk.board_catalog.models.BoardDescription
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.board_catalog.models.DtmiModel
import com.st.blue_sdk.board_catalog.models.Sensor
import com.st.blue_sdk.bt.advertise.AdvertiseFilter
import com.st.blue_sdk.bt.advertise.BlueNRGAdvertiseFilter
import com.st.blue_sdk.bt.advertise.BlueSTSDKAdvertiseFilter
import com.st.blue_sdk.bt.advertise.getFwInfo
import com.st.blue_sdk.common.Resource
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.exported.ExportedAudioOpusConfFeature
import com.st.blue_sdk.features.exported.ExportedAudioOpusMusicFeature
import com.st.blue_sdk.features.exported.ExportedAudioOpusVoiceFeature
import com.st.blue_sdk.features.exported.ExportedFeature
import com.st.blue_sdk.logger.Logger
import com.st.blue_sdk.models.ChunkProgress
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.services.NodeServerConsumer
import com.st.blue_sdk.services.NodeServerProducer
import com.st.blue_sdk.services.NodeServiceConsumer
import com.st.blue_sdk.services.NodeServiceProducer
import com.st.blue_sdk.services.debug.DebugMessage
import com.st.blue_sdk.services.fw_version.FwVersionBoard
import com.st.blue_sdk.services.ota.FwConsole
import com.st.blue_sdk.services.ota.OtaService
import com.st.blue_sdk.services.ota.UpgradeStrategy
import com.st.blue_sdk.utils.hasBluetoothPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BlueManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bleManager: BluetoothManager,
    private val catalog: BoardCatalogRepo,
    private val nodeServiceConsumer: NodeServiceConsumer,
    private val nodeServiceProducer: NodeServiceProducer,
    private val nodeServerConsumer: NodeServerConsumer,
    private val nodeServerProducer: NodeServerProducer,
    private val otaService: OtaService
) : BlueManager {

    private var serverWasEnable=true

    companion object {
        private val TAG = BlueManager::class.java.simpleName
        private const val EXPORTED_SERVICE = "00000000-0001-11e1-9ab4-0002a5d5c51b"
        private val EXPORTED_MAP: MutableMap<UUID, List<ExportedFeature>> = mutableMapOf()
    }

    init {
        EXPORTED_MAP[UUID.fromString(EXPORTED_SERVICE)] = listOf(
            ExportedAudioOpusVoiceFeature(), ExportedAudioOpusConfFeature(), ExportedAudioOpusMusicFeature()
        )
    }

    private val filters: List<AdvertiseFilter> = listOf(
        BlueSTSDKAdvertiseFilter(), BlueNRGAdvertiseFilter()
    )
    private var stopDeviceScan = false
    private val scanPeriod = 10000L

    override fun getAllLoggers(nodeId: String): List<Logger> {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getAllLoggers()
    }

    override fun anyFeatures(nodeId: String, features: List<String>): Boolean {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getNodeFeatures().any {
            features.contains(it.name)
        }
    }

    override fun allFeatures(nodeId: String, features: List<String>): Boolean {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getNodeFeatures().filter {
            features.contains(it.name)
        }.distinctBy { it.name }.size == features.distinct().size
    }

    override fun addAllLoggers(loggers: List<Logger>) {
        nodeServiceConsumer.getNodeServices().forEach { it.addLoggers(loggers) }
    }

    override fun disableAllLoggers(nodeId: String?, loggerTags: List<String>) {
        if (nodeId == null) {
            nodeServiceConsumer.getNodeServices().forEach { it.disableAllLoggers(loggerTags) }
        } else {
            nodeServiceConsumer.getNodeService(nodeId)?.let { service ->
                service.disableAllLoggers(loggerTags)
            }
        }
    }

    override fun clearAllLoggers(nodeId: String?, loggerTags: List<String>) {
        if (nodeId == null) {
            nodeServiceConsumer.getNodeServices().forEach { it.clearAllLoggers(loggerTags) }
        } else {
            nodeServiceConsumer.getNodeService(nodeId)?.let { service ->
                service.clearAllLoggers(loggerTags)
            }
        }
    }

    override fun enableAllLoggers(nodeId: String?, loggerTags: List<String>) {
        if (nodeId == null) {
            nodeServiceConsumer.getNodeServices().forEach { it.enableAllLoggers(loggerTags) }
        } else {
            nodeServiceConsumer.getNodeService(nodeId)?.let { service ->
                service.enableAllLoggers(loggerTags)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun scanNodes(): Flow<Resource<List<Node>>> = callbackFlow {

        if (context.hasBluetoothPermission().not()) {
            throw IllegalStateException("Missing BlueTooth Permissions")
        }

        stopDeviceScan = false
        nodeServiceProducer.clear()

        val bleScanner = bleManager.adapter.bluetoothLeScanner

        trySend(Resource.loading())

        val scanCallback = object : ScanCallback() {
            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                bleScanner.stopScan(this)
                trySend(
                    Resource.error(
                        R.string.blue_st_sdk_error_ble_scan_failed, errorCode, null
                    )
                )
                close()
            }

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                result?.let {
                    if (stopDeviceScan.not()) {
                        if (createOrUpdateDeviceToCollection(filters, it)) {
                            val services = nodeServiceConsumer.getNodeServices()

                            launch {
                                trySend(
                                    Resource.loading(getNodes(services))
                                )
                            }
                        }
                    }
                }
            }
        }

        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        bleScanner.startScan(listOf(), scanSettings, scanCallback)

        launch {
            withTimeoutOrNull(scanPeriod) {
                while (stopDeviceScan.not()) {
                    delay(200)
                }
            }

            trySend(
                Resource.success(getNodes(nodeServiceConsumer.getNodeServices()))
            )

            close()
        }

        awaitClose {
            bleScanner.stopScan(scanCallback)
        }

    }.flowOn(Dispatchers.IO).catch { e ->
        Log.e(TAG, "scan exception", e)
        emit(Resource.error(R.string.blue_st_sdk_error_ble_scan_failed, data = null))
    }

    private suspend fun getNodes(services: List<NodeService>): List<Node> =
        services.map { service -> getFirmwareInfo(service) }

    private suspend fun getFirmwareInfo(nodeService: NodeService): Node {
        val node = nodeService.getNode()

        return getBoardFirmware(nodeService)?.let { catalogInfo ->
            val fwCompatibleList = catalog.getFwCompatible(deviceId = catalogInfo.bleDevId)
                .filter { (it.fwName != catalogInfo.fwName) || (it.fwVersion != catalogInfo.fwVersion) }
                .filter { it.fota.bootloaderType == catalogInfo.fota.bootloaderType }
                .filter { !it.fota.fwUrl.isNullOrEmpty() }.sortedBy { it.fwName }

            //List of fws update ordered by fw version
            val listOfFwUpdate = catalog.getFw(
                deviceId = catalogInfo.bleDevId, fwName = catalogInfo.fwName
            ).filter { it.fota.fwUrl != null }.filter { it.fwVersion > catalogInfo.fwVersion }.sortedBy { it.fwVersion }

            //Search if there is a mandatory update
            val fwMandatory = listOfFwUpdate.firstOrNull { it.fota.mandatory==true }

            //the update will be the mandatory one, or the latest available
            val fwUpdate = fwMandatory ?: listOfFwUpdate.maxByOrNull { it.fwVersion }

//            val fwUpdate = catalog.getFw(
//                deviceId = catalogInfo.bleDevId, fwName = catalogInfo.fwName
//            ).filter { it.fota.fwUrl != null }.filter { it.fwVersion > catalogInfo.fwVersion }
//                .minByOrNull { it.fwVersion }

            node.copy(
                catalogInfo = catalogInfo, fwUpdate = fwUpdate, fwCompatibleList = fwCompatibleList
            )
        } ?: node
    }

    override suspend fun getNodeWithFirmwareInfo(nodeId: String): Node {
        val nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: throw IllegalStateException(
            "Unable to find NodeService for $nodeId"
        )

        return getFirmwareInfo(nodeService)
    }

    override fun stopScan() {
        stopDeviceScan = true
    }

    @Synchronized
    private fun createOrUpdateDeviceToCollection(
        filters: List<AdvertiseFilter>, scanResult: ScanResult
    ): Boolean {

        val advertisingData = scanResult.scanRecord ?: return false

        if (filters.isNotEmpty()) {
            val advertiseInfo =
                filters.asSequence().map { it.decodeAdvertiseData(advertisingData.bytes) }
                    .filter { advInfo -> advInfo != null }.firstOrNull() ?: return false

            val nodeId = scanResult.device.address

            if (hasService(deviceAddress = nodeId).not()) {
                nodeServiceProducer.createService(
                    scanResult = scanResult,
                    advertiseInfo = advertiseInfo
                )

            } else {
                nodeServiceConsumer.getNodeService(nodeId)?.bleHal?.setRssi(
                    rssi = scanResult.rssi
                )
                nodeServiceConsumer.getNodeService(nodeId)?.bleHal?.setAdvertiseInfo(
                    advertiseInfo = advertiseInfo
                )

                nodeServiceConsumer.getNodeService(nodeId)?.bleHal?.setDeviceInfo(
                    device = scanResult.device
                )
            }

            return true
        }

        return false
    }

    private fun hasService(deviceAddress: String) =
        nodeServiceConsumer.getNodeService(deviceAddress) != null

    @SuppressLint("MissingPermission")
    override fun connectToNode(nodeId: String, maxPayloadSize: Int, enableServer: Boolean): Flow<Node> {
        val nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: throw IllegalStateException(
            "Unable to find NodeService for $nodeId"
        )

        stopScan()

        serverWasEnable = enableServer
        connectFromNode(node = nodeService.bleHal.getDevice())

        return nodeService.connectToNode(autoConnect = false, maxPayloadSize = maxPayloadSize)
    }


    override fun getNode(nodeId: String) = nodeServiceConsumer.getNodeService(nodeId)?.getNode()

    override fun getNodeStatus(nodeId: String) =
        (nodeServiceConsumer.getNodeService(nodeId)?.getDeviceStatus()
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId"))

    override fun getRssi(nodeId: String) {
        (nodeServiceConsumer.getNodeService(nodeId)?.getRssi()
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId"))
    }
    override fun isConnected(nodeId: String): Boolean =
        nodeServiceConsumer.getNodeService(nodeId)?.isConnected()
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

    override fun isReady(nodeId: String): Boolean =
        nodeServiceConsumer.getNodeService(nodeId)?.isReady()
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

    @SuppressLint("MissingPermission")
    override fun disconnect(nodeId: String?) {
        if (nodeId.isNullOrEmpty()) {
            nodeServiceConsumer.getNodeServices().forEach { it.disconnect() }
            nodeServiceProducer.clear()
            disconnectFromNode()
        } else {
            disconnectFromNode(nodeId = nodeId)
            val service = nodeServiceConsumer.getNodeService(nodeId)
            service?.disconnect()
            nodeServiceProducer.removeService(nodeId)
        }
    }

    override fun nodeFeatures(nodeId: String): List<Feature<*>> {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getNodeFeatures()
    }

    override suspend fun enableFeatures(
        nodeId: String, features: List<Feature<*>>,
        onFeaturesEnabled: CoroutineScope.() -> Unit
    ): Boolean {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.setFeaturesNotifications(features = features, true,onFeaturesEnabled)
    }

    override suspend fun disableFeatures(
        nodeId: String, features: List<Feature<*>>
    ): Boolean {
        var result = false
        try {

            val service = nodeServiceConsumer.getNodeService(nodeId)
                //?: throw IllegalStateException("Unable to find NodeService for $nodeId")

            service?.let {
                result = service.setFeaturesNotifications(features = features, false)
            }
        } catch (e: Exception) {
            Log.d(TAG, e.stackTraceToString())
        }
        return result
    }

    override fun getFeatureUpdates(
        nodeId: String,
        features: List<Feature<*>>,
        autoEnable: Boolean,
        onFeaturesEnabled: CoroutineScope.() -> Unit
    ): Flow<FeatureUpdate<*>> {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getFeatureUpdates(features, autoEnable, onFeaturesEnabled)
    }

    override suspend fun readFeature(
        nodeId: String,
        feature: Feature<*>,
        timeout: Long
    ): List<FeatureUpdate<*>> {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.readFeature(
            feature = feature,
            responseTimeout = timeout
        )
    }

    override suspend fun writeDebugMessage(nodeId: String, msg: String): Boolean {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.writeDebugMessage(msg)
    }

    override fun getConfigControlUpdates(nodeId: String): Flow<FeatureResponse> {

        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.getConfigControlUpdates()
    }

    override fun getDebugMessages(nodeId: String): Flow<DebugMessage> {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")
        return service.getDebugMessages()
    }

    override fun getChunkProgressUpdates(nodeId: String): Flow<ChunkProgress> {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")
        return service.getChunkProgressUpdates()
    }

    override suspend fun resetChunkProgressUpdates(nodeId: String) {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")
        service.resetChunkProgressUpdates()
    }

    override suspend fun writeFeatureCommand(
        nodeId: String,
        featureCommand: FeatureCommand,
        responseTimeout: Long,
        retry: Int,
        retryDelay: Long
    ): FeatureResponse? {
        val service = nodeServiceConsumer.getNodeService(nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        return service.writeFeatureCommand(
            featureCommand = featureCommand,
            responseTimeout = responseTimeout,
            retry = retry,
            retryDelay = retryDelay
        )
    }

    override suspend fun getDtmiModel(nodeId: String,isBeta: Boolean): DtmiModel? {
        val nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: return null
        val advInfo = nodeService.getNode().advertiseInfo ?: return null
        return advInfo.getFwInfo()?.let {
            catalog.getDtmiModel(it.deviceId, it.fwId,isBeta)
        }
    }

    override suspend fun getBoardCatalog(): List<BoardFirmware> = catalog.getBoardCatalog()

    override suspend fun getBoardsDescription(): List<BoardDescription>  = catalog.getBoardsDescription()

    override suspend fun reset(url: String?) {catalog.reset(url)}

    override suspend fun setBoardCatalog(
        fileUri: Uri, contentResolver: ContentResolver
    ): List<BoardFirmware> = catalog.setBoardCatalog(fileUri, contentResolver)

    override suspend fun setDtmiModel(
        nodeId: String, fileUri: Uri, contentResolver: ContentResolver
    ): DtmiModel? {
        val nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: return null
        val advInfo = nodeService.getNode().advertiseInfo ?: return null
        return advInfo.getFwInfo()?.let {
            catalog.setDtmiModel(
                deviceId = it.deviceId,
                bleFwId = it.fwId,
                fileUri = fileUri,
                contentResolver = contentResolver
            )
        }
    }

    private suspend fun getBoardFirmware(nodeService: NodeService): BoardFirmware? {
        val advInfo = nodeService.getNode().advertiseInfo ?: return null
        return advInfo.getFwInfo()?.let { catalog.getFwDetailsNode(it.deviceId, it.fwId) }
    }

    override suspend fun getBoardFirmware(nodeId: String): BoardFirmware? {
        val nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: return null
        return getBoardFirmware(nodeService = nodeService)
    }

    override suspend fun getSensorAdapter(uniqueId: Int): Sensor? {
       return catalog.getSensorAdapter(uniqueId=uniqueId)
    }

    override suspend fun getBleCharacteristics() : List<BleCharacteristic> {
        return catalog.getBleCharacteristics()
    }

    override suspend fun upgradeFw(nodeId: String): FwConsole? {
        return otaService.updateFirmware(nodeId)
    }

    override fun getFwUpdateStrategy(nodeId: String): UpgradeStrategy {
        return otaService.getFwUpdateStrategy(nodeId)
    }


    override suspend fun getFwVersion(nodeId: String): FwVersionBoard? {
        return otaService.getFwVersion(nodeId)
    }

    private fun connectFromNode(node: Node): Boolean {
        val server = nodeServerConsumer.getNodeServer(node.device.address)
            ?: nodeServerProducer.createServer(node, EXPORTED_MAP)
        return if(serverWasEnable) {
            server.connectToPeripheral()
        } else {
            false
        }
    }

    private fun disconnectFromNode(nodeId: String? = null): Boolean {
        if (nodeId == null) {
            nodeServerConsumer.getNodeServers().forEach {
                if(serverWasEnable) {
                    it.disconnectFromPeripheral()
                }
            }

            nodeServerProducer.clear()

            return true
        } else {
            nodeServerConsumer.getNodeServer(nodeId)?.let { server ->
                if(serverWasEnable) {
                    server.disconnectFromPeripheral()
                }

                nodeServerProducer.removeServer(nodeId = nodeId)

                return true
            }
        }

        return false
    }
}
