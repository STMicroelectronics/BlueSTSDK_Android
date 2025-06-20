package com.st.blue_sdk.features.extended.asset_tracking_event.model
import kotlin.experimental.and

enum class AssetTrackingEventType {
    Reset,
    Fall,
    Shock,
    Stationary,
    Motion,
    Null;

    companion object {
        fun getAssetTrackingEvent(type: Short) = when ((type and 0xFF).toInt()) {
            0x00 -> Reset
            0x01 -> Fall
            0x02 -> Shock
            0x03 -> Stationary
            0x04 -> Motion
            else -> Null
        }
    }
}
data class AssetTrackingEventData(
    val type: AssetTrackingEventType,
    val fall:FallAssetTrackingEvent? = null,
    val shock:ShockAssetTrackingEvent? = null,
    val status: StatusAssetTrackingEvent? = null
)