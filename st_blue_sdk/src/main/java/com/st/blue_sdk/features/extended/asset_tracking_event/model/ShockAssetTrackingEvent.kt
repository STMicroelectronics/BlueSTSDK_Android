package com.st.blue_sdk.features.extended.asset_tracking_event.model

data class ShockAssetTrackingEvent(
    val durationMSec: Float,
    val intensityG: FloatArray,
    val orientations: Array<AssetTrackingOrientationType>,
    val angles: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShockAssetTrackingEvent

        if (durationMSec != other.durationMSec) return false
        if (!intensityG.contentEquals(other.intensityG)) return false
        if (!orientations.contentEquals(other.orientations)) return false
        if (!angles.contentEquals(other.angles)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = durationMSec.hashCode()
        result = 31 * result + intensityG.contentHashCode()
        result = 31 * result + orientations.contentHashCode()
        result = 31 * result + angles.contentHashCode()
        return result
    }

    companion object {
        const val UNDEF_ANGLE: Float = 180.0f
    }
}