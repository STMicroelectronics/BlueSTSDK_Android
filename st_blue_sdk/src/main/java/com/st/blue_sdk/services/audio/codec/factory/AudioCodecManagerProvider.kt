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

interface AudioCodecManagerProvider {

    fun createAudioCodecManager(
        nodeId: String,
        type: CodecType,
        isLe2MPhySupported: Boolean
    ): AudioCodecManager

    fun getAudioCodecManager(nodeId: String): AudioCodecManager?

    fun removeAudioCodecManager(nodeId: String): Boolean
}