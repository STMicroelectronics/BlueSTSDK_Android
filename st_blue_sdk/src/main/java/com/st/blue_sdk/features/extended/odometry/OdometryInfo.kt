/*
 * Copyright (c) 2023(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.odometry

import android.annotation.SuppressLint
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class OdometryInfo(
    val x: FeatureField<Float>,
    val y: FeatureField<Float>,
    val theta: FeatureField<Float>,
    val magneticDirection : FeatureField<Float>? = null
) : Loggable {
    // Updated logHeader, logValue, and logDoubleValues for theta
    override val logHeader: String = "${x.logHeader}, ${y.logHeader}, ${theta.logHeader}, ${magneticDirection?.logHeader}"

    override val logValue: String = "${x.logValue}, ${y.logValue}, ${theta.logValue}, ${magneticDirection?.logValue}"

    override val logDoubleValues: List<Double> =
        listOf(x.value.toDouble(), y.value.toDouble(), theta.value.toDouble(), magneticDirection?.value?.toDouble() ?: 0.0)

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${x.name} = ${x.value} ${x.unit}\n")
        sampleValue.append("\t${y.name} = ${y.value} ${y.unit}\n")
        sampleValue.append("\t${theta.name} = ${theta.value} ${theta.unit}\n") // Updated for theta

        // Only append this line if magneticDirection is NOT null
        magneticDirection?.let {
            sampleValue.append("\t${it.name} = ${it.value} ${it.unit}\n")
        }
        return sampleValue.toString()
    }
}