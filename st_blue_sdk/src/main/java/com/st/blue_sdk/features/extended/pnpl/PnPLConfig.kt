package com.st.blue_sdk.features.extended.pnpl

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.pnpl.model.PnPLDevice
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class PnPLConfig(
    val deviceStatus: FeatureField<PnPLDevice?>
) : Loggable {
    override val logHeader = deviceStatus.logHeader

    override val logValue: String = deviceStatus.logValue
}
