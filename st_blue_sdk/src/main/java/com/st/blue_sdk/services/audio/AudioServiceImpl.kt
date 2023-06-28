/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio

import com.st.blue_sdk.features.RawAudio
import com.st.blue_sdk.features.audio.adpcm.AudioADPCMFeature
import com.st.blue_sdk.features.audio.adpcm.AudioADPCMSyncFeature
import com.st.blue_sdk.features.exported.ExportedAudioOpusMusicFeature
import com.st.blue_sdk.features.exported.ExportedAudioOpusVoiceFeature
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusConfFeature
import com.st.blue_sdk.features.extended.audio.opus.AudioOpusFeature
import com.st.blue_sdk.services.NodeServerConsumer
import com.st.blue_sdk.services.NodeServiceConsumer
import com.st.blue_sdk.services.audio.codec.CodecType
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.codec.EncodeParams
import com.st.blue_sdk.services.audio.codec.factory.AudioCodecManagerProvider
import com.st.blue_sdk.utils.BlueVoiceOpusTransportProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.st.blue_sdk.features.exported.ExportedAudioOpusConfFeature

@Singleton
class AudioServiceImpl @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val nodeServiceConsumer: NodeServiceConsumer,
    private val nodeServerConsumer: NodeServerConsumer,
    private val audioCodecManagerProvider: AudioCodecManagerProvider
) : AudioService {

    private val requiredOpusFeatures = Pair(
        AudioOpusConfFeature.NAME,
        AudioOpusFeature.NAME
    )

    private val requiredADPCMFeatures = Pair(
        AudioADPCMSyncFeature.NAME,
        AudioADPCMFeature.NAME
    )

    override suspend fun init(nodeId: String): Boolean {

        if (audioCodecManagerProvider.getAudioCodecManager(nodeId) != null) return true

        val nodeService = nodeServiceConsumer.getNodeService(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeServer for $nodeId")

        val isOpus =
            nodeService.getNodeFeatures().map { it.name }.containsAll(requiredOpusFeatures.toList())

        val isADPCM = nodeService.getNodeFeatures().map { it.name }
            .containsAll(requiredADPCMFeatures.toList())

        val codecManager = when {
            isOpus -> audioCodecManagerProvider.createAudioCodecManager(
                nodeId = nodeId,
                type = CodecType.OPUS,
                isLe2MPhySupported = nodeServer.isLe2MPhySupported
            )
            isADPCM -> audioCodecManagerProvider.createAudioCodecManager(
                nodeId = nodeId,
                type = CodecType.ADPCM,
                isLe2MPhySupported = nodeServer.isLe2MPhySupported
            )
            else -> null
        }

        val configFeatures = when {
            isOpus -> nodeService.getNodeFeatures()
                .filter { it.name == AudioOpusConfFeature.NAME }
            isADPCM -> nodeService.getNodeFeatures()
                .filter { it.name == AudioADPCMSyncFeature.NAME }
            else -> listOf()
        }

        return codecManager?.init(nodeService.getFeatureUpdates(configFeatures)) ?: false
    }

    override fun getCodecType(nodeId: String): CodecType {

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find AudioCodecManager for $nodeId")

        return audioCodecManager.type
    }

    override fun isServerEnable(nodeId: String): Boolean {
        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeServer for $nodeId")

        return nodeServer.isEnabled()
    }

    override fun isMusicServerEnable(nodeId: String): Boolean {
        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeServer for $nodeId")

        return nodeServer.isMusicEnable()
    }

    override fun isFullDuplexEnable(nodeId: String): Boolean {
        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeServer for $nodeId")

        return nodeServer.isFullDuplexEnable()
    }

    override fun enableAudio(nodeId: String): Boolean {

        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeServer for $nodeId")

        val config = ExportedAudioOpusConfFeature.NAME
        return nodeServer.notifyData(config, ExportedAudioOpusConfFeature.enableNotification())
    }

    override fun disableAudio(nodeId: String): Boolean {

        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeServer for $nodeId")

        val config = ExportedAudioOpusConfFeature.NAME
        return nodeServer.notifyData(config, ExportedAudioOpusConfFeature.disableNotification())
    }

    override fun startDecondingIncomingAudioStream(nodeId: String): Flow<ShortArray> {

        val nodeService = nodeServiceConsumer.getNodeService(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find AudioCodecManager for $nodeId")

        val audioFeatureName = when (audioCodecManager.type) {
            CodecType.OPUS -> AudioOpusFeature.NAME
            CodecType.ADPCM -> AudioADPCMFeature.NAME
        }

        val audioFeatures = nodeService.getNodeFeatures().filter { it.name == audioFeatureName }

        return nodeService.getFeatureUpdates(audioFeatures)
            .transform { featureUpdate ->
                if (featureUpdate.data is RawAudio) {
                    val decodedData = audioCodecManager.decode(featureUpdate.data.data.value)
                    emit(decodedData)
                }
            }
    }

    private fun sendAudioStream(featureName: String, nodeId: String, data: ShortArray): Boolean {

        val nodeService = nodeServiceConsumer.getNodeService(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeServer for $nodeId")

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find AudioCodecManager for $nodeId")

        val codedData = audioCodecManager.encode(data = data)
        val pack = BlueVoiceOpusTransportProtocol.packData(codedData, nodeServer.maxPayloadSize)
        var result = true
        pack.forEach {
            result = result && nodeServer.notifyData(featureName, it)
        }
        return result
    }

    override suspend fun sendVoiceAudioStream(nodeId: String, data: ShortArray) =
        sendAudioStream(ExportedAudioOpusVoiceFeature.NAME, nodeId, data)

    override suspend fun sendMusicAudioStream(nodeId: String, data: ShortArray) =
        sendAudioStream(ExportedAudioOpusMusicFeature.NAME, nodeId, data)

    override fun setEncodeParams(nodeId: String, params: EncodeParams) {

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find AudioCodecManager for $nodeId")

        audioCodecManager.setEncodeParams(params)
    }

    override fun getDecodeParams(nodeId: String): DecodeParams {

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find AudioCodecManager for $nodeId")

        return audioCodecManager.getDecodeParams()
    }

    override fun reset(nodeId: String) {

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find AudioCodecManager for $nodeId")

        audioCodecManager.reset()
    }

    override fun destroy(nodeId: String) {

        val nodeService = nodeServiceConsumer.getNodeService(nodeId = nodeId)
            ?: throw IllegalStateException("Unable to find NodeService for $nodeId")

        val featuresNames = when (getCodecType(nodeId)) {
            CodecType.OPUS -> requiredOpusFeatures.toList()
            CodecType.ADPCM -> requiredADPCMFeatures.toList()
        }

        val features = nodeService.getNodeFeatures().filter {
            featuresNames.contains(it.name)
        }

        coroutineScope.launch { nodeService.setFeaturesNotifications(features, false) }

        audioCodecManagerProvider.removeAudioCodecManager(nodeId)
    }
}

