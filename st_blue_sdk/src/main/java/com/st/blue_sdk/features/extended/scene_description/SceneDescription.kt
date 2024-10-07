package com.st.blue_sdk.features.extended.scene_description

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import com.st.blue_sdk.features.FeatureUpdate

class SceneDescription(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<SceneDescriptionInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {

        const val NAME = "Navigation Control"
        const val NUMBER_BYTES = 2
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<SceneDescriptionInfo> {
        return FeatureUpdate(
            featureName = name,
            readByte = data.size,
            timeStamp = timeStamp,
            rawData = data,
            data = SceneDescriptionInfo(name = "Place holder")
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}