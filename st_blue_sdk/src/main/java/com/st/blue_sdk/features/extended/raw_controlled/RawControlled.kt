/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.raw_controlled

import android.util.Log
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.raw_controlled.model.RawCustom
import com.st.blue_sdk.features.extended.raw_controlled.model.RawCustomEntry
import com.st.blue_sdk.features.extended.raw_controlled.model.RawPnPLEntry
import com.st.blue_sdk.features.extended.raw_controlled.model.RawPnPLEntryEnumLabel
import com.st.blue_sdk.features.extended.raw_controlled.model.RawPnPLEntryFormat
import com.st.blue_sdk.features.extended.raw_controlled.model.RawStreamIdEntry
import com.st.blue_sdk.utils.NumberConversion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

class RawControlled(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<RawControlledInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = false
) {

    companion object {
        const val NAME = "Raw Controlled"
        const val PROPERTY_NAME_ST_BLE_STREAM = "st_ble_stream"
        val HIDE_PROPERTIES_NAME = arrayOf("min", "max", "unit", "format", "elements", "output")
        const val STREAM_ID_NOT_FOUND: Int = 0xFF
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<RawControlledInfo> {

        val rawValues = mutableListOf<FeatureField<Byte>>()

        for (dataNum in 0 until (data.size - dataOffset)) {
            rawValues.add(
                FeatureField(
                    name = "B$dataNum",
                    value = data[dataOffset + dataNum]
                )
            )
        }
        val rawPnPLControlled = RawControlledInfo(
            data = rawValues
        )
        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = data.size - dataOffset,
            data = rawPnPLControlled
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}

fun searchPropertyDisplayName(
    modelUpdates: List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>,
    compName: String,
    propertyName: String,
    fieldName: String
): String? {
    var displayName: String? = null

    //from component model search the compId
    val schemaId = modelUpdates.firstOrNull { it.first.name == compName }?.first?.schema
    schemaId?.let {
        //from schema model  search the comp
        val comp = modelUpdates.firstOrNull { it.second.id == schemaId }
        comp?.let {
            //Search property
            val property =
                comp.second.contents.firstOrNull { content -> content.name == propertyName }

            property?.let {
                //Search Display Name
                if (property is DtmiContent.DtmiPropertyContent.DtmiComplexPropertyContent) {
                    if (property.schema is DtmiContent.DtmiObjectContent) {
                        displayName =
                            (property.schema).fields.firstOrNull { field -> field.name == fieldName }?.displayName?.get(
                                "en"
                            )
                    }
                }
            }
        }
    }

    return displayName
}

fun readRawPnPLFormat(
    rawPnPLFormat: MutableList<RawStreamIdEntry>,
    json: List<JsonObject>,
    modelUpdates: List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>,
) {
    rawPnPLFormat.clear()
    try {
        json.forEach { componentStatus ->
            componentStatus.entries.forEach { entry ->
                if (entry.value is JsonObject) {
                    val entryKey = entry.key
                    val jsonEntry = entry.value as JsonObject
                    if (jsonEntry.containsKey(RawControlled.PROPERTY_NAME_ST_BLE_STREAM)) {
                        val configuration =
                            jsonEntry[RawControlled.PROPERTY_NAME_ST_BLE_STREAM]
                        if (configuration is JsonObject) {
                            val componentName = entry.key
                            var streamId = 0
                            val formats = mutableListOf<RawPnPLEntry>()
                            var customFormat: RawCustom? = null

                            configuration.entries.forEach { singleEntry ->
                                if (singleEntry.value is JsonPrimitive) {
                                    if (singleEntry.key == "id") {
                                        streamId =
                                            (singleEntry.value as JsonPrimitive).content.toInt()
                                    } else {
                                        val customString =
                                            (singleEntry.value as JsonPrimitive).content
                                        val jsonDec = Json { encodeDefaults = true }
                                        customFormat =
                                            try {
                                                jsonDec.decodeFromString<RawCustom>(customString)
                                            } catch (e: Exception) {
                                                Log.d("readRawPnPLFormat", e.stackTraceToString())
                                                null
                                            }
                                    }
                                } else if (singleEntry.value is JsonObject) {
                                    val name = singleEntry.key

                                    val displayName = searchPropertyDisplayName(
                                        modelUpdates = modelUpdates,
                                        compName = entryKey,
                                        propertyName = RawControlled.PROPERTY_NAME_ST_BLE_STREAM,
                                        fieldName = name
                                    )
                                    val format =
                                        Json.decodeFromJsonElement<RawPnPLEntryFormat>(
                                            singleEntry.value
                                        )

                                    //Try to decode the Enum Format
                                    if (format.format == RawPnPLEntryFormat.RawPnPLEntryFormat.enum) {
                                        try {
                                            val customString =
                                                (singleEntry.value as JsonObject)["labels"]?.jsonPrimitive?.content
                                            customString?.let {
                                                val jsonDec = Json { encodeDefaults = true }
                                                val labelsParsed =
                                                    //try {
                                                    jsonDec.decodeFromString<List<RawPnPLEntryEnumLabel>>(
                                                        customString
                                                    )
                                                format.labelsParsed = labelsParsed
                                                formats.add(
                                                    RawPnPLEntry(
                                                        displayName = displayName,
                                                        name = name,
                                                        format = format
                                                    )
                                                )
                                            }
                                        } catch (e: Exception) {
                                            Log.d(
                                                "RawPnPLEntryFormat.enum",
                                                e.stackTraceToString()
                                            )
                                        }
                                    } else {
                                        formats.add(
                                            RawPnPLEntry(
                                                displayName = displayName,
                                                name = name,
                                                format = format
                                            )
                                        )
                                    }
                                }

                            }

                            rawPnPLFormat.add(
                                RawStreamIdEntry(
                                    componentName = componentName,
                                    streamId = streamId,
                                    formats = formats,
                                    customFormat = customFormat
                                )
                            )
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("readRawPnPLFormat", e.toString())
    }
}

fun decodeRawData(
    data: List<FeatureField<Byte>>,
    rawFormat: MutableList<RawStreamIdEntry>
): Int {
    var streamId = RawControlled.STREAM_ID_NOT_FOUND
    if (data.isNotEmpty()) {
        var counter = 0;
        val rawData = data.map { value -> value.value }.toByteArray()
        streamId = rawData[counter].toInt()
        counter++

        //Search the right component
        val foundStream = rawFormat.firstOrNull { it.streamId == streamId }
        foundStream?.let {
            foundStream.formats.forEach { formatRawPnpLEntry ->

                if (formatRawPnpLEntry.format.enable) {
                    //Reset the list of values
                    formatRawPnpLEntry.format.values.clear()
                    formatRawPnpLEntry.format.valuesFloat.clear()

                    when (formatRawPnpLEntry.format.format) {
                        RawPnPLEntryFormat.RawPnPLEntryFormat.int8_t -> {
                            for (index in 0 until (formatRawPnpLEntry.format.elements*formatRawPnpLEntry.format.channels)) {
                                if (rawData.size > counter) {

                                    val value = rawData[counter]
                                    formatRawPnpLEntry.format.values.add(value)

                                    var floatValue = value.toFloat()
                                    formatRawPnpLEntry.format.multiplyFactor?.let {
                                        floatValue *= it
                                    }
                                    formatRawPnpLEntry.format.valuesFloat.add(floatValue)

                                    counter++
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.uint8_t -> {
                            for (index in 0 until (formatRawPnpLEntry.format.elements*formatRawPnpLEntry.format.channels)) {
                                if (rawData.size >= counter) {

                                    val value = NumberConversion.byteToUInt8(
                                        rawData,
                                        counter
                                    )
                                    formatRawPnpLEntry.format.values.add(value)

                                    var floatValue = value.toFloat()
                                    formatRawPnpLEntry.format.multiplyFactor?.let {
                                        floatValue *= it
                                    }
                                    formatRawPnpLEntry.format.valuesFloat.add(floatValue)

                                    counter++
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.uint16_t -> {
                            for (index in 0 until (formatRawPnpLEntry.format.elements*formatRawPnpLEntry.format.channels)) {
                                if (rawData.size >= (counter + 2)) {

                                    val value = NumberConversion.LittleEndian.bytesToUInt16(
                                        rawData,
                                        counter
                                    )
                                    formatRawPnpLEntry.format.values.add(value)

                                    var floatValue = value.toFloat()
                                    formatRawPnpLEntry.format.multiplyFactor?.let {
                                        floatValue *= it
                                    }
                                    formatRawPnpLEntry.format.valuesFloat.add(floatValue)

                                    counter += 2
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.int16_t -> {
                            for (index in 0 until (formatRawPnpLEntry.format.elements*formatRawPnpLEntry.format.channels)) {
                                if (rawData.size >= (counter + 2)) {

                                    val value = NumberConversion.LittleEndian.bytesToInt16(
                                        rawData,
                                        counter
                                    )
                                    formatRawPnpLEntry.format.values.add(value)

                                    var floatValue = value.toFloat()
                                    formatRawPnpLEntry.format.multiplyFactor?.let {
                                        floatValue *= it
                                    }
                                    formatRawPnpLEntry.format.valuesFloat.add(floatValue)

                                    counter += 2
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.uint32_t -> {
                            for (index in 0 until (formatRawPnpLEntry.format.elements*formatRawPnpLEntry.format.channels)) {
                                if (rawData.size >= (counter + 4)) {

                                    val value = NumberConversion.LittleEndian.bytesToUInt32(
                                        rawData,
                                        counter
                                    )
                                    formatRawPnpLEntry.format.values.add(value)

                                    var floatValue = value.toFloat()
                                    formatRawPnpLEntry.format.multiplyFactor?.let {
                                        floatValue *= it
                                    }
                                    formatRawPnpLEntry.format.valuesFloat.add(floatValue)

                                    counter += 4
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.int32_t -> {
                            for (index in 0 until (formatRawPnpLEntry.format.elements*formatRawPnpLEntry.format.channels)) {
                                if (rawData.size >= (counter + 4)) {

                                    val value = NumberConversion.LittleEndian.bytesToInt32(
                                        rawData,
                                        counter
                                    )
                                    formatRawPnpLEntry.format.values.add(value)

                                    var floatValue = value.toFloat()
                                    formatRawPnpLEntry.format.multiplyFactor?.let {
                                        floatValue *= it
                                    }
                                    formatRawPnpLEntry.format.valuesFloat.add(floatValue)

                                    counter += 4
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.float -> {
                            for (index in 0 until (formatRawPnpLEntry.format.elements*formatRawPnpLEntry.format.channels)) {
                                if (rawData.size >= (counter + 4)) {
                                    var value = NumberConversion.LittleEndian.bytesToFloat(
                                        rawData,
                                        counter
                                    )
                                    formatRawPnpLEntry.format.values.add(value)

                                    formatRawPnpLEntry.format.multiplyFactor?.let {
                                        value *= it
                                    }
                                    formatRawPnpLEntry.format.valuesFloat.add(value)

                                    counter += 4
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.enum -> {
                            for (index in 0 until (formatRawPnpLEntry.format.elements*formatRawPnpLEntry.format.channels)) {
                                if (rawData.size > counter) {
                                    var value = NumberConversion.byteToUInt8(
                                        rawData,
                                        counter
                                    ).toInt()

                                    val outputString =
                                        formatRawPnpLEntry.format.labelsParsed?.firstOrNull { entry ->
                                            entry.value == value
                                        }?.label ?: "NotRecognized"
                                    formatRawPnpLEntry.format.values.add(outputString)

                                    formatRawPnpLEntry.format.valuesFloat.add(value.toFloat())
                                    counter++
                                }
                            }
                        }
                    }
                }
            }
            foundStream.customFormat?.let { customFormat ->
                customFormat.output.forEach { output ->
                    output.values.clear()
                    when (output.type) {
                        RawCustomEntry.RawPnPLCustomEntryFormat.int8_t -> {
                            for (index in 0 until output.elements) {
                                if (rawData.size > counter) {
                                    output.values.add(rawData[counter])
                                    counter++
                                }
                            }
                        }

                        RawCustomEntry.RawPnPLCustomEntryFormat.char,
                        RawCustomEntry.RawPnPLCustomEntryFormat.uint8_t -> {
                            for (index in 0 until output.elements) {
                                if (rawData.size >= counter) {
                                    output.values.add(
                                        NumberConversion.byteToUInt8(
                                            rawData,
                                            counter
                                        ).toInt()
                                    )
                                    counter++
                                }
                            }
                        }

                        RawCustomEntry.RawPnPLCustomEntryFormat.uint16_t -> {
                            for (index in 0 until output.elements) {
                                if (rawData.size >= (counter + 2)) {
                                    output.values.add(
                                        NumberConversion.LittleEndian.bytesToUInt16(
                                            rawData,
                                            counter
                                        )
                                    )
                                    counter += 2
                                }
                            }
                        }

                        RawCustomEntry.RawPnPLCustomEntryFormat.int16_t -> {
                            for (index in 0 until output.elements) {
                                if (rawData.size >= (counter + 2)) {
                                    output.values.add(
                                        NumberConversion.LittleEndian.bytesToInt16(
                                            rawData,
                                            counter
                                        )
                                    )
                                    counter += 2
                                }
                            }
                        }

                        RawCustomEntry.RawPnPLCustomEntryFormat.uint32_t -> {
                            for (index in 0 until output.elements) {
                                if (rawData.size >= (counter + 4)) {
                                    output.values.add(
                                        NumberConversion.LittleEndian.bytesToUInt32(
                                            rawData,
                                            counter
                                        )
                                    )
                                    counter += 4
                                }
                            }
                        }

                        RawCustomEntry.RawPnPLCustomEntryFormat.int32_t -> {
                            for (index in 0 until output.elements) {
                                if (rawData.size >= (counter + 4)) {
                                    output.values.add(
                                        NumberConversion.LittleEndian.bytesToInt32(
                                            rawData,
                                            counter
                                        )
                                    )
                                    counter += 4
                                }
                            }
                        }

                        RawCustomEntry.RawPnPLCustomEntryFormat.float -> {
                            for (index in 0 until output.elements) {
                                if (rawData.size >= (counter + 4)) {
                                    output.values.add(
                                        NumberConversion.LittleEndian.bytesToFloat(
                                            rawData,
                                            counter
                                        )
                                    )
                                    counter += 4
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return streamId
}