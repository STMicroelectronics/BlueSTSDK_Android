/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.ota

import android.bluetooth.BluetoothManager
import android.util.Log
import com.st.blue_sdk.NodeService
import com.st.blue_sdk.board_catalog.BoardCatalogRepo
import com.st.blue_sdk.board_catalog.models.BoardFotaType
import com.st.blue_sdk.bt.advertise.getFwInfo
import com.st.blue_sdk.features.ota.ImageFeature
import com.st.blue_sdk.features.ota.ImageInfo
import com.st.blue_sdk.features.ota.nrg.ExpectedImageTUSeqNumberFeature
import com.st.blue_sdk.features.ota.nrg.NewImageTUContentFeature
import com.st.blue_sdk.features.ota.stm32wb.OTAControl
import com.st.blue_sdk.features.ota.stm32wb.OTAReboot
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.services.NodeServiceConsumer
import com.st.blue_sdk.services.NodeServiceProducer
import com.st.blue_sdk.services.debug.DebugService
import com.st.blue_sdk.services.fw_version.FwVersion
import com.st.blue_sdk.services.fw_version.FwVersionBoard
import com.st.blue_sdk.services.ota.characteristic.CharacteristicFwUpgrade
import com.st.blue_sdk.services.ota.characteristic.CharacteristicWithACKFwUpgrade
import com.st.blue_sdk.services.ota.debug.DebugFwUpgrade
import com.st.blue_sdk.services.ota.debug.DebugFwUpgradeWithResume
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtaServiceImpl @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val bleManager: BluetoothManager,
    private val catalog: BoardCatalogRepo,
    private val nodeServiceProducer: NodeServiceProducer,
    private val nodeServiceConsumer: NodeServiceConsumer
) : OtaService {

    companion object {

        private val TAG = OtaServiceImpl::class.simpleName

        private val STBOX_NEW_FW_UPGRADE_PROTOCOL =
            FwVersionBoard("SENSORTILE.BOX", "L4R9", 3, 0, 15)

        private val STM32WB_NEW_FW_UPGRADE_PROTOCOL =
            FwVersionBoard("STM32WB OTA", "STM32WB", 1, 0, 0)

        private val STM32WBA_NEW_FW_UPGRADE_PROTOCOL =
            FwVersionBoard("STM32WBA OTA", "STM32WBA", 1, 0, 0)

        private val STM32WB0X_NEW_FW_UPGRADE_PROTOCOL =
            FwVersionBoard("STM32WB0X OTA", "STM32WB0X", 1, 0, 0)

        const val DEFAULT_BOARD_NAME = "BLUENRG OTA"
        const val DEFAULT_MCU_NAME = "BLUENRG"

        const val COMMAND_GET_VERSION_BOARD_FW = "versionFw\n"
        const val COMMAND_GET_VERSION_BLE_FW = "versionBle\n"

        const val FW_VERSION_READ_TIMEOUT = 10000L
        const val FW_VERSION_READ_RETRY_EACH = 10
    }

    override suspend fun updateFirmware(
        nodeId: String
    ): FwConsole? {

        val nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: return null
        return getFwConsole(nodeService)
    }

    override fun getFwUpdateStrategy(nodeId: String): UpgradeStrategy {

        if (hasOTACharacteristics(nodeId)) {
            return UpgradeStrategy.CHARACTERISTIC
        }

        if (hasBlueNRGProtocol(nodeId)) {
            return UpgradeStrategy.NRG
        }

        val debugService = getDebugService(nodeId)
        if (debugService != null) {
            return UpgradeStrategy.DEBUG_CONSOLE
        }

        return UpgradeStrategy.UNKNOWN
    }

    private suspend fun getFwConsole(
        nodeService: NodeService
    ): FwConsole? {

        val nodeId = nodeService.getNode().device.address
        val advInfo = nodeService.getNode().advertiseInfo ?: return null
        val boardModel = advInfo.getBoardType()

        val updateMethod = getFwUpdateStrategy(nodeId)
        if (updateMethod == UpgradeStrategy.CHARACTERISTIC) {
            return CharacteristicFwUpgrade(
                bleManager = bleManager,
                coroutineScope = coroutineScope,
                nodeServiceConsumer = nodeServiceConsumer,
                nodeServiceProducer = nodeServiceProducer,
                catalogRepository = catalog
            )
        }

        if (updateMethod == UpgradeStrategy.NRG) {
            return CharacteristicWithACKFwUpgrade(coroutineScope, nodeServiceConsumer)
        }

        if (updateMethod == UpgradeStrategy.DEBUG_CONSOLE) {

            val boardFirmware = advInfo.getFwInfo()?.let {
                catalog.getFwDetailsNode(it.deviceId, it.fwId)
            }

            //Fast Fota is Enabled only for BlueST-SDK V2 board type
            val hasFastFota = boardFirmware?.fota?.type == BoardFotaType.FAST
            val fotaMaxChunkSize = boardFirmware?.fota?.maxChunkLength
            val fotaChunkDivisorConstraint = boardFirmware?.fota?.maxDivisorConstraint

            return when (boardModel) {
                Boards.Model.SENSOR_TILE_BOX -> {
                    if (boardFirmware == null) {
                        if (stBoxHasNewFwUpgradeProtocol(getFwVersion(nodeId))) {
                            //"Special" Fota for SensorTile.box official Fw
                            DebugFwUpgradeWithResume(coroutineScope, nodeService.debugService)
                        } else {
                            //Normal Fota never Fast Fota
                            DebugFwUpgrade(coroutineScope, nodeService.debugService)
                        }
                    } else {
                        DebugFwUpgrade(
                            coroutineScope,
                            nodeService.debugService,
                            hasFastFota,
                            fotaMaxChunkSize,
                            fotaChunkDivisorConstraint
                        )
                    }
                }
                else -> DebugFwUpgrade(
                    coroutineScope,
                    nodeService.debugService,
                    hasFastFota,
                    fotaMaxChunkSize,
                    fotaChunkDivisorConstraint
                )
            }
        }

        return null
    }

    override suspend fun getFwVersion(
        nodeId: String
    ): FwVersionBoard? {
        var result: FwVersionBoard? = null

        nodeServiceConsumer.getNodeService(nodeId)?.let { nodeService ->

            if (hasOTACharacteristics(nodeId)) {
                return when(nodeService.getNode().boardType) {
                    Boards.Model.WB55_NUCLEO_BOARD,
                    Boards.Model.WB5M_DISCOVERY_BOARD,
                    Boards.Model.WB55_USB_DONGLE_BOARD,
                    Boards.Model.WB15_NUCLEO_BOARD,
                    Boards.Model.WB1M_DISCOVERY_BOARD -> STM32WB_NEW_FW_UPGRADE_PROTOCOL

                    Boards.Model.WBA5X_NUCLEO_BOARD,
                    Boards.Model.WBA_DISCOVERY_BOARD-> STM32WBA_NEW_FW_UPGRADE_PROTOCOL

                    Boards.Model.WB0X_NUCLEO_BOARD -> STM32WB0X_NEW_FW_UPGRADE_PROTOCOL

                    else -> STM32WB_NEW_FW_UPGRADE_PROTOCOL
                }
            }

            nodeService.getNodeFeatures()
                .firstOrNull { it.name == ImageFeature.NAME }
                ?.let {
                    val readResults = nodeService.readFeature(it)
                    if (readResults.isNotEmpty() && readResults[0].data is ImageInfo) {
                        val imageInfo = readResults[0].data as ImageInfo
                        return FwVersionBoard(
                            boardName = DEFAULT_BOARD_NAME,
                            mcuType = DEFAULT_MCU_NAME,
                            majorVersion = imageInfo.protocolVersionMajor.value,
                            minorVersion = imageInfo.protocolVersionMinor.value,
                            patchVersion = 0
                        )
                    }
                }

            getDebugService(nodeId)?.let { debugService ->
                val buffer = StringBuffer()
                var errorCounter = 0

                try {
                    supervisorScope {
                        launch {
                            withTimeout(FW_VERSION_READ_TIMEOUT) {
                                debugService.getDebugMessages()
                                    .filter { it.isError.not() }
                                    .map { it.payload }
                                    .stateIn(this@launch, SharingStarted.Eagerly, initialValue = "")
                                    .onSubscription {
                                        debugService.write(COMMAND_GET_VERSION_BOARD_FW)
                                    }.collect { message ->
                                        buffer.append(message)

                                        if (buffer.isCompleteLine()) {
                                            buffer.trim()
                                            try {
                                                result = FwVersionBoard(buffer.toString())
                                                cancel()
                                            } catch (e: IllegalArgumentException) {
                                                buffer.setLength(0)

                                                errorCounter++

                                                if (errorCounter % FW_VERSION_READ_RETRY_EACH == 0) {
                                                    debugService.write(COMMAND_GET_VERSION_BOARD_FW)
                                                }
                                            }
                                        }
                                    }
                            }
                        }
                    }
                } catch (ex: TimeoutCancellationException) {
                    Log.d(TAG, ex.message, ex)
                }
            }
        }
        return result
    }

    private fun hasOTACharacteristics(nodeId: String): Boolean {
        val nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: return false
        return nodeService.getNodeFeatures()
            .firstOrNull {
                it.name == OTAReboot.NAME || it.name == OTAControl.NAME
            } != null
    }

    private fun hasBlueNRGProtocol(nodeId: String): Boolean {
        val nodeService = nodeServiceConsumer.getNodeService(nodeId) ?: return false
        return nodeService.getNodeFeatures()
            .firstOrNull {
                it.name == NewImageTUContentFeature.NAME || it.name == ExpectedImageTUSeqNumberFeature.NAME
            } != null
    }

    private fun stBoxHasNewFwUpgradeProtocol(version: FwVersion?): Boolean {
        return if (version is FwVersionBoard) {
            version.boardName == STBOX_NEW_FW_UPGRADE_PROTOCOL.boardName && version >= STBOX_NEW_FW_UPGRADE_PROTOCOL
        } else false
    }

    private fun getDebugService(nodeId: String): DebugService? {
        return nodeServiceConsumer.getNodeService(nodeId)?.debugService
    }

    private fun StringBuffer.isCompleteLine() = endsWith("\r\n") || endsWith("\n\r")

    private fun StringBuffer.trim() {
        if (length < 2) return

        delete(length - 2, length)

        var subStringPos = 0
        while (subStringPos != -1) {
            subStringPos = indexOf("\n");
            if (subStringPos != -1) {
                delete(subStringPos, subStringPos + 1)
            }
        }
    }
}
