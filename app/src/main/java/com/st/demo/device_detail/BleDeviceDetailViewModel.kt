/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.device_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.models.NodeState
import com.st.blue_sdk.services.audio.AudioService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleDeviceDetailViewModel @Inject constructor(
    private val audioService: AudioService,
    private val blueManager: BlueManager
) : ViewModel() {

    companion object {
        private val TAG = BleDeviceDetailViewModel::class.simpleName
        private const val MAX_RETRY_CONNECTION = 3
    }

    val features = MutableStateFlow<List<Feature<*>>>(emptyList())

    fun connect(deviceId: String, maxConnectionRetries: Int = MAX_RETRY_CONNECTION) {
        viewModelScope.launch {
            var retryCount = 0
            blueManager.connectToNode(deviceId).collect {

                val previousNodeState = it.connectionStatus.prev
                val currentNodeState = it.connectionStatus.current

                Log.d(
                    TAG,
                    "Node state (prev: $previousNodeState - current: $currentNodeState) retryCount: $retryCount"
                )

                if (previousNodeState == NodeState.Connecting &&
                    currentNodeState == NodeState.Disconnected
                ) {
                    retryCount += 1

                    if (retryCount > maxConnectionRetries) {
                        return@collect
                    }

                    Log.d(TAG, "Retry connection...")
                    blueManager.connectToNode(deviceId)
                }
            }
        }
    }

    fun bleDevice(deviceId: String): Flow<Node> =
        try {
            blueManager.getNodeStatus(nodeId = deviceId)
        } catch (ex: IllegalStateException) {
            flowOf()
        }

    fun getFeatures(deviceId: String) {
        features.update { blueManager.nodeFeatures(nodeId = deviceId) }
    }

    fun disconnect(deviceId: String) {
        features.update { emptyList() }
        viewModelScope.launch {
            blueManager.disconnect(nodeId = deviceId)
        }
    }

    fun showDebugConsoleBtn(): Boolean = true

    fun showAudioBtn(nodeId: String): Boolean = try {
        audioService.getDecodeParams(nodeId = nodeId)

        true
    } catch (ex: IllegalStateException) {
        false
    }
}