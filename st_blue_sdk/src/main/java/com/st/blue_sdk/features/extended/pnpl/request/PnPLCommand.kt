/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.pnpl.request

import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.utils.logJson
import com.st.blue_sdk.utils.toJsonElement
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class PnPLCommand(feature: PnPL, val cmd: PnPLCmd) :
    FeatureCommand(feature = feature, commandId = 0.toByte())

class PnPLCmd(
    val component: String? = null,
    val command: String,
    val request: String? = null,
    val fields: Map<String, Any>? = null
) {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val jsonElementFields =
        fields?.mapValues { it.value.toJsonElement() } ?: emptyMap()

    val jsonString: String
        get() {
            val mainKey = if (component.isNullOrEmpty()) {
                command
            } else {
                "${component}*${command}"
            }

            val jsonString = if (request.isNullOrEmpty()) {
                json.encodeToString(
                    mapOf(mainKey to jsonElementFields)
                )
            } else {
                if (fields == null) {
                    json.encodeToString(
                        mapOf(mainKey to request)
                    )
                } else {
                    json.encodeToString(
                        mapOf(mainKey to mapOf(request to jsonElementFields))
                    )
                }
            }

            jsonString.logJson(tag = PnPL.TAG)

            return jsonString
        }

    val jsonObject: JsonObject
        get() = json.decodeFromString(jsonString)

    companion object {
        val ALL = PnPLCmd(command = "get_status", request = "all")
        val DEVICE_INFO = PnPLCmd(command = "get_status", request = "deviceinfo")
        val LOG_CONTROLLER = PnPLCmd(command = "get_status", request = "log_controller")
        val TAGS_INFO = PnPLCmd(command = "get_status", request = "tags_info")
        val START_LOG = PnPLCmd(
            component = "log_controller",
            command = "start_log",
            fields = mapOf("interface" to 0)
        )
        val STOP_LOG = PnPLCmd(component = "log_controller", command = "stop_log")

        val ES1 = PnPLCmd(command = "element", fields = mapOf("param" to "plain"))
        val ES2 = PnPLCmd(command = "element", request = "param", fields = mapOf("obj" to "wow"))
        val ES3 = PnPLCmd(
            component = "element",
            command = "param",
            request = "request",
            fields = mapOf("obj" to "wow")
        )
        val ES4 = PnPLCmd(
            component = "element",
            command = "param",
            request = "request",
            fields = mapOf(
                "objs" to listOf(
                    mapOf("wow1" to 1),
                    "wow2",
                    "wow3",
                    "wow4"
                )
            )
        )
        val ES5 = PnPLCmd(component = "element", command = "param", fields = emptyMap())
    }
}

/*
{"log_controller*get_status":""}
{"element":{"param":"plain"}}
{"element":{"param":{"obj":"wow"}}}
{"element*param":{"request":{"obj":"wow"}}}
{"element*param":{"request":{"objs":[{"wow1":1},"wow2","wow3","wow4"]}}}
{"element*param":{}}
 */