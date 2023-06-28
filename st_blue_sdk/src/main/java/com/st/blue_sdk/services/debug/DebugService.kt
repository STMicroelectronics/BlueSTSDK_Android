/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.debug

import kotlinx.coroutines.flow.Flow

interface DebugService {

    suspend fun init()

    fun hasBleDebugService(): Boolean

    /**
     * write a message to the stdIn, the message can be split in more ble writes
     * the string will be converted in a byte array using the default charset configuration
     *
     * @param message message to send
     * @return number of char sent in Terminal standard characteristic
     */
    suspend fun write(message: String): Int

    /**
     * write an array of byte into the stdIn. the array can be split in more ble write
     *
     * @param data array to write
     * @param mtu
     *
     * @return number of byte sent
     */
    suspend fun write(data: ByteArray, mtu: Int? = null): Int

    fun getDebugMessages(): Flow<DebugMessage>

    suspend fun read(timeout: Long = 1000): DebugMessage?

    fun getMaxPayloadSize(): Int
}