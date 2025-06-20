package com.st.blue_sdk.features.extended.asset_tracking_event

import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventData
import com.st.blue_sdk.features.extended.asset_tracking_event.model.ShockAssetTrackingEvent.Companion.UNDEF_ANGLE
import com.st.blue_sdk.logger.Loggable
import com.st.blue_sdk.features.FeatureField

data class AssetTrackingEventInfo(
    val event: FeatureField<AssetTrackingEventData>
) : Loggable {
    override val logHeader: String = "type, heightCm, durationMSec, intensityX, intensityY, intensityZ, Current, PowerIndex"

    override val logValue: String =
        "${event.value.type}, ${event.value.fall?.heightCm ?: "null"}," +
                "${event.value.shock?.durationMSec ?: "null"}," +
                "${event.value.shock?.intensityG[0] ?: "null"}," +
                "${event.value.shock?.intensityG[1] ?: "null"}," +
                "${event.value.shock?.intensityG[2] ?: "null"}," +
                "${event.value.status?.current ?: "null"}," +
                "${event.value.status?.powerIndex ?: "null"}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\ttype = ${event.value.type}\n")
        event.value.fall?.let { fall ->
            sampleValue.append("\theight = ${fall.heightCm} [cm]")
        }
        event.value.shock?.let { shock ->
            sampleValue.append("\tduration = ${shock.durationMSec} [mSec]\n")
            sampleValue.append("\tintensity = ${shock.intensityG.contentToString()} [g]\n")
            if(shock.orientations.isNotEmpty()) {
                sampleValue.append("\torientations = ${shock.orientations.contentToString()}\n")
            }
            if(shock.angles.isNotEmpty()) {
                sampleValue.append("\tangles = [")
                shock.angles.forEachIndexed { index, angle ->
                    if(angle != UNDEF_ANGLE) {
                        sampleValue.append("$angle°")
                    } else {
                        sampleValue.append("undef")
                    }
                    if(index<shock.angles.size-1) {
                        sampleValue.append(", ")
                    }
                }
                sampleValue.append("]")
            }
        }

        event.value.status?.let { status ->
            sampleValue.append("\tCurrent = ${status.current} [µA]\n")
            sampleValue.append("\tPowerIndex = ${status.powerIndex}\n")
        }

        sampleValue.append("\n")
        return sampleValue.toString()
    }

    override val logDoubleValues: List<Double> = listOf()
}