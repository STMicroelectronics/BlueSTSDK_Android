package com.st.blue_sdk.features.extended.neai_anomaly_detection

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable

data class NeaiAnomalyDetectionInfo(
    val phase: FeatureField<PhaseType>,
    val state: FeatureField<StateType>,
    val phaseProgress: FeatureField<Short>,
    val status: FeatureField<StatusType>,
    val similarity: FeatureField<Short>
) : Loggable {
    override val logHeader: String =
        "${phase.logHeader}, ${state.logHeader}, ${phaseProgress.logHeader}," +
                "${status.logHeader}, ${similarity.logHeader}"

    override val logValue: String =
        "${phase.value}, ${state.value}, ${phaseProgress.value}," +
                "${status.value}, ${similarity.value}"

    override val logDoubleValues: List<Double> = listOf(
        NeaiAnomalyDetection.getPhaseCode(phase.value).toDouble(),
        NeaiAnomalyDetection.getStateValueCode(state.value).toDouble(),
        phaseProgress.value.toDouble(),
        NeaiAnomalyDetection.getStatusValueCode(status.value).toDouble(),
        similarity.value.toDouble()
    )

    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${phase.name} = ${phase.value}\n")
        sampleValue.append("\t${state.name} = ${state.value}\n")
        sampleValue.append("\t${phaseProgress.name} = ${phaseProgress.value} %\n")
        sampleValue.append("\t${status.name} = ${status.value} %\n")
        sampleValue.append("\t${similarity.name} = ${similarity.value} %\n")
        return sampleValue.toString()
    }
}

enum class PhaseType {
    Idle,
    Learning,
    Detection,
    Idle_Trained,
    Busy,
    Null
}

enum class StateType {
    Ok,
    Init_Not_Called,
    Board_Error,
    Knowledge_Error,
    Not_Enough_Learning,
    Minimal_Learning_done,
    Unknown_Error,
    Null
}

enum class StatusType {
    Normal,
    Anomaly,
    Null
}
