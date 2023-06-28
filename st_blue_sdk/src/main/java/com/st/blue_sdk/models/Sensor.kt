/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models

import kotlinx.serialization.Serializable

@Serializable
data class Sensor(
    val id: Int,
    val name: String,
    val sensorDescriptor: SensorDescriptor,
    val sensorStatus: SensorStatus
) : Comparable<Sensor> {
    override fun compareTo(other: Sensor): Int = id - other.id

    fun getSubSensorStatusForId(id: Int): SubSensorStatus? {
        val index = sensorDescriptor.subSensorDescriptors.indexOfFirst { it.id == id }
        return if (index >= 0) {
            sensorStatus.subSensorStatusList[index]
        } else {
            null
        }
    }
}
