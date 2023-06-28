/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services

import android.bluetooth.BluetoothManager
import android.content.Context
import com.st.blue_sdk.bt.server.NodeServer
import com.st.blue_sdk.features.exported.ExportedFeature
import com.st.blue_sdk.models.Node
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NodeServerStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bleManager: BluetoothManager
) : NodeServerConsumer, NodeServerProducer {

    private val nodes: MutableMap<String, NodeServer> = mutableMapOf()

    override fun getNodeServer(nodeId: String): NodeServer? = nodes[nodeId]

    override fun getNodeServers(): List<NodeServer> {
        return nodes.values.toList()
    }

    override fun createServer(
        node: Node,
        exportedServicesWithFeatures: Map<UUID, List<ExportedFeature>>
    ): NodeServer {

        nodes.remove(node.device.address)

        val nodeServer = NodeServer(
            context = context,
            bleManager = bleManager,
            device = node.device,
            featuresByService = exportedServicesWithFeatures
        )

        nodes[node.device.address] = nodeServer

        return nodeServer
    }

    override fun removeServer(nodeId: String) {
        nodes.remove(nodeId)
    }

    override fun clear() {
        nodes.clear()
    }
}