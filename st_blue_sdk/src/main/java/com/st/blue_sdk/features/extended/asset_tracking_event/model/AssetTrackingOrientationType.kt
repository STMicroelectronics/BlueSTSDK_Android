package com.st.blue_sdk.features.extended.asset_tracking_event.model

import kotlin.experimental.and

enum class AssetTrackingOrientationType {
    Undef,
    Positive,
    Negative;

    companion object {
        fun getAssetTrackingOrientationCode(type: Short) = when ((type and 0xFF).toInt()) {
            0x00 -> Undef
            0x01 -> Positive
            0x02 -> Negative
            else -> Undef
        }
    }

//    override fun toString(): String {
//        return super.toString()
//    }
}