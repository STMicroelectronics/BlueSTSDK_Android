package com.st.blue_sdk.features.extended.neai_extrapolation

import com.st.blue_sdk.features.extended.neai_extrapolation.model.NeaiExtrapolationData
import com.st.blue_sdk.features.extended.neai_extrapolation.model.getPhaseCode
import com.st.blue_sdk.features.extended.neai_extrapolation.model.getStateCode
import com.st.blue_sdk.logger.Loggable

data class NeaiExtrapolationInfo(val extrapolation: NeaiExtrapolationData?) : Loggable {
    override val logHeader: String =
        "phase, state, target, unit, stub"

    override val logValue: String =
        extrapolation?.let { "${extrapolation.phase}, ${extrapolation.state}, ${extrapolation.target}, ${extrapolation.unit}, ${extrapolation.stub}" }
            ?: "null,null,null,null,null"

    override val logDoubleValues: List<Double> = extrapolation?.let {
        listOf(getPhaseCode(extrapolation.phase).toDouble(),
            extrapolation.state?.let { getStateCode(extrapolation.state).toDouble() } ?: 0.0,
            extrapolation.target?.let {extrapolation.target.toDouble()} ?: 0.0)
    } ?: emptyList()


    override fun toString(): String {
       return extrapolation?.let {
           val sampleValue = StringBuilder()
           sampleValue.append("\tphase = ${extrapolation.phase}\n")

           extrapolation.state?.let {
               sampleValue.append("\tstate = ${extrapolation.state}\n")
           }

           extrapolation.target?.let {
               sampleValue.append("\ttarget = ${extrapolation.target}\n")
           }

           extrapolation.unit?.let {
               sampleValue.append("\tunit = ${extrapolation.unit}\n")
           }

           sampleValue.append("\tstub = ${extrapolation.stub}\n")

           sampleValue.toString()
       } ?: "empty\n"
    }
}
