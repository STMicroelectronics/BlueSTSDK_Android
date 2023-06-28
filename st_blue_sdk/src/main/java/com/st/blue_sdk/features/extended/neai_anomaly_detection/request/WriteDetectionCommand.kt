package com.st.blue_sdk.features.extended.neai_anomaly_detection.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetection

class WriteDetectionCommand(
    feature: NeaiAnomalyDetection
) : FeatureCommand(feature = feature, commandId = DETECTION_COMMAND) {
    companion object {
        const val DETECTION_COMMAND = 2.toByte()
    }
}