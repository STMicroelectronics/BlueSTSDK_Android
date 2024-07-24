package com.st.blue_sdk.features.extended.neai_extrapolation.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.neai_extrapolation.NeaiExtrapolation

class WriteStopExtrapolationCommand(
    feature: NeaiExtrapolation
) : FeatureCommand(feature = feature, commandId = STOP_EXTRAPOLATION_COMMAND) {
    companion object {
        const val STOP_EXTRAPOLATION_COMMAND = 0.toByte()
    }
}