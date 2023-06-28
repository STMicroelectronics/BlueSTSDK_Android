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
import com.st.blue_sdk.services.ota.FwUploadError
import com.st.blue_sdk.utils.NumberConversion
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Implement the FwUpgradeConsole for a board running the BlueMs firmware.
 * In this case the protocol is:
 * mobile:upgrade[Ble|Fw]+length+fileCrc
 * node:fileCrc
 * mobile: file data, the file is spited in message of 16bytes
 * node: when all the byte are write return 1 if the crc is ok, -1 otherwise
 */
class DebugFwUpgradeWithResume(
    coroutineScope: CoroutineScope,
    debugService: DebugService,
) : DebugFwUpgrade(
    coroutineScope = coroutineScope,
    debugService = debugService,
    supportFastFota = false
) {

    companion object {
        private val TAG = DebugFwUpgradeWithResume::class.simpleName
    }

    private var startingPacketId = 0L

    override fun getFwUpdateConfiguration(): FwUpgradeConfiguration {

        val payloadSize = DEFAULT_MAX_PAYLOAD_SIZE
        val packageDelay = DEFAULT_FW_PACKAGE_DELAY_MS
        val notificationInterval = DEFAULT_NOTIFY_EACH_PACKAGE

        return FwUpgradeConfiguration(payloadSize, packageDelay, notificationInterval)
    }

    override suspend fun onDebugMessageReceived(
        message: DebugMessage,
        computedCrc: Long
    ) {

        if (message.isError.not()) { // if received message isn't an error demand to super implementation
            super.onDebugMessageReceived(message, computedCrc)
            return
        }

        val messageBytes = message.payload.toByteArray()
        if (hasCompletedHandshake.not() && messageBytes[0] == 0x01.toByte()) { // resume fw upgrade from specific packet
            val requestedPackage = NumberConversion.LittleEndian.bytesToUInt32(messageBytes, 1)
            Log.d(TAG, "fw request to start sending data from chunk no. $requestedPackage")
            startingPacketId = requestedPackage
            notifyHandshakeCompletion()
            return
        }

        notifyError(FwUploadError.ERROR_CORRUPTED_FILE)
    }

    override fun getNextFwPayloadChunk(
        stream: InputStream,
        writtenBytes: Long,
        totalBytesToWrite: Long
    ): Pair<ByteArray, Int> {
        val chunkSize = min((totalBytesToWrite - writtenBytes), maxPayloadSize.toLong()).toInt()
        val out = ByteArray(chunkSize)
        stream.read(out, 0, chunkSize)
        val sentPackages = writtenBytes / maxPayloadSize
        val packageId = NumberConversion.LittleEndian.uint32ToBytes(sentPackages + 1)
        return Pair(out + packageId, chunkSize) // append package Id to fw data
    }

    override suspend fun getStartingByteOffset(): Long {
        val writtenBytes = maxPayloadSize * startingPacketId
        return max(0, writtenBytes)
    }
}