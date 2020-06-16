package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import com.google.gson.annotations.SerializedName

data class SensorStatus (
    //NOTE here there may be parameters in the future
    @SerializedName("subSensorStatus") val subSensorStatusList : List<SubSensorStatus>
)
{
    fun getSubSensorStatus(subSensorId: Int): SubSensorStatus? {
        return subSensorStatusList.getOrNull(subSensorId)
    }
}