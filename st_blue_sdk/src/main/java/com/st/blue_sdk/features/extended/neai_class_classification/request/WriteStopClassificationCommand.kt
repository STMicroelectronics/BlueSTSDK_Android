package com.st.blue_sdk.features.extended.neai_class_classification.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.neai_class_classification.NeaiClassClassification

class WriteStopClassificationCommand(
    feature: NeaiClassClassification
) : FeatureCommand(feature = feature, commandId = STOP_CLASSIFICATION_COMMAND) {
    companion object {
        const val STOP_CLASSIFICATION_COMMAND = 0.toByte()
    }
}