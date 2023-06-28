package com.st.blue_sdk.features.extended.neai_anomaly_detection.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetection

class WriteResetKnowledgeCommand(
    feature: NeaiAnomalyDetection
) : FeatureCommand(feature = feature, commandId = RESET_KNOWLEDGE_COMMAND) {
    companion object {
        const val RESET_KNOWLEDGE_COMMAND = 0xFF.toByte()
    }
}