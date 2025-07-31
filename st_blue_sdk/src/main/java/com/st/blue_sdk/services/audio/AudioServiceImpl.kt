/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio

import android.util.Log
import com.st.blue_sdk.features.Feature
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
import com.st.blue_sdk.services.audio.codec.adpcm.ADPCMParams
import kotlinx.coroutines.flow.emptyFlow

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
        if (nodeService == null) {
            Log.i("AudioServiceImpl", "Unable to find NodeService for $nodeId")
        }

        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)
        if (nodeServer == null) {
            Log.i("AudioServiceImpl", "Unable to find NodeServer for $nodeId")
        }

        val isOpus: Boolean
        val isADPCM: Boolean

        if (nodeService != null) {
            isOpus = nodeService.getNodeFeatures().map { it.name }
                .containsAll(requiredOpusFeatures.toList())
            isADPCM = nodeService.getNodeFeatures().map { it.name }
            .containsAll(requiredADPCMFeatures.toList())
        } else {
            isOpus = false
            isADPCM = false
        }

        val codecManager = when {
            isOpus -> audioCodecManagerProvider.createAudioCodecManager(
                nodeId = nodeId,
                type = CodecType.OPUS,
                isLe2MPhySupported = nodeServer?.isLe2MPhySupported == true
            )

            isADPCM -> audioCodecManagerProvider.createAudioCodecManager(
                nodeId = nodeId,
                type = CodecType.ADPCM,
                isLe2MPhySupported = nodeServer?.isLe2MPhySupported == true
            )

            else -> null
        }

        val configFeatures = when {
            isOpus -> {
                nodeService?.getNodeFeatures()?.filter { it.name == AudioOpusConfFeature.NAME }
                    ?: listOf()
            }

            isADPCM -> {
                nodeService?.getNodeFeatures()
                    ?.filter { it.name == AudioADPCMSyncFeature.NAME } ?: listOf()
            }

            else -> listOf()
        }

        return codecManager?.init(
            nodeService?.getFeatureUpdates(configFeatures) ?: emptyFlow()
        ) == true
    }

    override fun getCodecType(nodeId: String): CodecType {

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
        if (audioCodecManager == null) {
            Log.i("AudioServiceImpl", "Unable to find AudioCodecManager for $nodeId")
        }

        return audioCodecManager?.type ?: CodecType.ADPCM
    }

    override fun isServerEnable(nodeId: String): Boolean {
        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)

        if (nodeServer == null) {
            Log.i("AudioServiceImpl", "Unable to find NodeServer for $nodeId")
        }
        return nodeServer?.isEnabled() == true
    }

    override fun isMusicServerEnable(nodeId: String): Boolean {
        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)

        if (nodeServer == null) {
            Log.i("AudioServiceImpl", "Unable to find NodeServer for $nodeId")
        }

        return nodeServer?.isMusicEnable() == true
    }

    override fun isFullDuplexEnable(nodeId: String): Boolean {
        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)

        if (nodeServer == null) {
            Log.i("AudioServiceImpl", "Unable to find NodeServer for $nodeId")
        }

        return nodeServer?.isFullDuplexEnable() == true
    }

    override fun enableAudio(nodeId: String): Boolean {

        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)

        if (nodeServer == null) {
            Log.i("AudioServiceImpl", "Unable to find NodeServer for $nodeId")
        }

        val config = ExportedAudioOpusConfFeature.NAME
        return nodeServer?.notifyData(
            config,
            ExportedAudioOpusConfFeature.enableNotification()
        ) == true
    }

    override fun disableAudio(nodeId: String): Boolean {

        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)

        if (nodeServer == null) {
            Log.i("AudioServiceImpl", "Unable to find NodeServer for $nodeId")
        }

        val config = ExportedAudioOpusConfFeature.NAME
        return nodeServer?.notifyData(
            config,
            ExportedAudioOpusConfFeature.disableNotification()
        ) == true
    }

    override fun startDecodingIncomingAudioStream(nodeId: String): Flow<ShortArray> {

        val nodeService = nodeServiceConsumer.getNodeService(nodeId = nodeId)
        if (nodeService == null) {
            Log.i("AudioServiceImpl", "Unable to find NodeService for $nodeId")
        }

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
        if (audioCodecManager == null) {
            Log.i("AudioServiceImpl", "Unable to find AudioCodecManager for $nodeId")
        }

        if ((nodeService != null) && (audioCodecManager != null)) {
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
        } else {
            return emptyFlow()
        }
    }

    private fun sendAudioStream(featureName: String, nodeId: String, data: ShortArray): Boolean {

        val nodeServer = nodeServerConsumer.getNodeServer(nodeId = nodeId)
        if (nodeServer == null) {
            Log.i("AudioServiceImpl", "Unable to find NodeServer for $nodeId")
        }


        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
        if (audioCodecManager == null) {
            Log.i("AudioServiceImpl", "Unable to find AudioCodecManager for $nodeId")
        }

        if ((nodeServer != null) && (audioCodecManager != null)) {
//            Log.i("sendAudioStream","sending = ${data.toList()}")
        val codedData = audioCodecManager.encode(data = data)
//            Log.i("sendAudioStream","sending2= ${codedData.toList()}")
        val pack = BlueVoiceOpusTransportProtocol.packData(codedData, nodeServer.maxPayloadSize)
        var result = true
        pack.forEach {
            result = result && nodeServer.notifyData(featureName, it)
        }
        return result
        } else {
            return false
        }
    }

    override suspend fun sendVoiceAudioStream(nodeId: String, data: ShortArray) =
        sendAudioStream(ExportedAudioOpusVoiceFeature.NAME, nodeId, data)

    override suspend fun sendMusicAudioStream(nodeId: String, data: ShortArray) =
        sendAudioStream(ExportedAudioOpusMusicFeature.NAME, nodeId, data)

    override fun setEncodeParams(nodeId: String, params: EncodeParams) {

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
        if (audioCodecManager == null) {
            Log.i("AudioServiceImpl", "Unable to find AudioCodecManager for $nodeId")
        }

        audioCodecManager?.setEncodeParams(params)
    }

    override fun getDecodeParams(nodeId: String): DecodeParams {

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
        if (audioCodecManager == null) {
            Log.i("AudioServiceImpl", "Unable to find AudioCodecManager for $nodeId")
        }

        return audioCodecManager?.getDecodeParams() ?: ADPCMParams(
            index = 0,
            predSample = 0,
            samplingFreq = 8000,
            channels = 1
        )
    }

    override fun reset(nodeId: String) {

        val audioCodecManager = audioCodecManagerProvider.getAudioCodecManager(nodeId = nodeId)
        if (audioCodecManager == null) {
            Log.i("AudioServiceImpl", "Unable to find AudioCodecManager for $nodeId")
        }

        audioCodecManager?.reset()
    }

    override fun destroy(nodeId: String) {

        val nodeService = nodeServiceConsumer.getNodeService(nodeId = nodeId)
        if (nodeService == null) {
            Log.i("AudioServiceImpl", "Unable to find NodeService for $nodeId")
        }

        val featuresNames = when (getCodecType(nodeId)) {
            CodecType.OPUS -> requiredOpusFeatures.toList()
            CodecType.ADPCM -> requiredADPCMFeatures.toList()
        }

        val features = nodeService?.getNodeFeatures()?.filter {
            featuresNames.contains(it.name)
        } ?: listOf()

        coroutineScope.launch { nodeService?.setFeaturesNotifications(features, false) }

        audioCodecManagerProvider.removeAudioCodecManager(nodeId)
    }
}

