/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.ota.debug

import android.util.Log
import com.st.blue_sdk.services.debug.DebugMessage
import com.st.blue_sdk.services.debug.DebugService
import com.st.blue_sdk.services.ota.*
import com.st.blue_sdk.utils.NumberConversion
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.Checksum
import kotlin.math.min

/**
 * Implement the FwUpgradeConsole for a board running the BlueMs firmware.
 * In this case the protocol is:
 * mobile:upgrade[Ble|Fw]+length+fileCrc
 * node:fileCrc
 * mobile: file data, the file is spited in message of 16bytes
 * node: when all the byte are write return 1 if the crc is ok, -1 otherwise
 */
open class DebugFwUpgrade(
    private val coroutineScope: CoroutineScope,
    private val debugService: DebugService,
    private val supportFastFota: Boolean = false,
    private val fotaMaxChunkSize: Int? = null,
    private val fotaChunkDivisorConstraint: Int? = null
) : FwConsole {

    companion object {

        private val CHARSET = StandardCharsets.ISO_8859_1 //ASCII

        private val TAG = DebugFwUpgrade::class.simpleName

        /**
         * the Stm32 L4 can write only 8bytes at time, so sending a multiple of 8 simplify the fw code
         */

        const val DEFAULT_MAX_PAYLOAD_SIZE = 16

        private const val MIN_NOTIFY_EACH_PACKAGE = 2
        const val DEFAULT_NOTIFY_EACH_PACKAGE = 10

        private const val ACK_MSG = "\u0001"

        const val DEFAULT_FW_PACKAGE_DELAY_MS: Long = 13
        private const val MIN_FW_PACKAGE_DELAY_MS: Long = 1

        private const val MAX_RETRY_COUNT = 3
        private const val HANDSHAKE_TIMEOUT: Long = 10000

        private val UPLOAD_BOARD_FW = byteArrayOf(
            'u'.code.toByte(),
            'p'.code.toByte(),
            'g'.code.toByte(),
            'r'.code.toByte(),
            'a'.code.toByte(),
            'd'.code.toByte(),
            'e'.code.toByte(),
            'F'.code.toByte(),
            'w'.code.toByte()
        )
        private val UPLOAD_BLE_FW = byteArrayOf(
            'u'.code.toByte(),
            'p'.code.toByte(),
            'g'.code.toByte(),
            'r'.code.toByte(),
            'a'.code.toByte(),
            'd'.code.toByte(),
            'e'.code.toByte(),
            'B'.code.toByte(),
            'l'.code.toByte(),
            'e'.code.toByte()
        )
    }

    var maxPayloadSize = DEFAULT_MAX_PAYLOAD_SIZE
        private set

    private var packageDelayMs = DEFAULT_FW_PACKAGE_DELAY_MS

    private var notifyEachPackage = DEFAULT_NOTIFY_EACH_PACKAGE

    private var updateFwJob: Job? = null

    private val rendezvousChannel = Channel<Unit>()

    var hasCompletedHandshake = false
        private set

    private lateinit var fwUpdateListener: FwUpdateListener

    override fun launchFirmwareUpgrade(
        nodeId: String,
        fwType: FirmwareType,
        fileDescriptor: FwFileDescriptor,
        params: FwUpgradeParams?,
        fwUpdateListener: FwUpdateListener
    ): Boolean {

        if (updateFwJob != null)
            return false

        this.fwUpdateListener = fwUpdateListener

        val configuration = getFwUpdateConfiguration()
        setMaxPayloadSize(configuration.payloadSize)
        setPackageDelay(configuration.packageDelay)
        setPackageNotification(configuration.notifyInterval)

        updateFwJob = coroutineScope.launch { startFwUpgrade(fwType, fileDescriptor) }

        return updateFwJob != null
    }

    open fun getFwUpdateConfiguration(): FwUpgradeConfiguration {

        val payloadSize =
            if (supportFastFota) {
                var maxChuckSize = debugService.getMaxPayloadSize()

                //Check if we have one constraint on Max Length
                if (fotaMaxChunkSize != null) {
                    if (fotaMaxChunkSize != 0) {
                        if (fotaMaxChunkSize in 1 until maxChuckSize) {
                            maxChuckSize = fotaMaxChunkSize
                        }
                    }
                }

                //Check if we have also a further constraint
                if (fotaChunkDivisorConstraint != null) {
                    if (fotaChunkDivisorConstraint != 0) {
                        maxChuckSize =
                            (maxChuckSize / fotaChunkDivisorConstraint) * fotaChunkDivisorConstraint
                    }
                }
                maxChuckSize
            } else DEFAULT_MAX_PAYLOAD_SIZE

        val packageDelay: Long
        val notifyInterval: Int

        if (supportFastFota) {
            packageDelay = MIN_FW_PACKAGE_DELAY_MS
            notifyInterval = MIN_NOTIFY_EACH_PACKAGE
        } else {
            packageDelay = DEFAULT_FW_PACKAGE_DELAY_MS
            notifyInterval = DEFAULT_NOTIFY_EACH_PACKAGE
        }

        return FwUpgradeConfiguration(payloadSize, packageDelay, notifyInterval)
    }

    private fun setMaxPayloadSize(size: Int) {
        Log.d(TAG, "max payload size is $size bytes")
        maxPayloadSize = size
    }

    private fun setPackageDelay(delay: Long) {
        Log.d(TAG, "package delay is $delay ms")
        packageDelayMs = delay
    }

    private fun setPackageNotification(notification: Int) {
        Log.d(TAG, "package notification is $notification")
        notifyEachPackage = notification
    }

    private suspend fun startFwUpgrade(fwType: FirmwareType, fileDescriptor: FwFileDescriptor) {

        supervisorScope {

            val dataStream = fileDescriptor.openFile()
            if (dataStream == null) {
                notifyError(FwUploadError.ERROR_INVALID_FW_FILE)
                return@supervisorScope
            }

            val fileSize = fileDescriptor.length
            val crc = computeCrc32(fileDescriptor)

            launch {
                debugService.getDebugMessages().collect { message ->
                    onDebugMessageReceived(message, crc)
                }
            }

            val preambleData = prepareLoadCommand(fwType, fileSize, crc)
            val hasWrittenPreamble = writeDataChunk(preambleData)

            if (hasWrittenPreamble == 0) {
                notifyError(FwUploadError.ERROR_TRANSMISSION)
                return@supervisorScope
            }

            var transmissionFailCount = 0

            var writtenFwBytes: Long = getStartingByteOffset()
            withContext(Dispatchers.IO) { dataStream.skip(writtenFwBytes) }

            var payloadPair = getNextFwPayloadChunk(dataStream, writtenFwBytes, fileSize)

            launch {

                try {
                    withTimeout(HANDSHAKE_TIMEOUT) {
                        rendezvousChannel.receive() // wait handshake
                    }
                } catch (e: TimeoutCancellationException) {
                    notifyError(FwUploadError.ERROR_TRANSMISSION)
                    return@launch
                }

                while (true) {

                    if (writtenFwBytes >= fileSize) {
                        return@launch
                    }

                    delay(packageDelayMs * (transmissionFailCount + 1))

                    val hasWriteData = writeDataChunk(payloadPair.first, payloadPair.first.size) > 0
                    if (hasWriteData.not()) { // write failed
                        transmissionFailCount = +1
                        Log.d(
                            TAG,
                            "write error, resend payload. Transmissions fails = $transmissionFailCount"
                        )
                        if (transmissionFailCount >= MAX_RETRY_COUNT) {
                            Log.d(TAG, "stop fw upgrade. Too many transmissions.")
                            notifyError(FwUploadError.ERROR_TRANSMISSION)
                            return@launch
                        }
                    } else {
                        transmissionFailCount = 0
                        writtenFwBytes += payloadPair.second
                        payloadPair = getNextFwPayloadChunk(dataStream, writtenFwBytes, fileSize)
                    }

                    fwUpdateListener.onUpdate((writtenFwBytes.toFloat() / fileSize) * 100)
                    delay(25)
                }
            }
        }
    }

    private suspend fun writeDataChunk(payload: ByteArray, payloadSize: Int? = null): Int {
        return debugService.write(payload, payloadSize)
    }

    open suspend fun onDebugMessageReceived(
        message: DebugMessage,
        computedCrc: Long,
    ) {

        if (hasCompletedHandshake.not()) {

            if (checkCrc(computedCrc, message.payload).not()) {
                notifyError(FwUploadError.ERROR_TRANSMISSION)
                return
            }

            notifyHandshakeCompletion()
        } else {

            if (message.payload.equals(ACK_MSG, ignoreCase = true)) {
                notifySuccess()
                return
            }

            notifyError(FwUploadError.ERROR_CORRUPTED_FILE)
        }
    }

    suspend fun notifyHandshakeCompletion() {
        hasCompletedHandshake = true
        rendezvousChannel.send(Unit)
    }

    open suspend fun getStartingByteOffset(): Long = 0L

    /**
     *
     * merge the file size and crc for create the command that will start the upload on the
     * board
     * @param fwType firmware to update
     * @param fileSize number of file to send
     * @param fileCrc file crc
     * @return command to send to the board
     */
    private fun prepareLoadCommand(
        fwType: FirmwareType,
        fileSize: Long,
        fileCrc: Long
    ): ByteArray {
        val command: ByteArray
        var offset: Int
        if (fwType == FirmwareType.BLE_FW) {
            offset = UPLOAD_BLE_FW.size
            command = ByteArray(offset + 8)
            System.arraycopy(UPLOAD_BLE_FW, 0, command, 0, offset)
        } else {
            offset = UPLOAD_BOARD_FW.size
            command = ByteArray(offset + 8)
            System.arraycopy(UPLOAD_BOARD_FW, 0, command, 0, offset)
        }
        var temp: ByteArray = NumberConversion.LittleEndian.uint32ToBytes(fileSize)
        System.arraycopy(temp, 0, command, offset, temp.size)
        offset += temp.size
        temp = NumberConversion.LittleEndian.uint32ToBytes(fileCrc)
        System.arraycopy(temp, 0, command, offset, temp.size)
        return command
    }

    /**
     * Get next chunk of bytes to be written via debug console.
     *
     * @return a Pair of byteArray and Int.
     * byteArray contains data to send. Int represent how much data have been read from file.
     * When we have OTA with resume we must append the packetId to the readed data.
     * In latter case the size of the payload to send is bigger then the readed data from file
     * */
    open fun getNextFwPayloadChunk(
        stream: InputStream,
        writtenBytes: Long,
        totalBytesToWrite: Long
    ): Pair<ByteArray, Int> {
        val chunkSize = min((totalBytesToWrite - writtenBytes), maxPayloadSize.toLong()).toInt()
        val out = ByteArray(chunkSize)
        stream.read(out, 0, chunkSize)
        return Pair(out, chunkSize)
    }

    private fun computeCrc32(file: FwFileDescriptor): Long {
        val crc: Checksum = STM32Crc32()
        val buffer = ByteArray(4)
        val inputStream = BufferedInputStream(file.openFile())
        //the file must be multiple of 32bit,
        val fileSize: Long = file.length - file.length % 4
        try {
            var i: Long = 0
            while (i < fileSize) {
                if (inputStream.read(buffer) == buffer.size) crc.update(buffer, 0, buffer.size)
                i += 4
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return -1
        }

        return crc.value
    }

    /**
     * @param message message received from the node
     * @return true if the message contain the crc code that we have send
     */
    private fun checkCrc(crc: Long, message: String): Boolean {
        val rcvCrc = message.toByteArray(CHARSET)
        val myCrc = NumberConversion.LittleEndian.uint32ToBytes(crc)
        return Arrays.equals(rcvCrc, myCrc)
    }

    protected fun notifyError(error: FwUploadError) {
        updateFwJob?.cancel()
        updateFwJob = null
        fwUpdateListener.onError(error)
    }

    private fun notifySuccess() {
        updateFwJob?.cancel()
        updateFwJob = null
        fwUpdateListener.onComplete()
    }
}