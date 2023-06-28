/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.utils

import android.util.Log
import kotlinx.serialization.json.*

fun JsonElement?.toAny(): Any? =
    when (this) {
        JsonNull -> null
        is JsonPrimitive -> when {
            jsonPrimitive.isString -> jsonPrimitive.contentOrNull
            jsonPrimitive.isBoolean -> jsonPrimitive.booleanOrNull
            jsonPrimitive.isDouble -> jsonPrimitive.doubleOrNull
            jsonPrimitive.isFloat -> jsonPrimitive.floatOrNull
            jsonPrimitive.isInt -> jsonPrimitive.intOrNull
            jsonPrimitive.isLong -> jsonPrimitive.longOrNull
            else -> null
        }
        is JsonArray -> map { it.toAny() }
        is JsonObject -> map { it.key to it.value.toAny() }.toMap()
        else -> throw IllegalStateException("Can't serialize unknown type: $this")
    }

fun JsonObject?.toMapOfAny() =
    this?.map {
        it.key to it.value.toAny()
    }?.toMap() ?: emptyMap()

fun Any?.toJsonElement(): JsonElement =
    when (this) {
        null -> JsonNull
        is JsonElement -> this
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is Enum<*> -> JsonPrimitive(this.toString())
        is Map<*, *> -> JsonObject(this.map { it.key.toString() to it.value.toJsonElement() }
            .toMap())
        is Iterable<*> -> JsonArray(this.map { it.toJsonElement() })
        else -> throw IllegalStateException("Can't serialize unknown type: $this")
    }

val JsonPrimitive.isBoolean
    get() = try {
        this.boolean

        true
    } catch (ex: IllegalStateException) {
        false
    }

val JsonPrimitive.isDouble
    get() = try {
        this.double

        true
    } catch (ex: IllegalStateException) {
        false
    }

val JsonPrimitive.isInt
    get() = try {
        this.int

        true
    } catch (ex: IllegalStateException) {
        false
    }

val JsonPrimitive.isFloat
    get() = try {
        this.float

        true
    } catch (ex: IllegalStateException) {
        false
    }

val JsonPrimitive.isLong
    get() = try {
        this.long

        true
    } catch (ex: IllegalStateException) {
        false
    }

fun String.logJson(tag: String? = "stringToJson") {
    Log.d(tag, "*** - ${this.length}")
    this.chunked(150).forEach {
        Log.d(tag, it)
    }
    Log.d(tag, "*** - ${this.length}")
}

fun List<*>.toJsonElement(): JsonElement {
    val list: MutableList<JsonElement> = mutableListOf()
    this.forEach { value ->
        when (value) {
            null -> list.add(JsonNull)
            is Map<*, *> -> list.add(value.toJsonElement())
            is List<*> -> list.add(value.toJsonElement())
            is Boolean -> list.add(JsonPrimitive(value))
            is Number -> list.add(JsonPrimitive(value))
            is String -> list.add(JsonPrimitive(value))
            is Enum<*> -> list.add(JsonPrimitive(value.toString()))
            else -> throw IllegalStateException("Can't serialize unknown collection type: $value")
        }
    }
    return JsonArray(list)
}

fun Map<*, *>.toJsonElement(): JsonElement {
    val map: MutableMap<String, JsonElement> = mutableMapOf()
    this.forEach { (key, value) ->
        key as String
        when (value) {
            null -> map[key] = JsonNull
            is Map<*, *> -> map[key] = value.toJsonElement()
            is List<*> -> map[key] = value.toJsonElement()
            is Boolean -> map[key] = JsonPrimitive(value)
            is Number -> map[key] = JsonPrimitive(value)
            is String -> map[key] = JsonPrimitive(value)
            is Enum<*> -> map[key] = JsonPrimitive(value.toString())
            else -> throw IllegalStateException("Can't serialize unknown type: $value")
        }
    }

    return JsonObject(map)
}