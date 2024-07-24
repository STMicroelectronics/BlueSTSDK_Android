package com.st.blue_sdk.features.extended.neai_anomaly_detection

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.neai_anomaly_detection.request.WriteDetectionCommand
import com.st.blue_sdk.features.extended.neai_anomaly_detection.request.WriteLearningCommand
import com.st.blue_sdk.features.extended.neai_anomaly_detection.request.WriteResetKnowledgeCommand
import com.st.blue_sdk.features.extended.neai_anomaly_detection.request.WriteStopCommand
import com.st.blue_sdk.utils.NumberConversion
import kotlin.experimental.and

class NeaiAnomalyDetection(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<NeaiAnomalyDetectionInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "NEAI AD"
        const val NUMBER_BYTES = 7

        fun getPhaseValue(phase: Short) = when ((phase and 0xFF).toInt()) {
            0x00 -> PhaseType.Idle
            0x01 -> PhaseType.Learning
            0x02 -> PhaseType.Detection
            0x03 -> PhaseType.Idle_Trained
            0x04 -> PhaseType.Busy
            else -> PhaseType.Null
        }

        fun getPhaseCode(phase: PhaseType) = when (phase) {
            PhaseType.Idle -> 0x00
            PhaseType.Learning -> 0x01
            PhaseType.Detection -> 0x02
            PhaseType.Idle_Trained -> 0x03
            PhaseType.Busy -> 0x04
            PhaseType.Null -> 0xFF
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

        fun getStateValueCode(state: StateType) = when (state) {
            StateType.Ok -> 0x00
            StateType.Init_Not_Called -> 0x7B
            StateType.Board_Error -> 0x7C
            StateType.Knowledge_Error -> 0x7D
            StateType.Not_Enough_Learning -> 0x7E
            StateType.Minimal_Learning_done -> 0x7F
            StateType.Unknown_Error -> 0x80
            StateType.Null -> 0xFF
        }

        fun getStatusValue(status: Short) = when ((status and 0xFF).toInt()) {
            0x00 -> StatusType.Normal
            0x01 -> StatusType.Anomaly
            else -> StatusType.Null
        }

        fun getStatusValueCode(status: StatusType) = when (status) {
            StatusType.Normal -> 0x00
            StatusType.Anomaly -> 0x01
            StatusType.Null -> 0xFF
        }
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<NeaiAnomalyDetectionInfo> {
        require(data.size - dataOffset == NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }
        val phase = NumberConversion.byteToUInt8(data, dataOffset + 2)
        val state = NumberConversion.byteToUInt8(data, dataOffset + 3)
        val phaseProgress = NumberConversion.byteToUInt8(data, dataOffset + 4)
        val status = NumberConversion.byteToUInt8(data, dataOffset + 5)
        val similarity = NumberConversion.byteToUInt8(data, dataOffset + 6)

        val anomalyDetectionInfo = NeaiAnomalyDetectionInfo(
            phase = FeatureField(
                value = getPhaseValue(phase),
                name = "Phase",
                min = PhaseType.Idle,
                max = PhaseType.Idle_Trained
            ),
            state = FeatureField(
                value = getStateValue(state),
                name = "State",
                min = StateType.Ok,
                max = StateType.Unknown_Error
            ),
            phaseProgress = FeatureField(
                value = phaseProgress,
                name = "Phase Progress",
                min = 0,
                max = 100,
                unit = "%"
            ),
            status = FeatureField(
                value = getStatusValue(status),
                name = "Status",
                min = StatusType.Normal,
                max = StatusType.Anomaly
            ),
            similarity = FeatureField(
                value = similarity,
                name = "Similarity"
            )
        )

        return FeatureUpdate(
            featureName = name,
            rawData = data,
            readByte = NUMBER_BYTES,
            timeStamp = timeStamp,
            data = anomalyDetectionInfo
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is WriteStopCommand -> packCommandRequest(
                featureBit,
                WriteStopCommand.STOP_COMMAND,
                byteArrayOf()
            )

            is WriteResetKnowledgeCommand -> packCommandRequest(
                featureBit,
                WriteResetKnowledgeCommand.RESET_KNOWLEDGE_COMMAND,
                byteArrayOf()
            )

            is WriteLearningCommand -> packCommandRequest(
                featureBit,
                WriteLearningCommand.LEARNING_COMMAND,
                byteArrayOf()
            )

            is WriteDetectionCommand -> packCommandRequest(
                featureBit,
                WriteDetectionCommand.DETECTION_COMMAND,
                byteArrayOf()
            )

            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}