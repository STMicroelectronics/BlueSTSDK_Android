/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.qvar

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable
import kotlinx.serialization.Serializable

@Serializable
data class QVARInfo(
    val qvar: FeatureField<Long?>,
    val flag: FeatureField<Byte?>,
    val dqvar: FeatureField<Long?>,
    val param: FeatureField<Long?>,
) : Loggable {
    override val logHeader: String =
        "${qvar.logHeader}, ${flag.logHeader}, ${dqvar.logHeader}, ${param.logHeader}"

    override val logValue: String =
        "${qvar.logValue}, ${flag.logValue}, ${dqvar.logValue}, ${param.logValue}"

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${qvar.name} = ${qvar.value}\n")
        flag.let { sampleValue.append("\t${flag.name} = ${flag.value}\n") }
        dqvar.let { sampleValue.append("\t${dqvar.name} = ${dqvar.value}\n") }
        param.let { sampleValue.append("\t${param.name} = ${param.value}\n") }
        return sampleValue.toString()
    }
}
