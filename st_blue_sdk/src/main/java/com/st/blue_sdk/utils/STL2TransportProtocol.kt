/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.utils

import android.util.Log
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import kotlin.math.min

class STL2TransportProtocol(private var maxPayloadSize: Int = 20) {

    private var currentMessage: ByteArrayOutputStream? = null
    private var bytesRec=0
    private var numberPackets=0

    fun decapsulate(byteCommand: ByteArray): ByteArray? {
        if (byteCommand[0] == TP_START_PACKET) {
            currentMessage = ByteArrayOutputStream().apply {
                write(byteCommand, 1, byteCommand.size - 1)
            }
//            val startMessage = currentMessage?.toByteArray()?.let { String(it) } ?: ""
//            Log.d(TAG, "startMessage: ${String(byteCommand)}")
            bytesRec=byteCommand.size-1
            numberPackets=1
//            Log.d(TAG, "startMessage: ${currentMessage!!.size()} ...$totalSize")
        } else if (byteCommand[0] == TP_START_END_PACKET) {
//            val lastMessage = currentMessage?.toByteArray()?.let { String(it) } ?: ""
//            Log.d(TAG, "discardMessage: $lastMessage")
//            currentMessage = ByteArrayOutputStream()
            bytesRec=byteCommand.size-1
            numberPackets=1
            currentMessage = ByteArrayOutputStream().apply {
                write(byteCommand, 1, byteCommand.size - 1)
            }
            return currentMessage!!.toByteArray()
        } else if (byteCommand[0] == TP_MIDDLE_PACKET) {
            currentMessage?.write(byteCommand, 1, byteCommand.size - 1)
//            val currentMessageStr = currentMessage?.toByteArray()?.let { String(it) } ?: ""
//            Log.d(TAG, "currentMessageStr: $currentMessageStr")
            bytesRec+=byteCommand.size-1
            numberPackets++
//            Log.d(TAG, "middleMessage: ${currentMessage!!.size()} ...$totalSize")
        } else if (byteCommand[0] == TP_END_PACKET) {
            if (currentMessage != null) {
                currentMessage!!.write(byteCommand, 1, byteCommand.size - 1)
//                val finalMessage = currentMessage?.toByteArray()?.let { String(it) } ?: ""
//                Log.d(TAG, "finalMessage ${String(byteCommand)}")
                bytesRec+=byteCommand.size-1
                numberPackets++
//                Log.d(TAG, "finalMessage: ${currentMessage!!.size()} ...$totalSize")
                return currentMessage!!.toByteArray()
            }
        }
        return null
    }

    fun setMaxPayLoadSize(maxPayLoad: Int) {maxPayloadSize = maxPayLoad}

    fun getMaxPayLoadSize() = maxPayloadSize
    
    fun getNumberPackets() = numberPackets
    fun getBytesReceived() = bytesRec

    private fun toBytes(s: Short): ByteArray {
        return byteArrayOf(
            (s.toInt() and 0x00FF).toByte(),
            ((s.toInt() and 0xFF00) shr (8)).toByte()
        )
    }

