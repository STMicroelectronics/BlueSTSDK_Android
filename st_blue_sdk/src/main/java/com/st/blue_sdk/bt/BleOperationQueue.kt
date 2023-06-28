/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt

import android.bluetooth.BluetoothGattService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

class BleOperationQueue(private val coroutineScope: CoroutineScope) {

    private var channel: Channel<ChannelMessage<*>> = Channel()

    private var consumerJob: Job? = null

    fun start() {

        if (consumerJob?.isActive == true) {
            return
        }

        channel = Channel()
        consumerJob = coroutineScope.launch(Dispatchers.IO) {
            channel.consumeEach { message ->
                ensureActive()
                kotlin.runCatching { handleMessage(message) }
            }
        }
    }

    fun stop() {
        channel.close()
        consumerJob?.cancel()
    }

    private suspend fun <T> handleMessage(msg: ChannelMessage<T>) {
        msg.completable.complete(msg.operation.block())
    }

    suspend fun <T> enqueueOperation(operation: BleOperation<T>): CompletableDeferred<T?> {
        val completable = when (operation) {
            is BleOperation.WriteCharacteristic, is BleOperation.WriteDescriptor -> CompletableDeferred<Boolean>()
            is BleOperation.ReadCharacteristic -> CompletableDeferred<ByteArray?>()
            is BleOperation.DiscoverServices -> CompletableDeferred<List<BluetoothGattService>>()
            is BleOperation.ChangeMTU -> CompletableDeferred<Int>()
        } as CompletableDeferred<T?>
        channel.send(ChannelMessage(operation, completable))
        return completable
    }
}

data class ChannelMessage<T>(
    val operation: BleOperation<T>,
    val completable: CompletableDeferred<T?>
)

/**
 * Sealed class that represent available BLE operations
 */
sealed class BleOperation<T> {

    abstract val block: suspend () -> T?

    data class DiscoverServices<T>(override val block: suspend () -> T) : BleOperation<T>()

    data class WriteCharacteristic<T>(override val block: suspend () -> T?) : BleOperation<T>()

    data class WriteDescriptor<T>(override val block: suspend () -> T?) : BleOperation<T>()

    data class ReadCharacteristic<T>(override val block: suspend () -> T?) : BleOperation<T>()

    data class ChangeMTU<T>(override val block: suspend () -> T) : BleOperation<T>()
}