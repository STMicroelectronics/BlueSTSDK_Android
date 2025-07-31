/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio.codec.factory

import com.st.blue_sdk.services.audio.codec.AudioCodecManager
import com.st.blue_sdk.services.audio.codec.CodecType
import com.st.blue_sdk.services.audio.codec.adpcm.ADPCMManager
import com.st.blue_sdk.services.audio.codec.opus.OpusManager
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioCodecManagerProviderImpl @Inject constructor(
    private val coroutineScope: CoroutineScope
) : AudioCodecManagerProvider {

    private val mapManager = mutableMapOf<String, AudioCodecManager>()

    override fun createAudioCodecManager(
        nodeId: String, type: CodecType,
        isLe2MPhySupported: Boolean
    ): AudioCodecManager {

        val audioCodec = when (type) {
            CodecType.OPUS -> OpusManager(coroutineScope, isLe2MPhySupported)
            CodecType.ADPCM -> ADPCMManager(coroutineScope)
        }

        mapManager[nodeId] = audioCodec
        return audioCodec
    }

    override fun getAudioCodecManager(nodeId: String): AudioCodecManager? = mapManager[nodeId]

    override fun removeAudioCodecManager(nodeId: String): Boolean {

        getAudioCodecManager(nodeId)?.destroy()

        return mapManager.remove(nodeId) != null
    }
}