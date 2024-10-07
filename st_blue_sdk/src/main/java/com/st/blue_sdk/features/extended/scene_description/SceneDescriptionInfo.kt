package com.st.blue_sdk.features.extended.scene_description

import com.st.blue_sdk.logger.Loggable

data class SceneDescriptionInfo(val name: String,
): Loggable {
    override val logHeader: String = ""

    override val logValue: String = ""

    override fun toString(): String = "To Be Implemented"

    override val logDoubleValues: List<Double> = listOf()
}