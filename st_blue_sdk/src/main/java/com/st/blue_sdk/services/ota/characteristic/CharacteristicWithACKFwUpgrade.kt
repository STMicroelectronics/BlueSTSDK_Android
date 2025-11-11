/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.ota.characteristic

import android.util.Log
import com.st.blue_sdk.NodeService
import com.st.blue_sdk.features.ota.ImageFeature
import com.st.blue_sdk.features.ota.ImageInfo
import com.st.blue_sdk.features.ota.nrg.ExpectedImageTUSeqNumberFeature
import com.st.blue_sdk.features.ota.nrg.NewImageFeature
import com.st.blue_sdk.features.ota.nrg.NewImageTUContentFeature
import com.st.blue_sdk.features.ota.nrg.request.ImageTUUpload
import com.st.blue_sdk.features.ota.nrg.request.WriteNewImageParameter
import com.st.blue_sdk.features.ota.nrg.response.ExpectedImageSeqNumber
import com.st.blue_sdk.features.ota.nrg.response.ImageTUContentInfo
import com.st.blue_sdk.features.ota.nrg.response.NewImageInfo
import com.st.blue_sdk.services.NodeServiceConsumer
import com.st.blue_sdk.services.ota.*
import com.st.blue_sdk.utils.NumberConversion
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onSubscription
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.StrictMath.min
import kotlin.experimental.xor

