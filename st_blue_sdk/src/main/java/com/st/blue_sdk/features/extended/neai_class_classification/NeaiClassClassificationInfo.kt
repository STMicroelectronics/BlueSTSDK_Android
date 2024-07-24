package com.st.blue_sdk.features.extended.neai_class_classification

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.logger.Loggable

data class NeaiClassClassificationInfo(
    val phase: FeatureField<PhaseType>,
    val state: FeatureField<StateType>? = null,
    val mode: FeatureField<ModeType>,
    val classNum: FeatureField<Short>? = null,
    val classMajorProb: FeatureField<Short>? = null,
    val classProb: List<FeatureField<Short>>? = null
) : Loggable {

    override val logHeader: String =
        "${phase.logHeader}, ${mode.logHeader}" +
                state?.let { ", ${state.logHeader}" } +
                classNum?.let { ", ${classNum.logHeader}" } +
                classMajorProb?.let { ",  ${classMajorProb.logHeader}" } +
                classProb?.let {
                    ", " +
                            classProb.joinToString(separator = ", ") { it.logHeader }
                }

    override val logValue: String =
        "${phase.value}, ${mode.value}" +
                state?.let { ", ${state.value}" } +
                classNum?.let { ", ${classNum.value}" } +
                classMajorProb?.let { ", ${classMajorProb.value}" } +
                classProb?.let {
                    ", " +
                            classProb.joinToString(separator = ", ") { it.value.toString() }
                }

    override val logDoubleValues: List<Double> = listOf(
        NeaiClassClassification.getModeCode(mode.value).toDouble(),
        NeaiClassClassification.getPhaseCode(phase.value).toDouble(),
        NeaiClassClassification.getStateCode(state?.let { state.value } ?: StateType.Null)
            .toDouble(),
        classMajorProb?.let { classMajorProb.value.toDouble() } ?: -1.0,
        classNum?.let { classNum.value.toDouble() } ?: -1.0
    )


    override fun toString(): String {
        val sampleValue = StringBuilder()
        sampleValue.append("\t${phase.name} = ${phase.value}\n")
        sampleValue.append("\t${mode.name} = ${mode.value}\n")
        state?.let { sampleValue.append("\t${state.name} = ${state.value}\n") }
        classNum?.let { sampleValue.append("\t${classNum.name} = ${classNum.value}\n") }
        classMajorProb?.let { sampleValue.append("\t${classMajorProb.name} = ${classMajorProb.value}\n") }
        classProb?.let {
            sampleValue.append("\tClassProb:\n")
            classProb.forEach { sampleValue.append("\t\t${it.name} = ${it.value}%\n") }
        }
        return sampleValue.toString()
    }
}


enum class PhaseType {
    Idle,
    Classification,
    Busy,
    Null
}

enum class ModeType {
    One_Class,
    N_Class,
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