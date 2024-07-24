package com.st.blue_sdk.features.extended.neai_extrapolation

import android.util.Log
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.extended.neai_extrapolation.model.NeaiExtrapolationData
import com.st.blue_sdk.features.extended.neai_extrapolation.request.WriteStartExtrapolationCommand
import com.st.blue_sdk.features.extended.neai_extrapolation.request.WriteStopExtrapolationCommand
import kotlinx.serialization.json.Json

class NeaiExtrapolation(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<NeaiExtrapolationInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    isDataNotifyFeature = true,
    hasTimeStamp = false
) {
    companion object {
        const val NAME = "NEAI Extrapolation"
        private val TAG = NeaiExtrapolation::class.simpleName
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<NeaiExtrapolationInfo> {

        val text = data.toString(Charsets.UTF_8).dropLast(1)

        val update = try {
            json.decodeFromString<NeaiExtrapolationData>(text)
        } catch (e: Exception) {
            Log.d(TAG, e.stackTraceToString())
            null
        }

        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = NeaiExtrapolationInfo(extrapolation = update)
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is WriteStopExtrapolationCommand -> packCommandRequest(
                featureBit,
                WriteStopExtrapolationCommand.STOP_EXTRAPOLATION_COMMAND,
                byteArrayOf()
            )

            is WriteStartExtrapolationCommand -> packCommandRequest(
                featureBit,
                WriteStartExtrapolationCommand.START_EXTRAPOLATION_COMMAND,
                byteArrayOf()
            )

            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null

}