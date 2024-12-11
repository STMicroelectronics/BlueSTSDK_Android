/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.ota.characteristic

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.st.blue_sdk.NodeService
import com.st.blue_sdk.board_catalog.BoardCatalogRepo
import com.st.blue_sdk.board_catalog.models.BoardFotaType
import com.st.blue_sdk.bt.advertise.BleAdvertiseInfo
import com.st.blue_sdk.bt.advertise.BlueSTSDKAdvertiseFilter
import com.st.blue_sdk.bt.advertise.getFwInfo
import com.st.blue_sdk.features.WriteError
import com.st.blue_sdk.features.ota.stm32wb.OTAControl
import com.st.blue_sdk.features.ota.stm32wb.OTAControl.Companion.START_M0_COMMAND
import com.st.blue_sdk.features.ota.stm32wb.OTAControl.Companion.START_M4_COMMAND
import com.st.blue_sdk.features.ota.stm32wb.OTAFileUpload
import com.st.blue_sdk.features.ota.stm32wb.OTAReboot
import com.st.blue_sdk.features.ota.stm32wb.OTAWillReboot
import com.st.blue_sdk.features.ota.stm32wb.requests.*
import com.st.blue_sdk.features.ota.stm32wb.response.WillRebootInfo
import com.st.blue_sdk.features.ota.stm32wb.response.WillRebootInfoType
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.models.NodeState
import com.st.blue_sdk.services.NodeServiceConsumer
import com.st.blue_sdk.services.NodeServiceProducer
import com.st.blue_sdk.services.ota.*
import com.st.blue_sdk.utils.WbOTAUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.math.min

