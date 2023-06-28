package com.st.blue_sdk.board_catalog.models

import kotlinx.serialization.json.*

data class DtmiModel(
    val bleFwId: String,
    val bleDevId: String,
    val contents: List<DtmiContent>,
    val customDTMI: Boolean = false
) {
    val createdAt = System.currentTimeMillis()

    companion object {
        private const val APP_ST_BLE_COMPONENT_NAME = "applications_stblesensor"
    }

    fun extractComponents(demoName: String? = null): List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>> {
        val interfaces =
            contents.filterIsInstance<DtmiContent.DtmiInterfaceContent>()

        val componentsWithInterface =
            interfaces.firstOrNull()?.contents?.filterIsInstance<DtmiContent.DtmiComponentContent>()
                ?.mapNotNull { component ->
                    interfaces.find { it.id == component.schema }?.let { interfaceContent ->
                        Pair(component, interfaceContent)
                    }
                } ?: emptyList()

        if (demoName.isNullOrEmpty()) {
            return componentsWithInterface
        } else {
            val appStBleSensor =
                componentsWithInterface.find { it.first.name == APP_ST_BLE_COMPONENT_NAME }

            if (appStBleSensor != null) {
                val find = appStBleSensor.second.contents.find { it.name == demoName }
                if (find is DtmiContent.DtmiPropertyContent.DtmiComplexPropertyContent) {
                    val schema = find.schema
                    if (schema is DtmiContent.DtmiObjectContent) {
                        return schema.fields.mapNotNull { content ->
                            componentsWithInterface.find { it.first.name == content.name }
                        }
                    }
                }
            }
        }

        return emptyList()
    }

    fun extractComponent(compName: String): List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>> {
        val interfaces =
            contents.filterIsInstance<DtmiContent.DtmiInterfaceContent>()

        val componentsWithInterface =
            interfaces.firstOrNull()?.contents?.filterIsInstance<DtmiContent.DtmiComponentContent>()
                ?.mapNotNull { component ->
                    interfaces.find { it.id == component.schema }?.let { interfaceContent ->
                        Pair(component, interfaceContent)
                    }
                } ?: emptyList()

        return componentsWithInterface.filter { it.first.name == compName }
    }
}

const val JSON_KEY_TYPE = "@type"
const val JSON_KEY_ID = "@id"
const val JSON_KEY_CONTEXT = "@context"
const val JSON_KEY_DISPLAY_NAME = "displayName"
const val JSON_KEY_CONTENTS = "contents"
const val JSON_KEY_SCHEMA = "schema"
const val JSON_KEY_INIT = "initialValue"
const val JSON_KEY_NAME = "name"
const val JSON_KEY_DESCRIPTION = "description"
const val JSON_KEY_HIDE_TIME = "hideTime"
const val JSON_KEY_FALSE_NAME = "falseName"
const val JSON_KEY_TRUE_NAME = "trueName"
const val JSON_KEY_MAX_L = "maxLength"
const val JSON_KEY_MIN_L = "minLength"
const val JSON_KEY_TRIM = "trimWhitespace"
const val JSON_KEY_MAP_KEY = "mapKey"
const val JSON_KEY_MAP_VALUE = "mapValue"
const val JSON_KEY_COLOR = "color"
const val JSON_KEY_COLORS = "colors"
const val JSON_KEY_UNIT = "unit"
const val JSON_KEY_MIN = "minValue"
const val JSON_KEY_DECIMAL = "decimalPlaces"
const val JSON_KEY_MAX = "maxValue"
const val JSON_KEY_COMMENT = "comment"
const val JSON_KEY_COMMAND_TYPE = "commandType"
const val JSON_KEY_COMMAND_DURABLE = "durable"
const val JSON_KEY_REQUEST = "request"
const val JSON_KEY_RESPONSE = "response"
const val JSON_KEY_WRITEABLE = "writable"
const val NAME_MIN = "min"
const val NAME_MAX = "max"
const val JSON_KEY_VALUE_SCHEMA = "valueSchema"
const val JSON_KEY_DISPLAY_UNIT = "displayUnit"
const val JSON_KEY_ENUM_VALUES = "enumValues"
const val JSON_KEY_FIELDS = "fields"
const val JSON_KEY_CLOUD = "Cloud"

