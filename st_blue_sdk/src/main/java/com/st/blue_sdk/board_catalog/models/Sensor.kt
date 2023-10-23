package com.st.blue_sdk.board_catalog.models
import androidx.room.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(
    primaryKeys = ["unique_id"],
    tableName = "sensor_adapters"
)

@Serializable
data class Sensor(
    @SerialName(value = "unique_id")
    var unique_id: Int?=null,
    @SerialName(value = "id")
    var id: String = "",
    @SerialName(value = "description")
    var description: String = "",
    @SerialName(value = "icon")
    var icon: String = "",
    @SerialName(value = "output")
    var output: String = "",
    @SerialName(value = "outputs")
    var outputs: List<String> = listOf(),
    @SerialName(value = "model")
    var model: String = "",
    @SerialName(value = "notes")
    var notes: String = "",
    @SerialName(value = "dataType")
    var dataType: String = "",
    @SerialName(value = "um")
    var um: String = "",
    @SerialName(value = "fullScaleUm")
    var fullScaleUm: String = "",
    @SerialName(value = "datasheetLink")
    var datasheetLink: String = "",
    @SerialName(value = "fullScales")
    var fullScales: List<Int>? = null,
    @SerialName(value = "powerModes")
    var powerModes: List<PowerMode>? = null,
    @SerialName(value = "acquisitionTime")
    var acquisitionTime: Double? = null,
    @SerialName(value = "samplingFrequencies")
    var samplingFrequencies: List<Int>? = null,
    @SerialName(value = "bleMaxOdr")
    var bleMaxOdr: Double? = null,
    @SerialName(value = "board_compatibility")
    var board_compatibility: ArrayList<String> = ArrayList(),
    @SerialName(value = "configuration")
    var configuration: SensorConfiguration?=null
) {

    override fun equals(other: Any?): Boolean {
        return other is Sensor && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun hasSettings(): Boolean {

        if(configuration==null) {
            return false
        }

        if (configuration!!.regConfig != null) {
            return true
        }

        if (fullScales != null && !fullScales!!.isEmpty()) {
            return true
        }

        return powerModes != null && !powerModes!!.isEmpty()
    }

    fun hasSameConfiguration(otherConfiguration: SensorConfiguration): Boolean {
        return configuration == otherConfiguration
    }
}
