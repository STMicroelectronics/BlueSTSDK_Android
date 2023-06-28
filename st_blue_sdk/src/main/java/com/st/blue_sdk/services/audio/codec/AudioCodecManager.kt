/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio.codec

import com.st.blue_sdk.features.FeatureUpdate
import kotlinx.coroutines.flow.Flow

interface AudioCodecManager {

    val type: CodecType

    val codecName: String

    val channels: Int

    val samplingFreq: Int

    val isAudioEnabled: Boolean

    suspend fun init(configFlow: Flow<FeatureUpdate<*>>): Boolean

    fun enable(enable: Boolean)

    fun setEncodeParams(params: EncodeParams)

    fun getDecodeParams(): DecodeParams

    fun encode(data: ShortArray): ByteArray

    fun decode(data: ByteArray): ShortArray

    fun reset()

    fun destroy()
}