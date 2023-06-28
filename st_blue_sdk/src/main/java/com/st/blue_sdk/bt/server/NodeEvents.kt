/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt.server

import java.util.*

sealed class NodeEvents {
    /**
     * Event triggered when a new mtu is agree between the client and the server
     * @param node server that agree the new mtu
     * @param newValue new connection mtu
     */
    data class MtuUpdateEvent(val node: NodeServer, val mtu: Int) : NodeEvents()

    /**
     * Event triggered when a new  PHY is agree between the client and the server
     * @param node server that agree the new PHY
     * @param isUsingPhy2 true if both the client and server are using the 2MPHY
     */
    data class PhyUpdateEvent(val node: NodeServer, val isUsingPhy2: Boolean) : NodeEvents()

    /**
     * Event triggered when a client connect to the server
     */
    data class NodeConnectionEvent(val node: NodeServer) : NodeEvents()

    /**
     * Event triggered when a client disconnect from the server
     */
    data class NodeDisconnectionEvent(val node: NodeServer) : NodeEvents()

    /**
     * Event triggered when a client subscribe or unsubscribe from a characteristic
     */
    data class NotificationStateChange(
        val characteristicUid: UUID,
        val enabled: Boolean
    ) : NodeEvents()
}
