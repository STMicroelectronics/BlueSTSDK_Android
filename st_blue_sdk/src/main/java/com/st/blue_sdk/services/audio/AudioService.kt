/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio

import com.st.blue_sdk.services.audio.codec.CodecType
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.codec.EncodeParams
import kotlinx.coroutines.flow.Flow

interface AudioService {

    suspend fun init(nodeId: String): Boolean

    fun getCodecType(nodeId: String): CodecType

    fun isServerEnable(nodeId: String): Boolean

    fun enableAudio(nodeId: String): Boolean

    fun disableAudio(nodeId: String): Boolean
    fun startDecodingIncomingAudioStream(nodeId: String): Flow<ShortArray>

    fun setEncodeParams(nodeId: String, params: EncodeParams)

    fun getDecodeParams(nodeId: String): DecodeParams

    suspend fun sendMusicAudioStream(nodeId: String, data: ShortArray): Boolean

    suspend fun sendVoiceAudioStream(nodeId: String, data: ShortArray): Boolean

    fun reset(nodeId: String)

    fun destroy(nodeId: String)
    fun isMusicServerEnable(nodeId: String): Boolean

    fun isFullDuplexEnable(nodeId: String) : Boolean
}
