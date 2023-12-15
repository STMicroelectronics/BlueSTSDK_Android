package com.st.blue_sdk.board_catalog.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonObject

sealed class DtmiContent(
    val type: DtmiType,
    open val id: String,
    open val name: String,
    open val displayName: Map<String, String>
) {
    data class DtmiInterfaceContent(
        override val id: String,
        override val name: String,
        override val displayName: Map<String, String>,

        val contents: List<DtmiContent>,
        val context: List<String>
    ) : DtmiContent(
        id = id,
        name = name,
        displayName = displayName,

        type = DtmiType.INTERFACE
    )

    data class DtmiComponentContent(
        override val id: String,
        override val name: String,
        override val displayName: Map<String, String> = emptyMap(),

        val schema: String
    ) : DtmiContent(
        id = id,
        name = name,
        displayName = displayName,

        type = DtmiType.COMPONENT
    ) {
        val sensorType: SensorType
            get() = SensorType.fromName(name)

        val contentType: ContentType
            get() {
                val schemaSplitParts = schema.split(":")
                return schemaSplitParts.take(schemaSplitParts.size - 1).last().let {
                    when (it) {
                        "sensors" -> ContentType.SENSOR
                        "algorithms" -> ContentType.ALGORITHM
                        "other" -> ContentType.OTHER
                        "actuators" -> ContentType.ACTUATORS
                        else -> ContentType.NONE
                    }
                }
            }

        enum class ContentType {
            SENSOR, ALGORITHM, OTHER, ACTUATORS, NONE
        }
    }

    sealed class DtmiPropertyContent(
        override val id: String,
        override val name: String,
        override val displayName: Map<String, String>,

        open val displayUnit: Map<String, String>,
        open val description: Map<String, String>,
        open val comment: String,
        open val unit: String,
        open val color: String,
        open val writable: Boolean,
        open val isCloud: Boolean,
        open val semanticType: String
    ) : DtmiContent(
        id = id,
        name = name,
        displayName = displayName,

        type = DtmiType.PROPERTY
    ) {
        data class DtmiBooleanPropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val trueName: Map<String, String>,
            val falseName: Map<String, String>,
            val initValue: Boolean
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )

        data class DtmiIntegerPropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val minValue: Int?,
            val maxValue: Int?,
            val initValue: Int
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )

        data class DtmiLongPropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val minValue: Long?,
            val maxValue: Long?,
            val initValue: Long
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )

        data class DtmiDoublePropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val decimalPlaces: Int?,
            val initValue: Double,
            val minValue: Double?,
            val maxValue: Double?
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )

        data class DtmiFloatPropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val decimalPlaces: Int?,
            val initValue: Float,
            val minValue: Float?,
            val maxValue: Float?
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )

        data class DtmiStringPropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val minLength: Int?,
            val maxLength: Int?,
            val initValue: String,
            val trimWhitespace: Boolean
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )

        data class DtmiDurationPropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val initValue: String
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )

        data class DtmiDateTimePropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val initValue: String,
            val hideTime: Boolean
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )

        data class DtmiDatePropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val initValue: String
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )

        data class DtmiTimePropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val initValue: String
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )

        data class DtmiVectorPropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val initValue: Vector
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        ) {
            @kotlinx.serialization.Serializable
            data class Vector(
                val x: Int,
                val y: Int,
                val z: Int
            ) {
                companion object {
                    val EMPTY = Vector(x = 0, y = 0, z = 0)
                }
            }
        }

        data class DtmiGeoPropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val initValue: Geo
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        ) {
            @kotlinx.serialization.Serializable
            data class Geo(
                val lat: Double,
                val lon: Double,
                val alt: Double
            ) {
                companion object {
                    val DEFAULT = Geo(
                        .0, .0, .0
                    )
                }
            }
        }

        data class DtmiComplexPropertyContent(
            override val id: String,
            override val name: String,
            override val displayName: Map<String, String>,

            override val displayUnit: Map<String, String>,
            override val description: Map<String, String>,
            override val comment: String,
            override val unit: String,
            override val color: String,
            override val semanticType: String,
            override val writable: Boolean,
            override val isCloud: Boolean,

            val schema: DtmiContent,
        ) : DtmiPropertyContent(
            id = id,
            name = name,
            displayName = displayName,

            displayUnit = displayUnit,
            description = description,
            comment = comment,
            unit = unit,
            color = color,
            semanticType = semanticType,
            writable = writable,
            isCloud = isCloud
        )
    }

    data class DtmiEnumContent<T : Any>(
        override val id: String,
        override val displayName: Map<String, String>,
        override val name: String,

        val enumValues: List<DtmiEnum<T>> = emptyList(),
        val enumColors: List<DtmiColorEnum<T>> = emptyList(),
        val enumType: EnumType,
        val initValue: T
    ) : DtmiContent(
        id = id,
        name = name,
        displayName = displayName,

        type = DtmiType.ENUM
    ) {
        enum class EnumType {
            INTEGER, STRING
        }

        @kotlinx.serialization.Serializable
        data class DtmiEnum<T>(
            @SerialName("@id")
            val id: String? = null,
            val displayName: Map<String, String>,
            val enumValue: T,
            val name: String
        )

        @kotlinx.serialization.Serializable
        data class DtmiColorEnum<T>(
            val color: String,
            val value: T
        )
    }

    data class DtmiObjectContent(
        override val id: String,
        override val name: String,
        override val displayName: Map<String, String>,

        val initValue: JsonObject?,
        val fields: List<DtmiContent> = emptyList()
    ) : DtmiContent(
        id = id,
        name = name,
        displayName = displayName,

        type = DtmiType.OBJECT
    )

    data class DtmiMapContent(
        override val id: String,
        override val name: String,
        override val displayName: Map<String, String>,

        val initValue: JsonObject?,
        val mapKey: DtmiPropertyContent.DtmiStringPropertyContent,
        val mapValue: DtmiPropertyContent
    ) : DtmiContent(
        id = id,
        name = name,
        displayName = displayName,

        type = DtmiType.MAP
    )

    data class DtmiCommandContent(
        override val id: String,
        override val name: String,
        override val displayName: Map<String, String>,

        val commandType: String,
        val isCommandDurable: Boolean,
        val request: DtmiContent?,
        val response: DtmiContent?
    ) : DtmiContent(
        id = id,
        name = name,
        displayName = displayName,

        type = DtmiType.COMMAND
    )
}