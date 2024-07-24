package com.st.blue_sdk.features.extended.neai_class_classification

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.neai_class_classification.request.WriteStartClassificationCommand
import com.st.blue_sdk.features.extended.neai_class_classification.request.WriteStopClassificationCommand
import com.st.blue_sdk.utils.NumberConversion
import kotlin.experimental.and

class NeaiClassClassification(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<NeaiClassClassificationInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "NEAI Classification"
        const val MIN_NUMBER_BYTES = 4
        const val ONE_CLASS_NUMBER_BYTES = 6
        const val N_CLASS_UNKNOWN_CLASS_NUMBER_BYTES = 6
        const val N_CLASS_COMMON_NUMBER_BYTES = 6
        const val N_MAX_CLASS_NUMBER = 8
        const val CLASS_PROB_ESCAPE_CODE: Int = 0xFF

        fun getPhaseValue(phase: Short) = when ((phase and 0x0F).toInt()) {
            0x00 -> PhaseType.Idle
            0x01 -> PhaseType.Classification
            0x02 -> PhaseType.Busy
            else -> PhaseType.Null
        }

        fun getPhaseCode(phaseType: PhaseType) = when (phaseType) {
            PhaseType.Idle -> 0x00
            PhaseType.Classification -> 0x01
            PhaseType.Busy -> 0x02
            PhaseType.Null -> 0x0F
        }

        fun getStateValue(state: Short) = when ((state and 0xFF).toInt()) {
            0x00 -> StateType.Ok
            0x7B -> StateType.Init_Not_Called
            0x7C -> StateType.Board_Error
            0x7D -> StateType.Knowledge_Error
            0x7E -> StateType.Not_Enough_Learning
            0x7F -> StateType.Minimal_Learning_done
            0x80 -> StateType.Unknown_Error
            else -> StateType.Null
        }

        fun getStateCode(stateType: StateType) = when (stateType) {
            StateType.Ok -> 0x00
            StateType.Init_Not_Called -> 0x7B
            StateType.Board_Error -> 0x7C
            StateType.Knowledge_Error -> 0x7D
            StateType.Not_Enough_Learning -> 0x7E
            StateType.Minimal_Learning_done -> 0x7F
            StateType.Unknown_Error -> 0x80
            StateType.Null -> 0xFF
        }

        fun getModeValue(status: Short) = when ((status and 0x0F).toInt()) {
            0x01 -> ModeType.One_Class
            0x02 -> ModeType.N_Class
            else -> ModeType.Null
        }

        fun getModeCode(mode: ModeType) = when (mode) {
            ModeType.One_Class -> 0x01
            ModeType.N_Class -> 0x02
            ModeType.Null -> 0xFF
        }
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<NeaiClassClassificationInfo> {
        require(data.size - dataOffset >= MIN_NUMBER_BYTES) { "There are not at least $MIN_NUMBER_BYTES bytes available to read for $name feature" }

        val mode = NumberConversion.byteToUInt8(data, dataOffset + 2)
        val phase = NumberConversion.byteToUInt8(data, dataOffset + 2 + 1)
        val bytesUsed: Int
        val classificationInfo: NeaiClassClassificationInfo

        if ((data.size - dataOffset) == MIN_NUMBER_BYTES) {
            bytesUsed = MIN_NUMBER_BYTES
            //Idle phase
            classificationInfo = NeaiClassClassificationInfo(
                mode = FeatureField(
                    value = getModeValue(mode),
                    name = "Mode"
                ),
                phase = FeatureField(
                    value = getPhaseValue(phase),
                    name = "Phase"
                )
            )
        } else {
            //Classification phase
            when (getModeValue(mode)) {
                ModeType.One_Class -> {
                    //1 Class
                    require(data.size - dataOffset != ONE_CLASS_NUMBER_BYTES) { "Wrong number of bytes (${data.size - dataOffset}) for 1-Class" }
                    classificationInfo = NeaiClassClassificationInfo(
                        mode = FeatureField(
                            value = getModeValue(mode),
                            name = "Mode"
                        ),
                        phase = FeatureField(
                            value = getPhaseValue(phase),
                            name = "Phase"
                        ),
                        state = FeatureField(
                            value = getStateValue(
                                NumberConversion.byteToUInt8(
                                    data,
                                    dataOffset + 2 + 2
                                )
                            ),
                            name = "State"
                        ),
                        classNum = FeatureField(
                            value = 1,
                            name = "ClassesNumber"
                        ),
                        classMajorProb = FeatureField(
                            value = 1,
                            name = "Class Major Prob"
                        ),
                        classProb = listOf(
                            FeatureField(
                                value = NumberConversion.byteToUInt8(data, dataOffset + 2 + 3),
                                name = "Class 1 Probability",
                                unit = "%",
                                min = 0,
                                max = 100
                            )
                        )
                    )
                    bytesUsed = ONE_CLASS_NUMBER_BYTES
                }

                ModeType.N_Class -> {
                    //N Class
                    if (data.size - dataOffset == N_CLASS_UNKNOWN_CLASS_NUMBER_BYTES) {
                        //Unknown Class
                        classificationInfo = NeaiClassClassificationInfo(
                            mode = FeatureField(
                                value = getModeValue(mode),
                                name = "Mode"
                            ),
                            phase = FeatureField(
                                value = getPhaseValue(phase),
                                name = "Phase"
                            ),
                            state = FeatureField(
                                value = getStateValue(
                                    NumberConversion.byteToUInt8(
                                        data,
                                        dataOffset + 2 + 2
                                    )
                                ),
                                name = "State"
                            ),
                            classNum = FeatureField(
                                value = 1,
                                name = "ClassesNumber"
                            ),
                            classMajorProb = FeatureField(
                                value = NumberConversion.byteToUInt8(data, dataOffset + 2 + 3),
                                name = "Class Major Prob"
                            ),
                        )
                        bytesUsed = N_CLASS_UNKNOWN_CLASS_NUMBER_BYTES
                    } else {
                        //Inference

                        //Find number of Classes
                        val numClasses: Int = data.size - dataOffset - N_CLASS_COMMON_NUMBER_BYTES
                        require(numClasses < N_MAX_CLASS_NUMBER) { "Too many Classes $numClasses" }

                        //Read all the Class probabilities
                        val classes = mutableListOf<FeatureField<Short>>()
                        for (classNum in 0 until numClasses) {
                            classes.add(
                                FeatureField(
                                    max = 100,
                                    min = 0,
                                    name = "Class $classNum Probability",
                                    value = NumberConversion.byteToUInt8(
                                        data,
                                        dataOffset + N_CLASS_COMMON_NUMBER_BYTES + classNum
                                    )
                                )
                            )
                        }

                        //Prepare the Output
                        classificationInfo = NeaiClassClassificationInfo(
                            mode = FeatureField(
                                value = getModeValue(mode),
                                name = "Mode"
                            ),
                            phase = FeatureField(
                                value = getPhaseValue(phase),
                                name = "Phase"
                            ),
                            state = FeatureField(
                                value = getStateValue(
                                    NumberConversion.byteToUInt8(
                                        data,
                                        dataOffset + 2 + 2
                                    )
                                ),
                                name = "State"
                            ),
                            classNum = FeatureField(
                                value = numClasses.toShort(),
                                name = "ClassesNumber"
                            ),
                            classMajorProb = FeatureField(
                                value = NumberConversion.byteToUInt8(data, dataOffset + 2 + 3),
                                name = "Class Major Prob"
                            ),
                            classProb = classes
                        )
                        bytesUsed = N_CLASS_COMMON_NUMBER_BYTES + numClasses
                    }
                }

                else -> {
                    throw IllegalArgumentException("NEAI Classification mode type not recognized")
                }
            }
        }

        return FeatureUpdate(
            featureName = name,
            rawData = data,
            readByte = bytesUsed,
            timeStamp = timeStamp,
            data = classificationInfo
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is WriteStopClassificationCommand -> packCommandRequest(
                featureBit,
                WriteStopClassificationCommand.STOP_CLASSIFICATION_COMMAND,
                byteArrayOf()
            )

            is WriteStartClassificationCommand -> packCommandRequest(
                featureBit,
                WriteStartClassificationCommand.START_CLASSIFICATION_COMMAND,
                byteArrayOf()
            )

            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null

}