package com.st.blue_sdk.features.extended.neai_extrapolation.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.neai_extrapolation.NeaiExtrapolation

class WriteStartExtrapolationCommand(
    feature: NeaiExtrapolation
) : FeatureCommand(feature = feature, commandId = START_EXTRAPOLATION_COMMAND) {
    companion object {
        const val START_EXTRAPOLATION_COMMAND = 1.toByte()
    }
}