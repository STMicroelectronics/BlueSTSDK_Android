/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.raw_pnpl_controlled

import android.util.Log
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.model.RawPnPLEntry
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.model.RawPnPLEntryFormat
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.model.RawPnPLStreamIdEntry
import com.st.blue_sdk.utils.NumberConversion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

class RawPnPLControlled(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<RawPnPLControlledInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier,
    hasTimeStamp = false
) {

    companion object {
        const val NAME = "Raw PnPL Controlled"
        const val PROPERTY_NAME_ST_BLE_STREAM = "st_ble_stream"
        val HIDE_PROPERTIES_NAME = arrayOf("min", "max", "unit", "format")
        const val STREAM_ID_NOT_FOUND: Int = 0xFF
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<RawPnPLControlledInfo> {

        val rawValues = mutableListOf<FeatureField<Byte>>()

        for (dataNum in 0 until (data.size - dataOffset)) {
            rawValues.add(
                FeatureField(
                    name = "B$dataNum",
                    value = data[dataOffset + dataNum]
                )
            )
        }
        val rawPnPLControlled = RawPnPLControlledInfo(
            data = rawValues
        )
        return FeatureUpdate(
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
    rawPnPLFormat: MutableList<RawPnPLStreamIdEntry>,
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
                    if (jsonEntry.containsKey(RawPnPLControlled.PROPERTY_NAME_ST_BLE_STREAM)) {
                        val configuration =
                            jsonEntry[RawPnPLControlled.PROPERTY_NAME_ST_BLE_STREAM]
                        if (configuration is JsonObject) {
                            val componentName = entry.key
                            var streamId = 0
                            val formats = mutableListOf<RawPnPLEntry>()

                            configuration.entries.forEach { singleEntry ->
                                if (singleEntry.value is JsonPrimitive) {
                                    streamId =
                                        (singleEntry.value as JsonPrimitive).content.toInt()
                                } else if (singleEntry.value is JsonObject) {
                                    val name = singleEntry.key

                                    val displayName = searchPropertyDisplayName(
                                        modelUpdates = modelUpdates,
                                        compName = entryKey,
                                        propertyName = RawPnPLControlled.PROPERTY_NAME_ST_BLE_STREAM,
                                        fieldName = name
                                    )
                                    val format =
                                        Json.decodeFromJsonElement<RawPnPLEntryFormat>(
                                            singleEntry.value
                                        )
                                    formats.add(
                                        RawPnPLEntry(
                                            displayName = displayName,
                                            name = name,
                                            format = format
                                        )
                                    )
                                }

                            }

                            rawPnPLFormat.add(
                                RawPnPLStreamIdEntry(
                                    componentName = componentName,
                                    streamId = streamId,
                                    formats = formats
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

fun decodeRawPnPLData(
    data: List<FeatureField<Byte>>,
    rawPnPLFormat: MutableList<RawPnPLStreamIdEntry>
): Int {
    var streamId = RawPnPLControlled.STREAM_ID_NOT_FOUND
    if (data.isNotEmpty()) {
        var counter = 0;
        val rawData = data.map { value -> value.value }.toByteArray()
        streamId = rawData[counter].toInt()
        counter++

        //Search the right component
        val foundStream = rawPnPLFormat.firstOrNull { it.streamId == streamId }
        foundStream?.let {
            foundStream.formats.forEach { formatRawPnpLEntry ->

                if (formatRawPnpLEntry.format.enable) {
                    //Reset the list of values
                    formatRawPnpLEntry.format.values.clear()

                    when (formatRawPnpLEntry.format.format) {
                        RawPnPLEntryFormat.RawPnPLEntryFormat.uint8_t -> {
                            for (index in 0 until formatRawPnpLEntry.format.elements) {
                                if (rawData.size > counter) {
                                    formatRawPnpLEntry.format.values.add(rawData[counter])
                                    counter++
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.int8_t -> {
                            for (index in 0 until formatRawPnpLEntry.format.elements) {
                                if (rawData.size >= counter) {
                                    formatRawPnpLEntry.format.values.add(
                                        NumberConversion.byteToUInt8(
                                            rawData,
                                            counter
                                        ).toInt()
                                    )
                                    counter++
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.uint16_t -> {
                            for (index in 0 until formatRawPnpLEntry.format.elements) {
                                if (rawData.size >= (counter + 2)) {
                                    formatRawPnpLEntry.format.values.add(
                                        NumberConversion.LittleEndian.bytesToUInt16(
                                            rawData,
                                            counter
                                        )
                                    )
                                    counter += 2
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.int16_t -> {
                            for (index in 0 until formatRawPnpLEntry.format.elements) {
                                if (rawData.size >= (counter + 2)) {
                                    formatRawPnpLEntry.format.values.add(
                                        NumberConversion.LittleEndian.bytesToInt16(
                                            rawData,
                                            counter
                                        )
                                    )
                                    counter += 2
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.uint32_t -> {
                            for (index in 0 until formatRawPnpLEntry.format.elements) {
                                if (rawData.size >= (counter + 4)) {
                                    formatRawPnpLEntry.format.values.add(
                                        NumberConversion.LittleEndian.bytesToUInt32(
                                            rawData,
                                            counter
                                        )
                                    )
                                    counter += 4
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.int32_t -> {
                            for (index in 0 until formatRawPnpLEntry.format.elements) {
                                if (rawData.size >= (counter + 4)) {
                                    formatRawPnpLEntry.format.values.add(
                                        NumberConversion.LittleEndian.bytesToInt32(
                                            rawData,
                                            counter
                                        )
                                    )
                                    counter += 4
                                }
                            }
                        }

                        RawPnPLEntryFormat.RawPnPLEntryFormat.float -> {
                            for (index in 0 until formatRawPnpLEntry.format.elements) {
                                if (rawData.size >= (counter + 4)) {
                                    formatRawPnpLEntry.format.values.add(
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