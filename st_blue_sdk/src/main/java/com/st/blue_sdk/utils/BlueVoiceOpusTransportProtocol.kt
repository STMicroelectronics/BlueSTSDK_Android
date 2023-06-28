/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.utils

import java.io.ByteArrayOutputStream
import kotlin.math.min

/**
 * class that unpack the messages encapsulated into the BlueVoice transport protocol.
 * a package will be returned when frameSize bytes will be received
 */
internal class BlueVoiceOpusTransportProtocol(private val frameSize: Int) {

    private var partialData: ByteArray = byteArrayOf()

    /**
     * extract the data from the dataPacakge, return null if the frame is not completed or an array
     * of frameSize bytes
     */
    fun unpackData(dataPacket: ByteArray): ByteArray? {
        when (dataPacket[0]) {
            BV_OPUS_TP_START_PACKET -> {
                resetPartialData()
                appendPackage(dataPacket)
                return null
            }
            BV_OPUS_TP_START_END_PACKET -> {
                resetPartialData()
                appendPackage(dataPacket)
                return partialData
            }
            BV_OPUS_TP_MIDDLE_PACKET -> {
                appendPackage(dataPacket)
                return null
            }
            BV_OPUS_TP_END_PACKET -> {
                appendPackage(dataPacket)
                return partialData
            }
        }
        return null
    }

    private fun resetPartialData() {
        partialData = byteArrayOf()
    }


    private fun appendPackage(audioSample: ByteArray) {
        partialData = partialData.plus(audioSample.copyOfRange(1, audioSample.size))
    }

    companion object {

        /**
         * Split the codedData array into a list of messages of maxLength encapsulating the
         * data into the BlueVoice transport protocol
         */
        fun packData(codedData: ByteArray, maxLength: Int): List<ByteArray> {
            var head = BV_OPUS_TP_START_PACKET
            val baos = ByteArrayOutputStream()
            var cnt = 0
            var size: Int
            val codedDataLength = codedData.size
            val nPackage = (codedDataLength + (maxLength - 1) / 2) / (maxLength - 1)
            val packData = ArrayList<ByteArray>(nPackage)
            while (cnt < codedDataLength) {
                size = min(maxLength - 1, codedDataLength - cnt)
                if (codedDataLength - cnt <= maxLength - 1) {
                    head = if (cnt == 0) {
                        BV_OPUS_TP_START_END_PACKET
                    } else {
                        BV_OPUS_TP_END_PACKET
                    }
                }
                when (head) {
                    BV_OPUS_TP_START_PACKET -> {
                        /*First part of an Opus packet*/
                        baos.reset()
                        baos.write(head.toInt())
                        baos.write(codedData, 0, maxLength - 1)
                        packData.add(baos.toByteArray())
                        head = BV_OPUS_TP_MIDDLE_PACKET
                    }
                    BV_OPUS_TP_START_END_PACKET -> {
                        /*First and last part of an Opus packet*/
                        baos.reset()
                        baos.write(head.toInt())
                        baos.write(codedData, 0, codedDataLength)
                        packData.add(baos.toByteArray())
                        head = BV_OPUS_TP_START_PACKET
                    }
                    BV_OPUS_TP_MIDDLE_PACKET -> {
                        /*Central part of an Opus packet*/
                        baos.reset()
                        baos.write(head.toInt())
                        baos.write(codedData, cnt, maxLength - 1)
                        packData.add(baos.toByteArray())
                    }
                    BV_OPUS_TP_END_PACKET -> {
                        /*Last part of an Opus packet*/
                        baos.reset()
                        baos.write(head.toInt())
                        baos.write(codedData, cnt, codedDataLength - cnt)
                        packData.add(baos.toByteArray())
                        head = BV_OPUS_TP_START_PACKET
                    }
                }
                /*length variables update*/
                cnt += size
            }
            return packData
        }

        /** Opus Transport Protocol  */
        private const val BV_OPUS_TP_START_PACKET = 0x00.toByte()
        private const val BV_OPUS_TP_START_END_PACKET = 0x20.toByte()
        private const val BV_OPUS_TP_MIDDLE_PACKET = 0x40.toByte()
        private const val BV_OPUS_TP_END_PACKET = 0x80.toByte()
    }
}