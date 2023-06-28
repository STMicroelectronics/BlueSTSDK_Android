/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.audio.codec.adpcm

import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.audio.adpcm.AudioADPCMSync
import com.st.blue_sdk.services.audio.codec.AudioCodecManager
import com.st.blue_sdk.services.audio.codec.CodecType
import com.st.blue_sdk.services.audio.codec.DecodeParams
import com.st.blue_sdk.services.audio.codec.EncodeParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.experimental.and

class ADPCMManager constructor(
    private val coroutineScope: CoroutineScope
) : AudioCodecManager {

    override val type: CodecType = CodecType.ADPCM
    override val codecName: String = "ADPCM"
    override val channels: Int = 1
    override val samplingFreq: Int = 8000
    override val isAudioEnabled: Boolean = true

    private var intraFlag: Boolean = false
    private var predsample: Int = 0
    private var index: Short = 0
    private var params: ADPCMParams =
        ADPCMParams(index = 0, predSample = 0, samplingFreq = samplingFreq, channels = channels)

    private var configCollectionJob: Job? = null

    companion object {
        private val STEP_SIZE_TABLE = shortArrayOf(
            7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
            19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
            50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
            130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
            337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
            876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
            2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
            5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
        )
        private val INDEX_TABLE =
            byteArrayOf(-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8)
    }

    override suspend fun init(configFlow: Flow<FeatureUpdate<*>>): Boolean {

        configCollectionJob?.cancel()
        configCollectionJob = configFlow.onEach { update ->
            when (update.data) {
                is AudioADPCMSync -> {
                    val params = ADPCMParams(
                        samplingFreq = samplingFreq,
                        channels = channels,
                        index = update.data.index.value,
                        predSample = update.data.predSample.value
                    )
                    setDecodeParams(params = params)
                }
            }
        }.launchIn(coroutineScope)

        return true
    }

    override fun enable(enable: Boolean) = Unit

    override fun setEncodeParams(params: EncodeParams) = Unit

    private fun setDecodeParams(params: DecodeParams) {
        this.params = params as ADPCMParams
        intraFlag = true
    }

    override fun getDecodeParams(): DecodeParams = params

    override fun encode(data: ShortArray): ByteArray = byteArrayOf()

    override fun decode(data: ByteArray): ShortArray {
        val result = ShortArray(size = data.size * 2, init = { 0 })
        data.forEachIndexed { i, _ ->
            result[2 * i] = _decode(data[i] and 0x0F)
            result[(2 * i) + 1] = _decode(((data[i].toInt() shr 4) and 0x0F).toByte())
        }

        return result
    }

    private fun _decode(code: Byte): Short {
        var diffq: Int
        if (intraFlag) {
            predsample = params.predSample
            index = params.index
            reset()
        }
        val step: Short = STEP_SIZE_TABLE[index.toInt()]

        /* 2. inverse code into diff */
        diffq = step.toInt() shr 3
        if (code.toInt() and 4 != 0) {
            diffq += step.toInt()
        }
        if (code.toInt() and 2 != 0) {
            diffq += step.toInt() shr 1
        }
        if (code.toInt() and 1 != 0) {
            diffq += step.toInt() shr 2
        }

        /* 3. add diff to predicted sample*/
        if (code.toInt() and 8 != 0) {
            predsample -= diffq
        } else {
            predsample += diffq
        }

        /* check for overflow*/
        if (predsample > 32767) {
            predsample = 32767
        } else if (predsample < -32768) {
            predsample = -32768
        }

        /* 4. find new quantizer step size */
        index = (index + (INDEX_TABLE[code.toInt()])).toShort()
        /* check for overflow*/
        if (index < 0) {
            index = 0
        }
        if (index > 88) {
            index = 88
        }

        /* 5. save predict sample and index for next iteration */
        /* done! static variables */

        /* 6. return new speech sample*/
        return predsample.toShort()
    }

    override fun reset() {
        intraFlag = false
    }

    override fun destroy() {
        configCollectionJob?.cancel()
    }
}