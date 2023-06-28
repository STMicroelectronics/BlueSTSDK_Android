package com.st.blue_sdk.features.extended.binary_content

import com.st.blue_sdk.features.FeatureCommand

class BinaryContentCommand(feature: BinaryContent, val data: ByteArray) :
    FeatureCommand(
        feature = feature,
        commandId = BinaryContent.FEATURE_SEND_BINARY_CONTENT_COMMAND
    )