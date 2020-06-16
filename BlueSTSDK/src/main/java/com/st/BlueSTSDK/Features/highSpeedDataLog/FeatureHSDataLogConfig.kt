package com.st.BlueSTSDK.Features.highSpeedDataLog

import com.st.BlueSTSDK.Debug
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.Field
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.*
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.*
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceParser.Companion.createHSDCommandJSON
import com.st.BlueSTSDK.Node
import java.io.ByteArrayOutputStream

class FeatureHSDataLogConfig constructor(n: Node) :
        Feature(FEATURE_NAME, n, arrayOf(STWINCONFIG_FIELD)) {
    private var mSTWINTransportDecoder = STWINTransportProtocol()

    //NOTE -- Model Classes Parsing from received Samples
    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {
        val commandFrame = mSTWINTransportDecoder.decapsulate(data)
        if (commandFrame != null) {
            val responseObj = DeviceParser.getJsonObj(commandFrame)
            val commandData = ConfigSample(
                    DeviceParser.extractDevice(responseObj),
                    DeviceParser.extractDeviceStatus(responseObj)
            )
            return ExtractResult(commandData, data.size)
        }
        return ExtractResult(null, data.size)
    }

    private fun sendWrite(bytesToSend: ByteArray, onSendComplete: Runnable?) {
        var byteSend = 0
        while (bytesToSend.size - byteSend > 20) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, byteSend + 20))
            byteSend += 20
        }
        if (byteSend != bytesToSend.size) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, bytesToSend.size),onSendComplete)
        } //if
    }

    private class ConfigSample(val device: Device?, val status: DeviceStatus?) :
            Sample(emptyArray(), arrayOf(STWINCONFIG_FIELD))

    fun sendGetCmd(command: HSDGetCmd) = sendCommand(command)

    fun sendSetCmd(command: HSDSetCmd, onSendComplete: Runnable?=null) = sendCommand(command,onSendComplete)

    fun sendControlCmd(command: HSDControlCmd) = sendCommand(command)

    private fun sendCommand(command:HSDCmd, onSendComplete: Runnable?=null){
        sendWrite(mSTWINTransportDecoder.encapsulate(createHSDCommandJSON(command)),onSendComplete)
    }

    private class STWINTransportProtocol {
        private var currentMessage: ByteArrayOutputStream? = null
        fun decapsulate(byteCommand: ByteArray): ByteArray? {
            if (byteCommand[0] == TP_START_PACKET) {
                currentMessage = ByteArrayOutputStream().apply {
                    write(byteCommand, 1, byteCommand.size - 1)
                }
            } else if (byteCommand[0] == TP_START_END_PACKET) {
                currentMessage = ByteArrayOutputStream()
                //TODO: the data are not copied
                //Log.e("FeatureSTWINConfig","currentMessage: " + byteToString(currentMessage.toByteArray()));
                return currentMessage!!.toByteArray()
            } else if (byteCommand[0] == TP_MIDDLE_PACKET) {
                currentMessage?.write(byteCommand, 1, byteCommand.size - 1)
            } else if (byteCommand[0] == TP_END_PACKET) {
                if (currentMessage != null) {
                    currentMessage!!.write(byteCommand, 1, byteCommand.size - 1)
                    //Log.e("FeatureSTWINConfig","currentMessage: " + byteToString(currentMessage.toByteArray()));
                    return currentMessage!!.toByteArray()
                }
            }
            return null
        }

        private fun toBytes(s: Short): ByteArray {
            return byteArrayOf((s.toInt() and 0x00FF).toByte(), ((s.toInt() and 0xFF00) shr (8)).toByte())
        }

        fun encapsulate(string: String?): ByteArray {
            val byteCommand = Debug.stringToByte(string)
            var head = TP_START_PACKET
            val baos = ByteArrayOutputStream()
            var cnt = 0
            val codedDataLength = byteCommand.size
            val mtuSize = MTU_SIZE
            while (cnt < codedDataLength) {
                var size = Math.min(mtuSize - 1, codedDataLength - cnt)
                if (codedDataLength - cnt <= mtuSize - 1) {
                    head = if (cnt == 0) {
                        if(codedDataLength - cnt <= mtuSize - 3) {
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
            return baos.toByteArray()
        }

        companion object {
            /** Transport Protocol  */
            private const val MTU_SIZE = 20
            private const val TP_START_PACKET = 0x00.toByte()
            private const val TP_START_END_PACKET = 0x20.toByte()
            private const val TP_MIDDLE_PACKET = 0x40.toByte()
            private const val TP_END_PACKET = 0x80.toByte()
        }
    }

    companion object {
        private const val FEATURE_NAME = "HSDataLogConfig"
        private const val FEATURE_DATA_NAME = "ConfigJson"
        private val STWINCONFIG_FIELD = Field(FEATURE_DATA_NAME, null, Field.Type.ByteArray, Byte.MAX_VALUE, Byte.MIN_VALUE)

        fun isLogging(sample: Sample): Boolean? {
            return getDeviceStatus(sample)?.isSDLogging
        }

        fun isSDCardInserted(sample: Sample): Boolean? {
            return getDeviceStatus(sample)?.isSDCardInserted
        }

        //NOTE -- Model Classes Parsing from received Samples
        fun getDeviceConfig(sample: Sample): Device? {
            val hsdSample = sample as? ConfigSample ?: return null
            return hsdSample.device
        }

        fun getDeviceInfo(sample: Sample): DeviceInfo? {
            return getDeviceConfig(sample)?.deviceInfo
        }

        fun getDeviceTagConfig(sample: Sample): TagConfig? {
            return getDeviceConfig(sample)?.tags
        }

        fun getDeviceStatus(sample: Sample): DeviceStatus? {
            val hsdSample = sample as? ConfigSample ?: return null
            return hsdSample.status
        }
    }
}