class CharacteristicFwUpgrade(
    private val bleManager: BluetoothManager,
    private val coroutineScope: CoroutineScope,
    private val nodeServiceConsumer: NodeServiceConsumer,
    private val nodeServiceProducer: NodeServiceProducer,
    private val catalogRepository: BoardCatalogRepo
) : FwConsole {

    companion object {

        private val TAG = CharacteristicFwUpgrade::class.simpleName

        private const val MAX_PAYLOAD_SIZE = 248
        private const val OTA_NODE_ID: Byte = 0x86.toByte()
        private const val DISCONNECTION_TIMEOUT = 20000L
        private const val BLE_SCAN_TIMEOUT = 10000L
        private const val OTA_NODE_READY_TIMEOUT = 10000L
        private const val UPLOAD_RESPONSE_DELAY = 5L
        private const val FINISH_UPLOAD_RESPONSE_DELAY = 250L

        fun buildFwUpgradeParams(
            firmwareType: FirmwareType,
            boardType: WbOTAUtils.WBBoardType,
            fileDescriptor: FwFileDescriptor,
            address: String,
            nbSectorsToErase: String
        ): FwUpgradeParams {

            //val firstSectorToDelete = WbOTAUtils.getFirstSectorToDelete(boardType, firmwareType)

            var sectorCount = nbSectorsToErase.toShortOrNull()

            if(sectorCount == null) {
                sectorCount = WbOTAUtils.getNumberOfSectorsToDelete(
                    boardType,
                    firmwareType,
                    fileDescriptor.getFileSize()
                )
            }

            val address2 = java.lang.Long.decode(address) //WbOTAUtils.getMemoryAddress(boardType)

            val firstSectorToDelete = WbOTAUtils.getFirstSectorToDelete(boardType, address2)

            return FwUpgradeParams.Stm32WbParams(
                offset = firstSectorToDelete.toLong(),
                sectorCount = sectorCount.toByte(),
                address = address2
            )
        }
    }

    private lateinit var nodeService: NodeService

    private lateinit var otaNodeService: NodeService

    private lateinit var fwUpdateListener: FwUpdateListener

    private lateinit var fwType: FirmwareType

    private lateinit var fileDescriptor: FwFileDescriptor

    private lateinit var updateArgs: FwUpgradeParams.Stm32WbParams

    private var wbUpdateJob: Job? = null

    override fun launchFirmwareUpgrade(
        nodeId: String,
        fwType: FirmwareType,
        fileDescriptor: FwFileDescriptor,
        params: FwUpgradeParams?,
        fwUpdateListener: FwUpdateListener
    ): Boolean {

        if (params !is FwUpgradeParams.Stm32WbParams) {
            return false
        }

        this.nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: return false

        this.fwUpdateListener = fwUpdateListener
        this.fwType = fwType
        this.fileDescriptor = fileDescriptor
        this.updateArgs = params

        coroutineScope.launch {
            if (isOTAMode(nodeService.getNode()).not()) {
                rebootToOTAMode()
            } else {
                otaNodeService = nodeService
                wbUpdateJob = coroutineScope.launch { writeOTAFile() }
            }
        }

        return true
    }

    private suspend fun rebootToOTAMode() {

        val rebootFeature = nodeService.getNodeFeatures()
            .firstOrNull { it.name == OTAReboot.NAME }

        if (rebootFeature == null) {
            fwUpdateListener.onError(FwUploadError.ERROR_UNKNOWN)
            return
        }

        coroutineScope.launch {
            try {
                withTimeout(DISCONNECTION_TIMEOUT) {
                    nodeService.getDeviceStatus().firstOrNull {
                        it.connectionStatus.current == NodeState.Disconnected
                    }
                    reconnectToOTADevice(nodeService.getNode().device.address)
                }
            } catch (e: TimeoutCancellationException) {
                fwUpdateListener.onError(FwUploadError.ERROR_UNKNOWN)
            }
        }

        val rebootRequest = RebootToOTAMode(
            feature = rebootFeature,
            sectorOffset = updateArgs.offset.toByte(),
            numSector = updateArgs.sectorCount
        )

        nodeService.writeFeatureCommand(
            featureCommand = rebootRequest,
            responseTimeout = UPLOAD_RESPONSE_DELAY
        )

        delay(200)
        nodeService.disconnect()
    }

    private suspend fun reconnectToOTADevice(address: String) {

        wbUpdateJob = coroutineScope.launch otaLaunch@{

            val lastAddressDigit: Int = address.substring(address.length - 2).toInt(16)
            val otaAddress =
                address.substring(0, address.length - 2) + String.format(
                    "%02X",
                    (lastAddressDigit + 1)
                )

            Log.d(TAG, "Search board with address: $otaAddress, old address was: $address")

            val synchronizationChannel = Channel<Unit>()

            // blocking call, wait for bleDevice to be discovered
            val resultPair = scanForOTADevices(otaAddress)
            if (resultPair == null) {
                cancelOTAJob(FwUploadError.ERROR_UNKNOWN, "OTA board not found")
                return@otaLaunch
            }

            Log.d(TAG, "OTA board found")
            otaNodeService = nodeServiceProducer.createService(resultPair.first, resultPair.second)

            if (isOTAMode(otaNodeService.getNode()).not()) {
                cancelOTAJob(
                    FwUploadError.ERROR_UNKNOWN,
                    "Discovered board is not in OTA mode"
                )
                return@otaLaunch
            }

            launch {
                try {
                    withTimeout(OTA_NODE_READY_TIMEOUT) {
                        otaNodeService.getDeviceStatus().firstOrNull {
                            Log.d(TAG, "OTA board status : ${it.connectionStatus.current}")
                            it.connectionStatus.current == NodeState.Ready
                        }
                        synchronizationChannel.send(Unit)
                    }
                } catch (e: Exception) {
                    cancelOTAJob(FwUploadError.ERROR_UNKNOWN, "Ota node not ready")
                }
            }
            otaNodeService.connectToNode(false)

            synchronizationChannel.receive() // block until ready to communicate with node
            writeOTAFile()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun scanForOTADevices(targetDeviceAddress: String): Pair<ScanResult, BleAdvertiseInfo>? {
        return withTimeoutOrNull(BLE_SCAN_TIMEOUT) {

            val advertiseFilter = BlueSTSDKAdvertiseFilter()

            suspendCancellableCoroutine { continuation ->
                val scanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult?) {
                        super.onScanResult(callbackType, result)

                        result?.let { scanResult ->
                            Log.d(TAG, "Found device with address: ${result.device.address}")
                            if (scanResult.device.address == targetDeviceAddress) {
                                advertiseFilter.decodeAdvertiseData(
                                    scanResult.scanRecord?.bytes ?: byteArrayOf()
                                )?.let { advInfo ->
                                    bleManager.adapter.bluetoothLeScanner.stopScan(this)
                                    if (continuation.isCompleted.not()) {
                                        continuation.resume(Pair(scanResult, advInfo))
                                    }
                                }
                            }
                        }
                    }
                }

                bleManager.adapter.bluetoothLeScanner.startScan(scanCallback)

                continuation.invokeOnCancellation {
                    bleManager.adapter.bluetoothLeScanner.stopScan(scanCallback)
                }
            }
        }
    }

    private fun cancelOTAJob(error: FwUploadError, logMessage: String) {
        Log.d(TAG, logMessage)
        fwUpdateListener.onError(error)
        wbUpdateJob?.cancel()
        wbUpdateJob = null
    }

    private suspend fun writeOTAFile() {
        supervisorScope {
            val isWBAProtocol = (otaNodeService.getNode().familyType == Boards.Family.WBA_FAMILY || otaNodeService.getNode().boardType == Boards.Model.WB0X_NUCLEO_BOARD)

            val payloadSize =
                otaNodeService.bleHal.requestPayloadSize(maxPayloadSize = MAX_PAYLOAD_SIZE)
            Log.d(TAG, "max payload size is $payloadSize")

            val otaWillRebootFeature =
                otaNodeService.getNodeFeatures()
                    .firstOrNull { it.name == OTAWillReboot.NAME }
            if (otaWillRebootFeature == null) {
                cancelOTAJob(
                    FwUploadError.ERROR_UNKNOWN,
                    "Discovered board doesn't have willReboot feature"
                )
                return@supervisorScope
            }

            val subscriptionResult =
                otaNodeService.setFeaturesNotifications(listOf(otaWillRebootFeature), true)
            if (subscriptionResult.not()) {
                cancelOTAJob(
                    FwUploadError.ERROR_UNKNOWN,
                    "Subscription to WillReboot OTA Feature failed"
                )
                return@supervisorScope
            }

            var uploadedData = 0
            val fileSize = fileDescriptor.length

            val stream = kotlin.runCatching { fileDescriptor.openFile() }.getOrNull()
            if (stream == null) {
                cancelFwUpload()
                cancelOTAJob(FwUploadError.ERROR_TRANSMISSION, "Error while opening file")
                return@supervisorScope
            }

            val writeDataFile: suspend CoroutineScope.() -> Unit  = {
                val maxPacketLength: Int
                if(isWBAProtocol) {
                    val maxPayloadSize = otaNodeService.getNode().maxPayloadSize
                    maxPacketLength = maxPayloadSize - (maxPayloadSize % 16)
                } else {
                    maxPacketLength = 244
                }

                Log.d(TAG, "maxPacketLength = $maxPacketLength")


                while (uploadedData < fileSize) {

                    val writtenDataCount = transferFwFile(
                        stream,
                        fileSize,
                        uploadedData,
                        maxPacketLength
                    )

                    uploadedData += writtenDataCount
                    fwUpdateListener.onUpdate((uploadedData.toFloat() / fileSize) * 100)
                }

                finishFwUpload()
            }

            otaNodeService.getFeatureUpdates(listOf(otaWillRebootFeature)).onEach {
                if (it.data is WillRebootInfo) {
                    when (it.data.infoType) {
                        WillRebootInfoType.REBOOT -> {
                            notifySuccess()
                            return@onEach
                        }
                        WillRebootInfoType.READY_TO_RECEIVE_FILE -> { writeDataFile() }
                        WillRebootInfoType.ERROR_NO_FREE -> { }
                        else -> {}
                    }
                } else {
                    cancelOTAJob(
                        FwUploadError.ERROR_TRANSMISSION,
                        "WillReboot feature has received: $it"
                    )
                }

            }.launchIn(this)

            Log.d(TAG, "Starting FW upgrade")
            startFwUpload(isWBAProtocol)

            if(!isWBAProtocol) {
                writeDataFile()
            }
        }
    }

    private fun notifySuccess() {
        fwUpdateListener.onComplete()
        wbUpdateJob?.cancel()
        wbUpdateJob = null
    }

    private suspend fun startFwUpload(isWBAProtocol: Boolean) {
        val otaControlFeature =
            otaNodeService.getNodeFeatures().firstOrNull { it.name == OTAControl.NAME }
                ?: return

        val commandId = if (fwType == FirmwareType.BLE_FW) START_M0_COMMAND else START_M4_COMMAND

        val nbSectorsToErase = if(isWBAProtocol) updateArgs.sectorCount.toLong() else null
        val startUploadRequest = StartUpload(otaControlFeature, commandId, updateArgs.address, nbSectorsToErase)
        otaNodeService.writeFeatureCommand(
            featureCommand = startUploadRequest,
            responseTimeout = UPLOAD_RESPONSE_DELAY
        )
    }

    private suspend fun cancelFwUpload() {
        val otaControlFeature =
            otaNodeService.getNodeFeatures().firstOrNull { it.name == OTAControl.NAME }
                ?: return

        val stopUploadRequest = StopUpload(otaControlFeature)
        otaNodeService.writeFeatureCommand(
            featureCommand = stopUploadRequest,
            responseTimeout = UPLOAD_RESPONSE_DELAY
        )
    }

    private suspend fun transferFwFile(
        stream: InputStream,
        totalBytesToWrite: Long,
        writtenBytes: Int,
        maxPayloadSize: Int
    ): Int {

        val otaControlFeature =
            otaNodeService.getNodeFeatures().firstOrNull { it.name == OTAFileUpload.NAME }
                ?: return 0
        //FixLP
        otaControlFeature.maxPayloadSize = maxPayloadSize
        val chunkSize = min((totalBytesToWrite - writtenBytes), maxPayloadSize.toLong()).toInt()
        val payload = ByteArray(chunkSize)
        stream.read(payload, 0, chunkSize)
        val uploadRequest = UploadOTAData(otaControlFeature, 0x00, payload)
        val writeResult = otaNodeService.writeFeatureCommand(
            featureCommand = uploadRequest,
            writeTimeout = 3000, //bigger timeout for phones not supporting PHY 2M
            responseTimeout = UPLOAD_RESPONSE_DELAY,
            retry = 3,
            retryDelay = 250
        )
        if (writeResult is WriteError) {
            Log.e(TAG, "FW payload write error")
        }
        return chunkSize
    }

    private suspend fun finishFwUpload() {

        val otaControlFeature =
            otaNodeService.getNodeFeatures().firstOrNull { it.name == OTAControl.NAME }
                ?: return

        val commandResult = otaNodeService.writeFeatureCommand(
            featureCommand = FinishUpload(otaControlFeature),
            responseTimeout = FINISH_UPLOAD_RESPONSE_DELAY,
            retry = 3,
            retryDelay = 250
        )

        if (commandResult is WriteError) {
            cancelOTAJob(
                FwUploadError.ERROR_TRANSMISSION,
                "Cannot deliver end of transmission command"
            )
        }
    }

    private suspend fun isOTAMode(node: Node): Boolean {

        node.advertiseInfo ?: return false

        if (node.advertiseInfo.getDeviceId() == OTA_NODE_ID || node.familyType == Boards.Family.WBA_FAMILY || node.boardType == Boards.Model.WB0X_NUCLEO_BOARD) {
            return true
        }

        if (node.advertiseInfo.getProtocolVersion() == 0x02.toShort()) {
            node.advertiseInfo.getFwInfo()?.let {
                val boardFirmware = catalogRepository.getFwDetailsNode(it.deviceId, it.fwId)
                if (boardFirmware != null) {
                    return boardFirmware.fota.type == BoardFotaType.WB_READY
                }
            }
        }

        return false
    }
}