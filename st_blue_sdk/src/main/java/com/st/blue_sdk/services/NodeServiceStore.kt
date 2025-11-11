/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services

import android.bluetooth.le.ScanResult
import android.content.Context
import com.st.blue_sdk.NodeService
import com.st.blue_sdk.board_catalog.BoardCatalogRepo
import com.st.blue_sdk.bt.advertise.BleAdvertiseInfo
import com.st.blue_sdk.bt.hal.FlowBleHal
import com.st.blue_sdk.logger.Logger
import com.st.blue_sdk.models.NodeState
import com.st.blue_sdk.services.config.ConfigControlServiceImpl
import com.st.blue_sdk.services.debug.DebugServiceImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NodeServiceStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val catalog: BoardCatalogRepo,
    private val loggers: Set<@JvmSuppressWildcards Logger>
) : NodeServiceConsumer, NodeServiceProducer {

    private val nodes: MutableMap<String, NodeService> = mutableMapOf()

    override fun getNodeService(nodeId: String): NodeService? = nodes[nodeId]

    override fun getNodeServices(): List<NodeService> {
        return nodes.values.toList()
    }

    override fun createService(
        scanResult: ScanResult,
        advertiseInfo: BleAdvertiseInfo
    ): NodeService {

        val deviceId = scanResult.device.address

        nodes[deviceId]?.let {
            if (it.getNode().connectionStatus.current != NodeState.Disconnected) {
                throw IllegalArgumentException("Duplicate node ID")
            }
            nodes.remove(deviceId)
        }

        val bleHAL = FlowBleHal(
            context = context,
            coroutineScope = coroutineScope,
            device = scanResult.device,
            rssi = scanResult.rssi,
            advertiseInfo = advertiseInfo
        )

        val debugService = DebugServiceImpl(bleHAL)
        val configControlService = ConfigControlServiceImpl(bleHAL)

        val nodeService = NodeService(
            coroutineScope = coroutineScope,
            advertiseInfo = advertiseInfo,
            bleHal = bleHAL,
            debugService = debugService,
            configControlService = configControlService,
            loggers = loggers.toMutableSet()
        )

        nodes[deviceId] = nodeService
        return nodeService
    }

    override fun removeService(nodeId: String) {
        nodes.remove(nodeId)
    }

    override fun clear() {
        nodes.clear()
    }
}