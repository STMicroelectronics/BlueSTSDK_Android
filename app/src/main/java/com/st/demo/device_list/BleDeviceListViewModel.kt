/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.device_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.common.Status
import com.st.blue_sdk.models.LeNode
import com.st.blue_sdk.models.Node
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleDeviceListViewModel @Inject constructor(
    private val blueManager: BlueManager
) : ViewModel() {

    val scanBleDevices = MutableStateFlow<List<Node>>(emptyList())
    val scanBleLeDevices = MutableStateFlow<Pair<Long,List<LeNode>>>(Pair(0L,emptyList()))
    val isLoading = MutableStateFlow(false)

    private val _isLEScanning = MutableStateFlow(false)
    val isLEScanning = _isLEScanning.asStateFlow()

    fun scanSelectedDevicesType() {
        startScan(_isLEScanning.value)
    }

    private var scanPeripheralJob: Job? = null

    fun startScan(leDevices: Boolean) {
        _isLEScanning.update { leDevices }

        if(!leDevices) {
            scanPeripheralJob?.cancel()
            scanBleLeDevices.tryEmit(Pair(System.currentTimeMillis(),emptyList()))
            scanPeripheralJob = viewModelScope.launch {
                blueManager.scanNodes().map {
                    isLoading.tryEmit(it.status == Status.LOADING)

                    it.data ?: emptyList()
                }.collect {
                    scanBleDevices.tryEmit(it)
                }
            }
        } else {
            scanPeripheralJob?.cancel()
            scanBleDevices.tryEmit(emptyList())
            scanPeripheralJob = viewModelScope.launch {
                blueManager.scanLeNodes().map {
                    isLoading.tryEmit(it.status == Status.LOADING)
                    it.data ?: emptyList()
                }.collect {
                    scanBleLeDevices.tryEmit(Pair(System.currentTimeMillis(),it))
                }
            }
        }
    }
}