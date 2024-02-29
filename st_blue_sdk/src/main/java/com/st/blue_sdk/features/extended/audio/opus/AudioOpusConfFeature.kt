/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.audio.opus

import com.st.blue_sdk.features.*

class AudioOpusConfFeature(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<AudioOpusConf>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = false,
    isDataNotifyFeature = false
) {
    companion object {
        const val NAME = "AudioOpusConfFeature"
        private const val NUMBER_BYTES_1 = 4
        private const val NUMBER_BYTES_2 = 2

        const val BV_OPUS_CONF_CMD = 0x0B.toByte()
        const val BV_OPUS_CONTROL = 0x0A.toByte()
        private val BV_OPUS_ENABLE_NOTIF_REQ = 0x10.toByte()

        private const val OPUS_CONF_CMD_ID_INDEX = 0
        private const val OPUS_FRAME_SIZE_SUBINDEX = 1
        private const val OPUS_ONOFF_SUBINDEX = 1
        private const val OPUS_SAMPLING_FREQ_SUBINDEX = 2
        private const val OPUS_CHANNELS_SUBINDEX = 3

        private val EMPTY_DATA = AudioOpusConf(
            cmd = FeatureField(
                name = "cmd",
                value = null
            ),
            frameSize = FeatureField(
                name = "frameSize",
                value = null
            ),
            samplingFreq = FeatureField(
                name = "samplingFreq",
                value = null
            ),
            channels = FeatureField(
                name = "channels",
                value = null
            ),
            onOff = FeatureField(
                name = "onOff",
                value = null
            )
        )
    }

    private fun getFrameSize(data: ByteArray): Float {
        if (data[OPUS_CONF_CMD_ID_INDEX] == BV_OPUS_CONF_CMD) {
            when (data[OPUS_FRAME_SIZE_SUBINDEX]) {
                BV_OPUS_FRAME_SIZE_2_5 -> return 2.5f
                BV_OPUS_FRAME_SIZE_5 -> return 5f
                BV_OPUS_FRAME_SIZE_10 -> return 10f
                BV_OPUS_FRAME_SIZE_20 -> return 20f
                BV_OPUS_FRAME_SIZE_40 -> return 40f
                BV_OPUS_FRAME_SIZE_60 -> return 60f
            }
        }
        return OPUS_DEC_MS
    }

    private fun getSamplingFreq(data: ByteArray): Int {
        if (data[OPUS_CONF_CMD_ID_INDEX] == BV_OPUS_CONF_CMD) {
            when (data[OPUS_SAMPLING_FREQ_SUBINDEX]) {
                BV_OPUS_SAMPLING_FREQ_8 -> return 8000
                BV_OPUS_SAMPLING_FREQ_16 -> return 16000
                BV_OPUS_SAMPLING_FREQ_24 -> return 24000
                BV_OPUS_SAMPLING_FREQ_48 -> return 48000
            }
        }
        return OPUS_DEC_SAMPLING_FREQ
    }

    private fun getChannels(data: ByteArray): Int {
        if (data[OPUS_CONF_CMD_ID_INDEX] == BV_OPUS_CONF_CMD) {
            when (data[OPUS_CHANNELS_SUBINDEX]) {
                BV_OPUS_CHANNELS_1 -> return 1
                BV_OPUS_CHANNELS_2 -> return 2
            }
        }
        return OPUS_DEC_CHANNELS
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<AudioOpusConf> {
        require(data.size - dataOffset == NUMBER_BYTES_1 || data.size - dataOffset == NUMBER_BYTES_2)
        { "There are no $NUMBER_BYTES_1 or $NUMBER_BYTES_2 bytes available to read for $name feature" }

        return when {
            data[OPUS_CONF_CMD_ID_INDEX] == BV_OPUS_CONF_CMD -> {
                FeatureUpdate(
                    featureName = name,
                    rawData = data,
                    readByte = 4,
                    timeStamp = timeStamp,
                    data = EMPTY_DATA.copy(
                        cmd = FeatureField(name = "cmd", value = BV_OPUS_CONF_CMD),
                        frameSize = FeatureField(name = "frameSize", value = getFrameSize(data)),
                        samplingFreq = FeatureField(
                            name = "samplingFreq",
                            value = getSamplingFreq(data)
                        ),
                        channels = FeatureField(name = "channels", value = getChannels(data))
                    )
                )
            }
            data[OPUS_CONF_CMD_ID_INDEX] == BV_OPUS_CONTROL -> {
                FeatureUpdate(
                    featureName = name,
                    rawData = data,
                    readByte = 2,
                    timeStamp = timeStamp,
                    data = EMPTY_DATA.copy(
                        cmd = FeatureField(name = "cmd", value = BV_OPUS_CONTROL),
                        onOff = FeatureField(
                            name = "onOff",
                            //value = data[OPUS_ONOFF_SUBINDEX] == ENABLE_NOTIFICATION_COMMAND
                            value = data[OPUS_ONOFF_SUBINDEX]== BV_OPUS_ENABLE_NOTIF_REQ
                        )
                    )
                )
            }
            else -> FeatureUpdate(
                featureName = name,
                rawData = data,
                readByte = 0,
                timeStamp = timeStamp,
                data = EMPTY_DATA
            )
        }
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}