    fun encapsulate(string: String?): ByteArray {
        val byteCommand = string?.toByteArray(CHARSET) ?: byteArrayOf()
        var head = TP_START_PACKET
        val baos = ByteArrayOutputStream()
        var cnt = 0
        val codedDataLength = byteCommand.size
        val mtuSize = maxPayloadSize
        while (cnt < codedDataLength) {
            var size = min(mtuSize - 1, codedDataLength - cnt)
            if (codedDataLength - cnt <= mtuSize - 1) {
                head = if (cnt == 0) {
                    if (codedDataLength - cnt <= mtuSize - 3) {
                        TP_START_END_PACKET
                    } else {
                        TP_START_PACKET
                    }
                } else {
                    TP_END_PACKET
                }
            }
            when (head) {
                TP_START_PACKET -> {

                    /*First part of a packet*/baos.write(head.toInt())
                    baos.write(toBytes(codedDataLength.toShort()).reversedArray())
                    baos.write(byteCommand, 0, mtuSize - 3)
                    size = mtuSize - 3
                    head = TP_MIDDLE_PACKET
                }
                TP_START_END_PACKET -> {

                    /*First and last part of a packet*/baos.write(head.toInt())
                    baos.write(toBytes(codedDataLength.toShort()).reversedArray())
                    baos.write(byteCommand, 0, codedDataLength)
                    size = codedDataLength
                    head = TP_START_PACKET
                }
                TP_MIDDLE_PACKET -> {

                    /*Central part of a packet*/baos.write(head.toInt())
                    baos.write(byteCommand, cnt, mtuSize - 1)
                }
                TP_END_PACKET -> {

                    /*Last part of a packet*/baos.write(head.toInt())
                    baos.write(byteCommand, cnt, codedDataLength - cnt)
                    head = TP_START_PACKET
                }
            }
            /*length variables update*/cnt += size
        }
        //Log.i("STL2TransportProtocol","baos.size="+baos.size()+"cnt="+cnt)
        return baos.toByteArray()
    }

    fun encapsulate(byteCommand: ByteArray): ByteArray {
        var head = TP_START_PACKET
        val baos = ByteArrayOutputStream()
        var cnt = 0
        val codedDataLength = byteCommand.size
        val mtuSize = maxPayloadSize
        while (cnt < codedDataLength) {
            var size = Math.min(mtuSize - 1, codedDataLength - cnt)
            if (codedDataLength - cnt <= mtuSize - 1) {
                head = if (cnt == 0) {
                    if (codedDataLength - cnt <= mtuSize - 3) {
                        TP_START_END_PACKET
                    } else {
                        TP_START_PACKET
                    }
                } else {
                    TP_END_PACKET
                }
            }
            when (head) {
                TP_START_PACKET -> {
                    //Log.i("STL2TransportProtocol","TP_START_PACKET")
                    /*First part of a packet*/baos.write(head.toInt())
                    baos.write(toBytes(codedDataLength.toShort()).reversedArray())
                    baos.write(byteCommand, 0, mtuSize - 3)
                    size = mtuSize - 3
                    head = TP_MIDDLE_PACKET
                }
                TP_START_END_PACKET -> {
                    //Log.i("STL2TransportProtocol","TP_START_END_PACKET")
                    /*First and last part of a packet*/baos.write(head.toInt())
                    baos.write(toBytes(codedDataLength.toShort()).reversedArray())
                    baos.write(byteCommand, 0, codedDataLength)
                    size = codedDataLength
                    head = TP_START_PACKET
                }
                TP_MIDDLE_PACKET -> {
                    //Log.i("STL2TransportProtocol","TP_MIDDLE_PACKET")
                    /*Central part of a packet*/baos.write(head.toInt())
                    baos.write(byteCommand, cnt, mtuSize - 1)
                }
                TP_END_PACKET -> {
                    //Log.i("STL2TransportProtocol","TP_END_PACKET")
                    /*Last part of a packet*/baos.write(head.toInt())
                    baos.write(byteCommand, cnt, codedDataLength - cnt)
                    head = TP_START_PACKET
                }
            }
            /*length variables update*/cnt += size
            //Log.i("STL2TransportProtocol","baos.size="+baos.size()+"cnt="+cnt)
        }
        //Log.i("STL2TransportProtocol","End baos.size="+baos.size()+"cnt="+cnt)
        return baos.toByteArray()
    }

    companion object {
        private const val TAG = "STL2TransportProtocol"
        private const val TP_START_PACKET = 0x00.toByte()
        private const val TP_START_END_PACKET = 0x20.toByte()
        private const val TP_MIDDLE_PACKET = 0x40.toByte()
        private const val TP_END_PACKET = 0x80.toByte()
        private val CHARSET = StandardCharsets.ISO_8859_1
    }
}