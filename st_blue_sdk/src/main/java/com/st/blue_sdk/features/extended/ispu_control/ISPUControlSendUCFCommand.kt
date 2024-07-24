package com.st.blue_sdk.features.extended.ispu_control

import com.st.blue_sdk.features.FeatureCommand

class ISPUControlSendUCFCommand(feature: ISPUControlFeature, val data: ByteArray) :
    FeatureCommand(
        feature = feature,
        commandId = ISPUControlFeature.FEATURE_SEND_ISPU_CONTROL_UCF_COMMAND
    )