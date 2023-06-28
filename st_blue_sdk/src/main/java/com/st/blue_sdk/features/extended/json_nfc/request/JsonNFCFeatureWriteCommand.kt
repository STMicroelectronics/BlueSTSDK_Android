package com.st.blue_sdk.features.extended.json_nfc.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.json_nfc.JsonNFC

class JsonNFCFeatureWriteCommand(feature: JsonNFC, val nfcCommand: JsonCommand) :
    FeatureCommand(
        feature = feature,
        commandId = JsonNFC.FEATURE_SEND_NFC_COMMAND
    )