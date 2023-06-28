package com.st.blue_sdk.features.extended.neai_anomaly_detection.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetection

class WriteLearningCommand(
    feature: NeaiAnomalyDetection
) : FeatureCommand(feature = feature, commandId = LEARNING_COMMAND) {
    companion object {
        const val LEARNING_COMMAND = 1.toByte()
    }
}