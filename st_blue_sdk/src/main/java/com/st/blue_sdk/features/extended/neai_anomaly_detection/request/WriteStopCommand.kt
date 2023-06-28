package com.st.blue_sdk.features.extended.neai_anomaly_detection.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetection

class WriteStopCommand(
    feature: NeaiAnomalyDetection
) : FeatureCommand(feature = feature, commandId = STOP_COMMAND) {
    companion object {
        const val STOP_COMMAND = 0.toByte()
    }
}