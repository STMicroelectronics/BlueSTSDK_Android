/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models

object Boards {

    enum class Model {
        /** unknown board type  */
        GENERIC,

        /** STEVAL-WESU1 board  */
        STEVAL_WESU1,

        /** SensorTile board  */
        SENSOR_TILE,

        /** Blue Coin board  */
        BLUE_COIN,

        /** BlueNRG1 & BlueNRG2 ST eval board */
        STEVAL_IDB008VX,

        /** ST BlueNRG-Tile eval board */
        STEVAL_BCN002V1,

        /** SensorTile.Box board  */
        SENSOR_TILE_BOX,

        /** B-L475E-IOT01A board  */
        DISCOVERY_IOT01A,

        /** STEVAL-STWINKIT1 board  */
        STEVAL_STWINKIT1,

        /** STEVAL-STWINKT1B board  */
        STEVAL_STWINKT1B,

        /** B-L475E-IOT01A IoT Node 1.5  */
        B_L475E_IOT01A,

        /** B-U585I-IOT02A IoT Node 2.0  */
        B_U585I_IOT02A,

        /** Astra1 (WL + WB  */
        ASTRA1,

        /** SensorTile.Box PRO  */
        SENSOR_TILE_BOX_PRO,

        /** SensorTile.Box PROB  */
        SENSOR_TILE_BOX_PROB,

        /** STWIN.BOX  */
        STWIN_BOX,

        /** STWIN.BOXB  */
        STWIN_BOXB,

        /** Proteus  */
        PROTEUS,

        /** STDES-CBMLoRaBLE  */
        STDES_CBMLORABLE,

        /** WB Boards  */
        WB_BOARD,

        WBA_BOARD,

        /** Nucleo with STM32WB09 */
        NUCLEO_WB09KE,

        /** boards based on a x NUCLEO board  */
        NUCLEO, NUCLEO_F401RE, NUCLEO_L476RG, NUCLEO_L053R8, NUCLEO_F446RE
    } //Type

    fun getModelFromIdentifier(id: Int): Model {
        return when (val temp = (id and 0xFF)) {
            0x01 -> Model.STEVAL_WESU1
            0x02 -> Model.SENSOR_TILE
            0x03 -> Model.BLUE_COIN
            0x04 -> Model.STEVAL_IDB008VX
            0x05 -> Model.STEVAL_BCN002V1
            0x06 -> Model.SENSOR_TILE_BOX
            0x07 -> Model.DISCOVERY_IOT01A
            0x08 -> Model.STEVAL_STWINKIT1
            0x09 -> Model.STEVAL_STWINKT1B
            0x0A -> Model.B_L475E_IOT01A
            0x0B -> Model.B_U585I_IOT02A
            0x0C -> Model.ASTRA1
            0x0D -> Model.SENSOR_TILE_BOX_PRO
            0x0E -> Model.STWIN_BOX
            0x0F -> Model.PROTEUS
            0x10 -> Model.STDES_CBMLORABLE
            0x11 -> Model.SENSOR_TILE_BOX_PROB
            0x12 -> Model.STWIN_BOXB
            0x80 -> Model.NUCLEO
            0x7F -> Model.NUCLEO_F401RE
            0x7E -> Model.NUCLEO_L476RG
            0x7D -> Model.NUCLEO_L053R8
            0x7C -> Model.NUCLEO_F446RE
            0x8D -> Model.NUCLEO_WB09KE
            else -> {
                if (temp in 0x81..0x8A) {
                    Model.WB_BOARD
                } else {
                    if (temp in 0x8B..0x8C) {
                        Model.WBA_BOARD
                    } else {
                        Model.GENERIC
                    }
                }
            }
        }
    }
}