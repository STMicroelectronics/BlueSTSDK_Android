package com.st.blue_sdk.features.extended.ispu_control

import com.st.blue_sdk.features.FeatureCommand

class ISPUControlSendJSONCommand(feature: ISPUControlFeature, val data: ByteArray) :
    FeatureCommand(
        feature = feature,
        commandId = ISPUControlFeature.FEATURE_SEND_ISPU_CONTROL_JSON_COMMAND
    )