class CharacteristicWithACKFwUpgrade(
    private val coroutineScope: CoroutineScope,
    private val nodeServiceConsumer: NodeServiceConsumer,
) : FwConsole {

    private lateinit var fileDescriptor: FwFileDescriptor

    private lateinit var nodeService: NodeService

    private lateinit var fwUpdateListener: FwUpdateListener

    private var fwUpgradeJob: Job? = null

    private var ackTimeoutJob: Job? = null

    private var blueNRGClientType = 1 // BLUENRG 1 or 2 (client or mart phone)

    private var fwImagePacketSize: Int = FW_IMAGE_PACKET_SIZE_DEFAULT

    private val currentPhaseStateFlow = MutableStateFlow(ProtocolPhase(ProtocolState.MTU_REQUEST))

    private var isSDKVersionHigherThan310 = false

    private var lastOtaAckEvery = OTA_ACK_EVERY

    private var cntExtended: Long = 0

    private var baseAddress: Long = 0

    private var flashLB: Long = 0

    private var flashUB: Long = 0

    private var retriesForChecksumError = 0

    private var retriesForSequenceError = 0

    private var retriesForMissedNotification = 0

    private var seqNum = 0

    private var blueNRGClientTypeForce1 = false

    private var fileData: ByteArray? = null

    private var completeWithError = false

    private var featureUpdatesRetries = 0

    private val localCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {

        private val TAG = CharacteristicWithACKFwUpgrade::class.simpleName

        private const val DEFAULT_WRITE_DATA_LEN = 20
        private const val REQUEST_MTU_TIMEOUT = 2000L
        private const val MAX_ATT_MTU = 220
        private const val FW_IMAGE_PACKET_SIZE_DEFAULT = 16

        // Sequence Number (2 bytes), NeedsAcks (1 byte), Checksum (1 byte)
        private const val OTA_SUPPORT_INFO_SIZE = 4
        private const val OTA_ACK_EVERY: Byte = 8

        private const val RETRIES_FOR_CHECKSUM_ERROR_MAX = 4
        private const val RETRIES_FOR_SEQUENCE_ERROR_MAX = 4

        private const val FW_UPLOAD_MSG_TIMEOUT_MS = 8000L

        private const val RETRIES_FOR_MISSED_NOTIFICATION_MAX = 400

        private const val RETRY_MAX = 5
    }

    private data class ProtocolPhase(
        val state: ProtocolState,
        val random: Double = Math.random()
    )

    private enum class ProtocolState {
        MTU_REQUEST,
        READ_PARAM_SDK_SERVER_VERSION,
        READ_BLUENRG_SERVER_TYPE,
        RANGE_FLASH_MEM,
        PARAM_FLASH_MEM,
        READ_PARAM_FLASH_MEM,
        START_ACK_NOTIFICATION,
        FIRST_RECEIVED_NOTIFICATION,
        WRITE_CHUNK_DATA,
        CLOSURE,
        EXIT_PROTOCOL
    }

    override fun launchFirmwareUpgrade(
        nodeId: String,
        fwType: FirmwareType,
        fileDescriptor: FwFileDescriptor,
        params: FwUpgradeParams?,
        fwUpdateListener: FwUpdateListener
    ): Boolean {

        this.fileDescriptor = fileDescriptor
        this.nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: return false
        this.fwUpdateListener = fwUpdateListener

        coroutineScope.launch { handleFwUpdate() }

        return true
    }

    private suspend fun handleFwUpdate() {
        fwUpgradeJob = localCoroutineScope.launch {
            currentPhaseStateFlow.collect { phase ->
                Log.d(TAG, "current update phase is: ${phase.state.name}")
                when (phase.state) {
                    ProtocolState.MTU_REQUEST -> requestMTU()
                    ProtocolState.READ_PARAM_SDK_SERVER_VERSION -> readParamSkdServerVersion()
                    ProtocolState.READ_BLUENRG_SERVER_TYPE -> readBlueNRGServerType()
                    ProtocolState.RANGE_FLASH_MEM -> rangeFlashMem()
                    ProtocolState.PARAM_FLASH_MEM -> paramFlashMem()
                    ProtocolState.READ_PARAM_FLASH_MEM -> readParamFlashMem()
                    ProtocolState.START_ACK_NOTIFICATION -> startAckNotification()
                    ProtocolState.FIRST_RECEIVED_NOTIFICATION -> evaluateFirstNotification()
                    ProtocolState.WRITE_CHUNK_DATA -> writeDataChunk()
                    ProtocolState.CLOSURE -> close()
                    ProtocolState.EXIT_PROTOCOL -> Unit
                }
            }
        }
    }

    private suspend fun requestMTU() {

        val nextPhase = try {
            withTimeout(REQUEST_MTU_TIMEOUT) {
                nodeService.getDeviceStatus().onSubscription {
                    nodeService.bleHal.requestPayloadSize(maxPayloadSize = MAX_ATT_MTU)
                }.firstOrNull()?.let {

                    if (it.mtu - OTA_SUPPORT_INFO_SIZE > FW_IMAGE_PACKET_SIZE_DEFAULT) {

                        blueNRGClientType = 2

                        // Set number of 16-bytes blocks to be sent on a single OTA Client packet
                        val numberOfBlocksPerPacket =
                            (it.mtu - OTA_SUPPORT_INFO_SIZE) / FW_IMAGE_PACKET_SIZE_DEFAULT
                        if (numberOfBlocksPerPacket == 1) {
                            //if the mtu extension is not available, try to use a lower connection interval to speed up the upload
                            nodeService.bleHal.requestLowerConnectionInterval()
                        }

                        fwImagePacketSize = FW_IMAGE_PACKET_SIZE_DEFAULT * numberOfBlocksPerPacket
                        ProtocolState.READ_BLUENRG_SERVER_TYPE
                    } else {
                        ProtocolState.READ_PARAM_SDK_SERVER_VERSION
                    }
                } ?: ProtocolState.READ_PARAM_SDK_SERVER_VERSION
            }
        } catch (e: TimeoutCancellationException) {
            Log.d(TAG, e.stackTraceToString())
            ProtocolState.READ_PARAM_SDK_SERVER_VERSION
        }

        currentPhaseStateFlow.tryEmit(ProtocolPhase(nextPhase))
    }

    private suspend fun readParamSkdServerVersion() {

        val newImageFeature =
            nodeService.getNodeFeatures().firstOrNull { it.name == NewImageFeature.NAME }
        if (newImageFeature == null) {
            notifyError()
            return
        }

        val featureUpdates = nodeService.readFeature(newImageFeature)
        if (featureUpdates.isEmpty() || featureUpdates[0].data !is NewImageInfo) {
            notifyError()
            return
        }

        val newImageInfo = featureUpdates[0].data as NewImageInfo
        handleNewImageFeatureUpdate(newImageInfo)
    }

    private fun handleNewImageFeatureUpdate(newImageInfo: NewImageInfo) {

        val nextPhase =
            if (currentPhaseStateFlow.value.state == ProtocolState.READ_PARAM_SDK_SERVER_VERSION) {
                isSDKVersionHigherThan310 = newImageInfo.otaAckEvery.value >= 2
                ProtocolState.READ_BLUENRG_SERVER_TYPE
            } else {
                if (newImageInfo.otaAckEvery.value != OTA_ACK_EVERY
                    || newImageInfo.imageSize.value != cntExtended
                    || newImageInfo.baseAddress.value != baseAddress
                ) {
                    featureUpdatesRetries += 1
                    Log.d(TAG, "retries: $featureUpdatesRetries")
                    if (featureUpdatesRetries > RETRY_MAX) {
                        completeWithError = true
                        ProtocolState.CLOSURE
                    } else {
                        currentPhaseStateFlow.value.state
                    }
                } else {
                    featureUpdatesRetries = 0
                    ProtocolState.START_ACK_NOTIFICATION
                }
            }

        currentPhaseStateFlow.tryEmit(ProtocolPhase(nextPhase))
    }

    private suspend fun readBlueNRGServerType() {

        val feature =
            nodeService.getNodeFeatures()
                .firstOrNull { it.name == NewImageTUContentFeature.NAME }
        if (feature == null) {
            notifyError()
            return
        }

        val featureUpdates = nodeService.readFeature(feature)
        if (featureUpdates.isEmpty() || featureUpdates[0].data !is ImageTUContentInfo) {
            notifyError()
            return
        }

        val tuContentInfo = featureUpdates[0].data as ImageTUContentInfo
        val nextPhase = if (blueNRGClientType == 1) {
            if (isSDKVersionHigherThan310 || tuContentInfo.expectedWriteLength.value <= DEFAULT_WRITE_DATA_LEN) {
                ProtocolState.RANGE_FLASH_MEM
            } else {
                completeWithError = true
                ProtocolState.CLOSURE
            }
        } else {

            if (tuContentInfo.expectedWriteLength.value <= DEFAULT_WRITE_DATA_LEN) { // server is BlueNRG1 (client is BlueNRG2 extension)
                fwImagePacketSize = FW_IMAGE_PACKET_SIZE_DEFAULT // force BlueNRG1 protocol size
                Log.d(
                    TAG,
                    "FwPacketSize = $fwImagePacketSize, content info size = ${tuContentInfo.expectedWriteLength.value}"
                )
            }

            ProtocolState.RANGE_FLASH_MEM
        }

        currentPhaseStateFlow.tryEmit(ProtocolPhase(nextPhase))
    }

    private suspend fun rangeFlashMem() {

        val feature =
            nodeService.getNodeFeatures().firstOrNull { it.name == ImageFeature.NAME }
        if (feature == null) {
            notifyError()
            return
        }

        val featureUpdates = nodeService.readFeature(feature)
        if (featureUpdates.isEmpty() || featureUpdates[0].data !is ImageInfo) {
            notifyError()
            return
        }

        val imageInfo = featureUpdates[0].data as ImageInfo

        flashLB = imageInfo.flashLB.value
        flashUB = imageInfo.flashUB.value

        // Set base address
        baseAddress = flashLB

        currentPhaseStateFlow.tryEmit(ProtocolPhase(ProtocolState.PARAM_FLASH_MEM))
    }

    private suspend fun paramFlashMem() {

        cntExtended = fileDescriptor.length

        if (fileDescriptor.length % fwImagePacketSize != 0L) { // residue of fw_image_packet_size
            // to have always cntExtended as multiple of fw_image_packet_size
            cntExtended = (fileDescriptor.length / fwImagePacketSize + 1) * fwImagePacketSize
        }

        val isBaseAddressInRange =
            baseAddress >= flashLB && baseAddress + cntExtended <= flashUB && baseAddress % 512 == 0L

        if (isBaseAddressInRange.not()) {
            notifyError()
        } else {
            if (blueNRGClientType == 2) {
                collectNewImageFeatureUpdates()
            }

            nodeService.getNodeFeatures().firstOrNull { it.name == NewImageFeature.NAME }
                ?.let {
                    val command = WriteNewImageParameter(
                        it as NewImageFeature,
                        OTA_ACK_EVERY,
                        cntExtended,
                        baseAddress
                    )
                    nodeService.writeFeatureCommand(command)
                    currentPhaseStateFlow.tryEmit(ProtocolPhase(ProtocolState.READ_PARAM_FLASH_MEM))
                }
        }
    }

    private suspend fun collectNewImageFeatureUpdates() {

        val newImageFeature =
            nodeService.getNodeFeatures().firstOrNull { it.name == NewImageFeature.NAME }
                ?: return

        localCoroutineScope.launch {
            nodeService.getFeatureUpdates(listOf(newImageFeature)).collect {
                if (it.data is NewImageInfo) {
                    handleNewImageFeatureUpdate(it.data)
                }
            }
        }

        nodeService.setFeaturesNotifications(listOf(newImageFeature), true)
    }

    private suspend fun readParamFlashMem() {
        readParamSkdServerVersion()
    }

    private suspend fun startAckNotification() {

        val expectedImageFeature =
            nodeService.getNodeFeatures()
                .firstOrNull { it.name == ExpectedImageTUSeqNumberFeature.NAME }
                ?: return

        localCoroutineScope.launch {
            nodeService.getFeatureUpdates(listOf(expectedImageFeature)).collect {
                if (it.data is ExpectedImageSeqNumber) {
                    handleAckNotification(it.data)
                }
            }
        }

        nodeService.setFeaturesNotifications(listOf(expectedImageFeature), true)
    }

    private fun handleAckNotification(featureUpdate: ExpectedImageSeqNumber) {

        ackTimeoutJob?.cancel()
        retriesForMissedNotification = 0

        if (currentPhaseStateFlow.value.state == ProtocolState.START_ACK_NOTIFICATION) {
            currentPhaseStateFlow.tryEmit(ProtocolPhase(ProtocolState.FIRST_RECEIVED_NOTIFICATION))
        } else {
            val errorCode = featureUpdate.errorAck.value
            val nextExpectedCharacterBlock = featureUpdate.nextExpectedCharBlock.value
            val canProceed = evaluateFeatureResponse(nextExpectedCharacterBlock, errorCode)
            if (canProceed) {
                val sentData = (seqNum * fwImagePacketSize)
                fwUpdateListener.onUpdate((sentData.toFloat() / fileDescriptor.length) * 100)

                if (cntExtended - sentData <= 0) {
                    currentPhaseStateFlow.tryEmit(ProtocolPhase(ProtocolState.CLOSURE))
                    return
                }

                if (cntExtended - (seqNum + OTA_ACK_EVERY) * fwImagePacketSize < 0) { // residue of (OTA_ACK_EVERY * fw_image_packet_size)
                    // if next sequence is the last one with residue size
                    lastOtaAckEvery =
                        (cntExtended / fwImagePacketSize - seqNum).toByte()  // to have sendData=0; cntExtended is multiple of fw_image_packet_size
                }

                currentPhaseStateFlow.tryEmit(ProtocolPhase(currentPhaseStateFlow.value.state))
            } else {
                if (!blueNRGClientTypeForce1) {
                    notifyError()
                }
                currentPhaseStateFlow.tryEmit(ProtocolPhase(ProtocolState.CLOSURE))
            }
        }
    }

    private fun evaluateFeatureResponse(
        nextExpectedCharBlock: Int,
        ack: ExpectedImageSeqNumber.ErrorCode
    ): Boolean {

        var result = false

        when (ack) {
            ExpectedImageSeqNumber.ErrorCode.FLASH_WRITE_FAILED -> {
                Log.d(TAG, "FLASH_WRITE_FAILED")
            }
            ExpectedImageSeqNumber.ErrorCode.FLASH_VERIFY_FAILED -> {
                Log.d(TAG, "FLASH_VERIFY_FAILED")
            }
            ExpectedImageSeqNumber.ErrorCode.CHECK_SUM_ERROR -> if (retriesForChecksumError < RETRIES_FOR_CHECKSUM_ERROR_MAX) {
                Log.d(TAG, "CHECK_SUM_ERROR")
                seqNum = nextExpectedCharBlock
                result = true
                retriesForChecksumError++
            }
            ExpectedImageSeqNumber.ErrorCode.SEQUENCE_ERROR -> if (retriesForSequenceError < RETRIES_FOR_SEQUENCE_ERROR_MAX) {
                Log.d(TAG, "SEQUENCE_ERROR")
                seqNum = nextExpectedCharBlock
                result = true
                retriesForSequenceError++
                Log.d(
                    TAG,
                    "retriesForSequenceError: $retriesForSequenceError   fw_image_packet_size: $fwImagePacketSize"
                )
            } else {
                if (nextExpectedCharBlock == 0 && fwImagePacketSize > FW_IMAGE_PACKET_SIZE_DEFAULT) {
                    if (!blueNRGClientTypeForce1) {
                        //we try with the extended mtu but we had an error, the fist time try also
                        //to change the connection interval and use a smaller package length
                        nodeService.bleHal.requestLowerConnectionInterval()
                    }
                    fwImagePacketSize = FW_IMAGE_PACKET_SIZE_DEFAULT
                    Log.d(TAG, "SEQ error 2 restore packet size to $FW_IMAGE_PACKET_SIZE_DEFAULT")
                    retriesForSequenceError = 0
                    seqNum = nextExpectedCharBlock
                    blueNRGClientTypeForce1 = true
                }
            }
            ExpectedImageSeqNumber.ErrorCode.NO_ERROR -> {
                Log.d(TAG, "NO_ERROR")
                seqNum = nextExpectedCharBlock
                retriesForChecksumError = 0
                retriesForSequenceError = 0
                result = true
            }
            ExpectedImageSeqNumber.ErrorCode.UNKNOWN_ERROR -> {
                Log.d(TAG, "UNKNOWN_ERROR")
            }
        }
        return result
    }

    private fun evaluateFirstNotification() {
        try {
            fileData = ByteArray(fileDescriptor.length.toInt())
            val inputStream = fileDescriptor.openFile()
            val readBytes = inputStream!!.read(fileData)
            inputStream.close()
            if (readBytes.toLong() != fileDescriptor.length) {
                notifyError(FwUploadError.ERROR_INVALID_FW_FILE)
                return
            }
            currentPhaseStateFlow.tryEmit(ProtocolPhase(ProtocolState.WRITE_CHUNK_DATA))
        } catch (e: FileNotFoundException) {
            Log.d(TAG, e.stackTraceToString())
            notifyError(FwUploadError.ERROR_INVALID_FW_FILE)
        } catch (e: IOException) {
            Log.d(TAG, e.stackTraceToString())
            notifyError(FwUploadError.ERROR_TRANSMISSION)
        }
    }

    private suspend fun writeDataChunk() {

        val feature =
            nodeService.getNodeFeatures().firstOrNull { it.name == NewImageTUContentFeature.NAME }
        if (feature == null) {
            notifyError()
            return
        }

        var localSeqNumber = seqNum

        val endIndex =
            if (lastOtaAckEvery != 0.toByte()) (lastOtaAckEvery - (seqNum + 1) % OTA_ACK_EVERY) % lastOtaAckEvery + 1 else 0


        //Set MaxPayload Size
        (feature as NewImageTUContentFeature).setMaxPayLoadSize(nodeService.getNode().maxPayloadSize)

        for (i in 0 until endIndex) {

            // checksum:1 byte + payload:fw_image_packet_size byte +  needsAck:1 byte + SeqNum:2 byte
            val payload = ByteArray(fwImagePacketSize)
            val writtenDataCount = localSeqNumber * fwImagePacketSize


            val copyLength =
                min(fwImagePacketSize, (fileDescriptor.length - writtenDataCount).toInt())

            if(copyLength<=0) {
                //for fixing crash like
                //console.firebase.google.com/project/bluems-b07aa/crashlytics/app/android:com.st.bluems/issues/7c82eeb8884dd7aba4a19938dd0fa481?time=7d&types=crash&sessionEventKey=68E507E6025E00011AA02FEF12BA17DC_2136901913591678020
                startAckTimeout()
                break
            }

            fileData?.let { data ->
                System.arraycopy(data, writtenDataCount, payload, 0, copyLength)
            }

            val needsAck: Byte = if (i == endIndex - 1) 1 else 0
            val seqNumber = NumberConversion.LittleEndian.uint16ToBytes(localSeqNumber)

            val temp = payload + byteArrayOf(needsAck) + seqNumber
            val checksum: Byte = calculateChecksum(temp)

            val message = byteArrayOf(checksum) + temp

            //Log.i("BlueNRGFota","SeqNum=${localSeqNumber}Message = ${message.contentToString()}")
            val writeCommand = ImageTUUpload(feature, message)
            nodeService.writeFeatureCommand(featureCommand = writeCommand, responseTimeout = 5)
            if (needsAck == 0.toByte()) {
                localSeqNumber++
            } else {
                startAckTimeout()
            }
        }
    }

    private suspend fun startAckTimeout() {
        ackTimeoutJob?.cancel()
        ackTimeoutJob = localCoroutineScope.launch {
            delay(FW_UPLOAD_MSG_TIMEOUT_MS)
            if (retriesForMissedNotification < RETRIES_FOR_MISSED_NOTIFICATION_MAX) {
                retriesForMissedNotification++
            } else {
                notifyError()
            }
        }
    }

    private fun calculateChecksum(message: ByteArray): Byte {
        var checksum: Byte = 0
        for (element in message) {
            checksum = checksum xor element
        }
        //Log.i("BlueNRGFota", "size=${message.size}checksum=$checksum")
        return checksum
    }

    private fun close() {

        ackTimeoutJob?.cancel()

        val nextPhase = if (blueNRGClientTypeForce1) {
            blueNRGClientType = 1 // repeat all as blueNRG 1
            blueNRGClientTypeForce1 = false
            ProtocolState.READ_PARAM_SDK_SERVER_VERSION
        } else {

            if (completeWithError) {
                notifyError()
            } else {
                notifySuccess()
            }

            ProtocolState.EXIT_PROTOCOL
        }

        currentPhaseStateFlow.tryEmit(ProtocolPhase(nextPhase))
    }

    private fun notifyError(fwUploadError: FwUploadError = FwUploadError.ERROR_UNKNOWN) {
        fwUpgradeJob?.cancel()
        ackTimeoutJob?.cancel()
        localCoroutineScope.cancel()
        fwUpdateListener.onError(fwUploadError)
    }

    private fun notifySuccess() {
        fwUpgradeJob?.cancel()
        ackTimeoutJob?.cancel()
        localCoroutineScope.cancel()
        fwUpdateListener.onComplete()
    }
}