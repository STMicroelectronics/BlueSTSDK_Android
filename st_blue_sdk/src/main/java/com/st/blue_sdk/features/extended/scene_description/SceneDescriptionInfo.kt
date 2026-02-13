package com.st.blue_sdk.features.extended.scene_description

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.scene_description.model.SceneDescriptorData
import com.st.blue_sdk.logger.Loggable

data class SceneDescriptionInfo(
    val payload: FeatureField<SceneDescriptorData?>
): Loggable {
    override val logHeader = payload.logHeader

    override val logValue = payload.logValue

    override val logDoubleValues: List<Double> = listOf()

    override fun toString(): String {
        val sampleValue = StringBuilder()

        payload.value?.let { data ->
            data.tofZones?.forEachIndexed { index, zoneList ->
                val zoneString = zoneList.joinToString(", ", "[", "]")
                sampleValue.append("\t${payload.name}_$index = $zoneString\n")
            }
        } ?: run {

            sampleValue.append("\t${payload.name} = null\n")
        }

        return sampleValue.toString()
    }
}