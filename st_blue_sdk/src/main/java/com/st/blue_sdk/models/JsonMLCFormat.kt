package com.st.blue_sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class JsonMLCFormat(
    @SerialName("json_format")
    val jsonFormat: JsonMLCFormatVersion? = null,
    @SerialName("application")
    val application: JsonMLCApplication? = null,
    @SerialName("date")
    val date: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("sensors")
    val sensors: ArrayList<JsonMLCSensors> = arrayListOf()
) {
    fun isForSensorName(sensorName: String): Boolean {
        return sensors.any { it.name.map{itt -> itt.uppercase()}.contains(sensorName.uppercase()) }
    }

    //For Flow Support
    fun toFlowSensorConfiguration(sensorName: String, isMLC: Boolean): MLCFlowSupport? {
        val currentSensor = sensors.find { it.name.map{itt -> itt.uppercase()}.contains(sensorName.uppercase()) }

        currentSensor?.let { sensor ->
            //Found one configuration for the current Sensor

            val regConfig: String
            var labels = ""
            var mlcEnabled = false
            var fsmEnabled = false

            val writeOperations = sensor.configuration.filter { it.type == "write" }

            //Full UCF Program
            regConfig =
                writeOperations.map { it.address!!.removePrefix("0x") + it.data!!.removePrefix("0x") }
                    .joinToString("")

            // Check if the MLC or the FSM are enabled
            var stmcPage = false
            for (singleOperation in writeOperations) {
                //Loop on all the write operations
                if ((singleOperation.address == "0x01") && (singleOperation.data == "0x80")) {
                    stmcPage = true
                } else if ((singleOperation.address == "0x01") && (singleOperation.data == "0x00")) {
                    stmcPage = false
                }

                if (stmcPage) {
                    if (singleOperation.address == "0x05") {
                        val reg = singleOperation.data!!.removePrefix("0x").toInt(16)
                        fsmEnabled = (reg and 0x01) != 0
                        mlcEnabled = (reg and 0x10) != 0
                    }
                }
            }

            //Search all the labels
            if (currentSensor.outputs.isNotEmpty()) {
                //<MLC0_SRC>DT1,0='angle_1',1='angle_2',2='angle_3',3='angle_4',4='angle_5',5='angle_6',6='angle_7',7='angle_8',8='angle_9',9='angle_10';
                //<MLC1_SRC>DT2,1='angle_2',2='angle_3',3='angle_4',4='angle_5',5='angle_6',6='angle_7',7='angle_8',8='angle_9',9='angle_10',10='angle_11';
                //<MLC2_SRC>DT3,2='angle_3',3='angle_4',4='angle_5',5='angle_6',6='angle_7',7='angle_8',8='angle_9',9='angle_10',10='angle_11',11='angle_12';

                if (isMLC) {
                    val mLCOutputs = currentSensor.outputs.filter { it.core == "MLC" }
                    mLCOutputs.forEach { output ->
                        labels += "<${output.regName}>${output.name},"
                        output.results.forEach { result ->
                            labels += result.code.removePrefix("0x").toInt(16).toString() + "='" + result.label + "',"
                        }
                        labels = labels.dropLast(1)
                        labels += ";"
                    }

                } else {
                    val fSMOutputs = currentSensor.outputs.filter { it.core == "FSM" }
                    if (fSMOutputs.isNotEmpty()) {
                        fSMOutputs.forEach { output ->
                            labels += "<${output.regName}>${output.name},"
                            output.results.forEach { result ->
                                labels += result.code.removePrefix("0x").toInt(16).toString() + "='" + result.label + "',"
                            }
                            labels = labels.dropLast(1)
                            labels += ";"
                        }
                    }
                }
            }

            return MLCFlowSupport(
                regConfig = regConfig,
                labels = labels,
                mlcEnabled = mlcEnabled,
                fsmEnabled = fsmEnabled
            )
        }
        return null
    }

    //For PnPL Support
    fun toPnPLOutputString(sensorName: String): String? {
        val currentSensor = sensors.find { it.name.map{itt -> itt.uppercase()}.contains(sensorName.uppercase()) }

        currentSensor?.let { sensor ->
            //Found one configuration for the current Sensor
            val writeDelayOperations = sensor.configuration.filter { it.type == "write" || it.type == "delay"}

            if(writeDelayOperations.isNotEmpty()) {

                return writeDelayOperations.map { operation ->
                    if(operation.type == "write") {
                        operation.address!!.removePrefix("0x") + operation.data!!.removePrefix("0x")
                    } else {
                        //The data for wait is not a hex value
                        String.format(locale = Locale.getDefault(), "W%03d", operation.data!!)
                    }
                }.joinToString("")
            }
        }
        return null
    }
}

//For Flow Support
data class MLCFlowSupport(
    val regConfig: String,
    val labels: String,
    val mlcEnabled: Boolean,
    val fsmEnabled: Boolean
)


@Serializable
data class JsonMLCFormatVersion(
    @SerialName("type")
    val type: String? = null,
    @SerialName("version")
    val version: String? = null
)

@Serializable
data class JsonMLCApplication(
    @SerialName("name")
    val name: String? = null,
    @SerialName("version")
    val version: String? = null
)

@Serializable
data class JsonMLCSensors(
    @SerialName("name")
    val name: List<String> = listOf(),
    @SerialName("configuration")
    val configuration: List<JsonMLCConfiguration> = listOf(),
    @SerialName("outputs")
    val outputs: List<JsonMLCOutputs> = listOf(),
    @SerialName("fifo_encodings")
    val fifoEncodings: List<JsonMLCFifoEncodings> = listOf()
)

@Serializable
data class JsonMLCConfiguration(
    @SerialName("type")
    val type: String? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("data")
    val data: String? = null
)

@Serializable
data class JsonMLCOutputs(
    @SerialName("name")
    val name: String? = null,
    @SerialName("core")
    val core: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("len")
    val len: String? = null,
    @SerialName("reg_addr")
    val regAddr: String? = null,
    @SerialName("reg_name")
    val regName: String? = null,
    @SerialName("results")
    val results: List<JsonMLCResult> = listOf()
)

@Serializable
data class JsonMLCResult(
    @SerialName("code")
    val code: String,
    @SerialName("label")
    val label: String
)


@Serializable
data class JsonMLCFifoEncodings(
    @SerialName("tag")
    val tag: String? = null,
    @SerialName("id")
    val id: String? = null,
    @SerialName("label")
    val label: String? = null
)