const val JSON_KEY_NUMB = "NUMBERVALUE"
const val JSON_KEY_INITIALIZED = "INITIALIZED"
const val JSON_KEY_DATETIME = "DATETIMEVALUE"
const val JSON_KEY_VISUALIZED = "VISUALIZED"

val excludedStringForSemanticType = listOf(
    JSON_KEY_CLOUD,
    JSON_KEY_NUMB,
    JSON_KEY_INITIALIZED,
    JSON_KEY_DATETIME,
    JSON_KEY_VISUALIZED
)

fun JsonElement.toDtmiContent(
    enforcedType: DtmiType? = null,
    initialValue: JsonElement? = null,
    colors: JsonElement? = null
): DtmiContent? = if (this is JsonObject) {
    this.toDtmiContent(enforcedType = enforcedType, initialValue = initialValue, colors = colors)
} else {
    null
}

fun JsonObject.toDtmiContent(
    enforcedType: DtmiType? = null,
    initialValue: JsonElement? = null,
    colors: JsonElement? = null
): DtmiContent {
    val typeJson = get(JSON_KEY_TYPE)
    val id = (get(JSON_KEY_ID) as JsonPrimitive?)?.contentOrNull ?: ""

    val displayNameJson = get(JSON_KEY_DISPLAY_NAME)
    var displayName = emptyMap<String, String>()
    if (displayNameJson is JsonObject) {
        displayName = Json.decodeFromJsonElement(displayNameJson)
    }

//            "@type": [
//                "Property", // if a property or a Telemetry (Telemetry is always writable false)
//                "Cloud", // if is a property cloud (as Telemetry is always writable false and has not color)
//                "Acceleration", // semantic type
//                "NumberValue", // generic type
//                "Visualized" // if has a color
//            ],

    var isCloud = false
    var semanticType = ""

    val type = enforcedType?.name ?: when (typeJson) {
        null -> DtmiType.PROPERTY.name // Todo: check if i can really assume that!
        is JsonNull -> DtmiType.PROPERTY.name // Todo: check if i can really assume that!
        is JsonObject -> "" // Unknown
        is JsonPrimitive -> typeJson.content.uppercase()
        is JsonArray -> {
            val types = Json.decodeFromJsonElement<List<JsonElement>>(typeJson)

            val jsonPrimitives = types.filterIsInstance<JsonPrimitive>().mapNotNull {
                it.contentOrNull?.uppercase()
            }

            isCloud = jsonPrimitives.find { it == JSON_KEY_CLOUD } != null

            semanticType = jsonPrimitives.find {
                excludedStringForSemanticType.contains(it).not() &&
                        DtmiType.values().map { dtmiType -> dtmiType.name }.contains(it).not()
            } ?: ""

            jsonPrimitives.find {
                DtmiType.values().map { dtmiType -> dtmiType.name }.contains(it)
            } ?: "" // Unknown
        }
    }

    val schemaJson = get(JSON_KEY_SCHEMA)
    val name = (get(JSON_KEY_NAME) as JsonPrimitive?)?.contentOrNull ?: ""

    val contextJson = get(JSON_KEY_CONTEXT)
    var context = emptyList<String>()
    if (contextJson is JsonArray) {
        context = Json.decodeFromJsonElement(contextJson)
    }

    val contentsJson = get(JSON_KEY_CONTENTS)
    var contents = emptyList<JsonElement>()
    if (contentsJson is JsonArray) {
        contents = Json.decodeFromJsonElement(contentsJson)
    }

    val unit = (get(JSON_KEY_UNIT) as JsonPrimitive?)?.contentOrNull ?: ""
    val color = (get(JSON_KEY_COLOR) as JsonPrimitive?)?.contentOrNull ?: ""
    val comment = (get(JSON_KEY_COMMENT) as JsonPrimitive?)?.contentOrNull ?: ""
    val baseWritable = (get(JSON_KEY_WRITEABLE) as JsonPrimitive?)?.booleanOrNull
        ?: (name != NAME_MIN && name != NAME_MAX)

    val displayUnitJson = get(JSON_KEY_DISPLAY_UNIT)
    var displayUnit = emptyMap<String, String>()
    if (displayUnitJson is JsonObject) {
        displayUnit = Json.decodeFromJsonElement(displayUnitJson)
    }

    val descriptionJson = get(JSON_KEY_DESCRIPTION)
    var description = emptyMap<String, String>()
    if (descriptionJson is JsonObject) {
        description = Json.decodeFromJsonElement(descriptionJson)
    }

    val fieldsJson = get(JSON_KEY_FIELDS)
    var fields = emptyList<DtmiContent>()
    if (fieldsJson is JsonArray) {
        fields = fieldsJson.mapNotNull { it.toDtmiContent() }
    }

    val enumValuesJson = get(JSON_KEY_ENUM_VALUES)

    val valueSchema = (get(JSON_KEY_VALUE_SCHEMA) as JsonPrimitive?)?.contentOrNull

    val commandType = (get(JSON_KEY_COMMAND_TYPE) as JsonPrimitive?)?.contentOrNull ?: ""
    val isCommandDurable = (get(JSON_KEY_COMMAND_DURABLE) as JsonPrimitive?)?.booleanOrNull ?: false

    return when (val dtmiType = DtmiType.valueOf(type)) {
        DtmiType.COMPONENT -> DtmiContent.DtmiComponentContent(
            id = id,
            displayName = displayName,
            name = name,
            schema = (schemaJson as JsonPrimitive).content
        )

        DtmiType.INTERFACE -> DtmiContent.DtmiInterfaceContent(id = id,
            name = id,
            displayName = displayName,
            context = context,
            contents = contents.mapNotNull { it.toDtmiContent() })

        DtmiType.TELEMETRY, DtmiType.PROPERTY -> {
            val writable = if (dtmiType == DtmiType.TELEMETRY) false else baseWritable
            if (schemaJson is JsonPrimitive) {
                val schema = schemaJson.content.uppercase()
                when (DtmiPropertyType.valueOf(schema)) {
                    DtmiPropertyType.BOOLEAN -> {
                        val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.booleanOrNull ?: false
                        val trueNameJson = get(JSON_KEY_TRUE_NAME)
                        var trueName = emptyMap<String, String>()
                        if (trueNameJson is JsonObject) {
                            trueName = Json.decodeFromJsonElement(trueNameJson)
                        }
                        val falseNameJson = get(JSON_KEY_FALSE_NAME)
                        var falseName = emptyMap<String, String>()
                        if (falseNameJson is JsonObject) {
                            falseName = Json.decodeFromJsonElement(falseNameJson)
                        }
                        DtmiContent.DtmiPropertyContent.DtmiBooleanPropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            comment = comment,
                            description = description,
                            color = color,
                            displayUnit = displayUnit,
                            writable = if (dtmiType == DtmiType.TELEMETRY) false else writable,
                            isCloud = isCloud,
                            semanticType = semanticType,
                            trueName = trueName,
                            falseName = falseName,
                            initValue = init
                        )
                    }

                    DtmiPropertyType.LONG -> {
                        val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.longOrNull ?: 0L

                        val minValue = (get(JSON_KEY_MIN) as JsonPrimitive?)?.longOrNull
                        val maxValue = (get(JSON_KEY_MAX) as JsonPrimitive?)?.longOrNull

                        DtmiContent.DtmiPropertyContent.DtmiLongPropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            comment = comment,
                            description = description,
                            color = color,
                            displayUnit = displayUnit,
                            minValue = minValue,
                            maxValue = maxValue,
                            initValue = init,
                            writable = writable,
                            isCloud = isCloud,
                            semanticType = semanticType
                        )
                    }

                    DtmiPropertyType.INTEGER -> {
                        val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.intOrNull ?: 0

                        val minValue = (get(JSON_KEY_MIN) as JsonPrimitive?)?.intOrNull
                        val maxValue = (get(JSON_KEY_MAX) as JsonPrimitive?)?.intOrNull

                        DtmiContent.DtmiPropertyContent.DtmiIntegerPropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            comment = comment,
                            description = description,
                            color = color,
                            displayUnit = displayUnit,
                            minValue = minValue,
                            maxValue = maxValue,
                            initValue = init,
                            writable = writable,
                            semanticType = semanticType,
                            isCloud = isCloud
                        )
                    }

                    DtmiPropertyType.FLOAT -> {

                        val decimalPlaces = (get(JSON_KEY_DECIMAL) as JsonPrimitive?)?.intOrNull
                        val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.floatOrNull ?: 0f
                        val minValue = (get(JSON_KEY_MIN) as JsonPrimitive?)?.floatOrNull
                        val maxValue = (get(JSON_KEY_MAX) as JsonPrimitive?)?.floatOrNull

                        DtmiContent.DtmiPropertyContent.DtmiFloatPropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            comment = comment,
                            description = description,
                            color = color,
                            decimalPlaces = decimalPlaces,
                            displayUnit = displayUnit,
                            initValue = init,
                            minValue = minValue,
                            maxValue = maxValue,
                            writable = writable,
                            semanticType = semanticType,
                            isCloud = isCloud
                        )
                    }

                    DtmiPropertyType.DOUBLE -> {

                        val decimalPlaces = (get(JSON_KEY_DECIMAL) as JsonPrimitive?)?.intOrNull
                        val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.doubleOrNull ?: .0
                        val minValue = (get(JSON_KEY_MIN) as JsonPrimitive?)?.doubleOrNull
                        val maxValue = (get(JSON_KEY_MAX) as JsonPrimitive?)?.doubleOrNull

                        DtmiContent.DtmiPropertyContent.DtmiDoublePropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            comment = comment,
                            description = description,
                            color = color,
                            decimalPlaces = decimalPlaces,
                            displayUnit = displayUnit,
                            initValue = init,
                            minValue = minValue,
                            maxValue = maxValue,
                            writable = writable,
                            semanticType = semanticType,
                            isCloud = isCloud
                        )
                    }

                    DtmiPropertyType.DATE -> {
                        val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.contentOrNull ?: ""

                        DtmiContent.DtmiPropertyContent.DtmiDatePropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            comment = comment,
                            description = description,
                            color = color,
                            displayUnit = displayUnit,
                            writable = writable,
                            isCloud = isCloud,
                            semanticType = semanticType,
                            initValue = init
                        )
                    }

                    DtmiPropertyType.DATETIME -> {

                        val hideTime =
                            (get(JSON_KEY_HIDE_TIME) as JsonPrimitive?)?.booleanOrNull ?: false

                        val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.contentOrNull ?: ""

                        DtmiContent.DtmiPropertyContent.DtmiDateTimePropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            comment = comment,
                            description = description,
                            color = color,
                            displayUnit = displayUnit,
                            writable = writable,
                            isCloud = isCloud,
                            semanticType = semanticType,
                            initValue = init,
                            hideTime = hideTime
                        )
                    }

                    DtmiPropertyType.TIME -> {

                        val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.contentOrNull ?: ""

                        DtmiContent.DtmiPropertyContent.DtmiTimePropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            comment = comment,
                            description = description,
                            color = color,
                            displayUnit = displayUnit,
                            writable = writable,
                            isCloud = isCloud,
                            semanticType = semanticType,
                            initValue = init
                        )
                    }

                    DtmiPropertyType.DURATION -> {

                        val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.contentOrNull ?: ""

                        DtmiContent.DtmiPropertyContent.DtmiDurationPropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            comment = comment,
                            description = description,
                            color = color,
                            displayUnit = displayUnit,
                            initValue = init,
                            writable = writable,
                            semanticType = semanticType,
                            isCloud = isCloud
                        )
                    }

                    DtmiPropertyType.STRING -> {

                        val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.contentOrNull ?: ""
                        val trimWhitespace =
                            (get(JSON_KEY_TRIM) as JsonPrimitive?)?.booleanOrNull ?: false
                        val minLength = (get(JSON_KEY_MIN_L) as JsonPrimitive?)?.intOrNull
                        val maxLength = (get(JSON_KEY_MAX_L) as JsonPrimitive?)?.intOrNull

                        DtmiContent.DtmiPropertyContent.DtmiStringPropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            comment = comment,
                            description = description,
                            color = color,
                            displayUnit = displayUnit,
                            writable = writable,
                            isCloud = isCloud,
                            semanticType = semanticType,
                            minLength = minLength,
                            maxLength = maxLength,
                            initValue = init,
                            trimWhitespace = trimWhitespace
                        )
                    }

                    DtmiPropertyType.GEOPOINT -> {

                        var init: DtmiContent.DtmiPropertyContent.DtmiGeoPropertyContent.Geo =
                            DtmiContent.DtmiPropertyContent.DtmiGeoPropertyContent.Geo.DEFAULT
                        val initJson = get(JSON_KEY_INIT)
                        if (initJson is JsonObject) {
                            init = Json.decodeFromJsonElement(initJson)
                        }

                        DtmiContent.DtmiPropertyContent.DtmiGeoPropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            displayUnit = displayUnit,
                            comment = comment,
                            description = description,
                            color = color,
                            writable = writable,
                            isCloud = isCloud,
                            semanticType = semanticType,
                            initValue = init
                        )
                    }

                    DtmiPropertyType.VECTOR -> {

                        var init: DtmiContent.DtmiPropertyContent.DtmiVectorPropertyContent.Vector =
                            DtmiContent.DtmiPropertyContent.DtmiVectorPropertyContent.Vector.EMPTY
                        val initJson = get(JSON_KEY_INIT)
                        if (initJson is JsonObject) {
                            init = Json.decodeFromJsonElement(initJson)
                        }

                        DtmiContent.DtmiPropertyContent.DtmiVectorPropertyContent(
                            id = id,
                            displayName = displayName,
                            name = name,
                            unit = unit,
                            displayUnit = displayUnit,
                            comment = comment,
                            description = description,
                            color = color,
                            writable = writable,
                            isCloud = isCloud,
                            semanticType = semanticType,
                            initValue = init
                        )
                    }
                }
            } else {
                DtmiContent.DtmiPropertyContent.DtmiComplexPropertyContent(
                    id = id,
                    displayName = displayName,
                    schema = schemaJson?.toDtmiContent(
                        initialValue = get(JSON_KEY_INIT),
                        colors = get(JSON_KEY_COLORS)
                    )
                        ?: throw IllegalStateException("a complex property cannot be a null schema"),
                    displayUnit = displayUnit,
                    name = name,
                    unit = unit,
                    comment = comment,
                    description = description,
                    color = color,
                    writable = writable,
                    semanticType = semanticType,
                    isCloud = isCloud
                )
            }
        }

        DtmiType.OBJECT -> DtmiContent.DtmiObjectContent(
            id = id,
            name = id,
            displayName = displayName,
            fields = fields,
            initValue = initialValue as JsonObject?
        )

        DtmiType.ENUM -> when (val enumType =
            DtmiContent.DtmiEnumContent.EnumType.valueOf(valueSchema?.uppercase() ?: "")) {
            DtmiContent.DtmiEnumContent.EnumType.INTEGER -> {
                var enumValues = emptyList<DtmiContent.DtmiEnumContent.DtmiEnum<Int>>()
                if (enumValuesJson is JsonArray) {
                    enumValues = Json.decodeFromJsonElement(enumValuesJson)
                }
                val init = (initialValue as JsonPrimitive?)?.intOrNull
                    ?: enumValues.firstOrNull()?.enumValue ?: 0

                var enumColors: List<DtmiContent.DtmiEnumContent.DtmiColorEnum<Int>> = emptyList()
                if (colors is JsonArray) {
                    enumColors = Json.decodeFromJsonElement(colors)
                }

                DtmiContent.DtmiEnumContent(
                    id = id,
                    name = id,
                    displayName = displayName,
                    enumValues = enumValues,
                    enumColors = enumColors,
                    initValue = init,
                    enumType = enumType
                )
            }

            DtmiContent.DtmiEnumContent.EnumType.STRING -> {
                var enumValues = emptyList<DtmiContent.DtmiEnumContent.DtmiEnum<String>>()
                if (enumValuesJson is JsonArray) {
                    try {
                        enumValues = Json.decodeFromJsonElement(enumValuesJson)
                    } catch (ex: Exception) {
                        // Handle a error in json
                        // https://github.com/STMicroelectronics/appconfig/blob/8a00f5281b6c0e2fe682894da49800e6431af6b1/dtmi/appconfig/steval_stwinbx1/datalog2_fpSnsDatalog2-1.expanded.json#L3688
                        enumValues =
                            Json.decodeFromJsonElement<List<DtmiContent.DtmiEnumContent.DtmiEnum<Int>>>(
                                enumValuesJson
                            ).map {
                                DtmiContent.DtmiEnumContent.DtmiEnum(
                                    id = it.id,
                                    displayName = it.displayName,
                                    enumValue = "${it.enumValue}",
                                    name = it.name
                                )
                            }

                    }
                }
                val init = (get(JSON_KEY_INIT) as JsonPrimitive?)?.contentOrNull
                    ?: enumValues.firstOrNull()?.enumValue ?: ""

                var enumColors: List<DtmiContent.DtmiEnumContent.DtmiColorEnum<String>> =
                    emptyList()
                if (colors is JsonArray) {
                    enumColors = Json.decodeFromJsonElement(colors)
                }

                DtmiContent.DtmiEnumContent(
                    id = id,
                    name = id,
                    displayName = displayName,
                    enumValues = enumValues,
                    enumColors = enumColors,
                    initValue = init,
                    enumType = enumType
                )
            }
        }

        DtmiType.COMMAND -> DtmiContent.DtmiCommandContent(
            id = id,
            displayName = displayName,
            name = name,
            commandType = commandType,
            isCommandDurable = isCommandDurable,
            request = get(JSON_KEY_REQUEST)?.toDtmiContent(),
            response = get(JSON_KEY_RESPONSE)?.toDtmiContent()
        )

        DtmiType.MAP -> {
            DtmiContent.DtmiMapContent(
                id = id,
                displayName = displayName,
                name = name,

                initValue = initialValue as JsonObject?,
                mapKey = get(JSON_KEY_MAP_KEY)?.toDtmiContent() as DtmiContent.DtmiPropertyContent.DtmiStringPropertyContent,
                mapValue = get(JSON_KEY_MAP_VALUE)?.toDtmiContent() as DtmiContent.DtmiPropertyContent,
            )
        }

        DtmiType.COMMANDPAYLOAD -> toDtmiContent(enforcedType = DtmiType.PROPERTY)
    }
}

enum class DtmiPropertyType {
    LONG, INTEGER, DOUBLE, FLOAT, BOOLEAN, STRING, DURATION,

    DATETIME, TIME, DATE,

    VECTOR,

    GEOPOINT
}

enum class DtmiType {
    COMPONENT, INTERFACE,

    PROPERTY, TELEMETRY, COMMANDPAYLOAD,

    OBJECT, ENUM, MAP,

    COMMAND
}