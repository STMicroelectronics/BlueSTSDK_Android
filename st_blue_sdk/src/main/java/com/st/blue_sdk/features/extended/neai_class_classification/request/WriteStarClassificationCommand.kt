package com.st.blue_sdk.features.extended.neai_class_classification.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.neai_class_classification.NeaiClassClassification

class WriteStarClassificationCommand(
    feature: NeaiClassClassification
) : FeatureCommand(feature = feature, commandId = START_CLASSIFICATION_COMMAND) {
    companion object {
        const val START_CLASSIFICATION_COMMAND = 1.toByte()
    }
}