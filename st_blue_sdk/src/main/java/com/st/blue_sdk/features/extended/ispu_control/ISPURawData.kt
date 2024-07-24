package com.st.blue_sdk.features.extended.ispu_control

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class ISPURawData(
    val data: FeatureField<ByteArray?>,
    val bytesRec: FeatureField<Int>,
    val numberPackets: FeatureField<Int>
) : Loggable {
    override val logHeader = data.logHeader
    override val logValue = data.logValue

    override val logDoubleValues: List<Double> = listOf()
}
