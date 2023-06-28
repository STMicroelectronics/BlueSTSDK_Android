/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorStatus(
    //NOTE here there may be parameters in the future
    @SerialName("subSensorStatus") val subSensorStatusList: List<SubSensorStatus>,
    val paramsLocked: Boolean = false
) {
    fun getSubSensorStatus(subSensorId: Int): SubSensorStatus? {
        return subSensorStatusList.getOrNull(subSensorId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorStatus

        if (subSensorStatusList != other.subSensorStatusList) return false
        if (paramsLocked != other.paramsLocked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = subSensorStatusList.hashCode()
        result = 31 * result + paramsLocked.hashCode()
        return result
    }
}

