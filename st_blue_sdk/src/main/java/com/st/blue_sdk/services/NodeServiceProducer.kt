/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services

import android.bluetooth.le.ScanResult
import com.st.blue_sdk.NodeService
import com.st.blue_sdk.bt.advertise.BleAdvertiseInfo

interface NodeServiceProducer {

    fun createService(scanResult: ScanResult, advertiseInfo: BleAdvertiseInfo): NodeService

    fun removeService(nodeId: String)

    fun clear()
}