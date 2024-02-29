/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.tof_multi_object

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class ToFMultiObjectInfo(
    val nObjsFound: FeatureField<Short>,
    val distanceObjs: List<FeatureField<Short>>,
    val presenceFound: FeatureField<Short>
) : Loggable {
    override val logHeader: String =
        (listOf(nObjsFound) + distanceObjs + listOf(presenceFound)).joinToString(separator = ", ") { it.logHeader }

    override val logValue: String =
        (listOf(nObjsFound) + distanceObjs + listOf(presenceFound)).joinToString(separator = ", ") { it.logValue }

    override val logDoubleValues: List<Double> = listOf(nObjsFound.value.toDouble())

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\tFound ${nObjsFound.value} ${nObjsFound.name}:\n")
        distanceObjs.forEach { sampleValue.append("\t\t${it.name} = ${it.value} ${it.unit}\n") }
        sampleValue.append("\tFound ${presenceFound.value} ${presenceFound.name}\n")
        return sampleValue.toString()
    }
}
