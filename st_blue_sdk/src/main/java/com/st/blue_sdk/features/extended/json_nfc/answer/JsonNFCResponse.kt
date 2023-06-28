package com.st.blue_sdk.features.extended.json_nfc.answer

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class JsonNFCResponse(
    val supportedModes: FeatureField<JsonReadModesResult?>
) : Loggable {
    override val logHeader = supportedModes.logHeader

    override val logValue: String = supportedModes.logValue
}
