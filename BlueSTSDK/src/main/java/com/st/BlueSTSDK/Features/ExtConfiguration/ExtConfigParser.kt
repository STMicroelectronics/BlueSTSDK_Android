package com.st.BlueSTSDK.Features.ExtConfiguration

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceParser
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.HSDCmd
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.WifSettings

class ExtConfigParser {

    companion object {

        private val gsonEncDec = GsonBuilder()
                .create()

        //create JSON String from Command Classes
        @JvmStatic
        fun createExtConfigCommandJSON(obj: Any):String{
            return gsonEncDec.toJson(obj)
        }

        @JvmStatic
        fun createExtConfigArgumentJSON(obj: Any): JsonElement {
            return gsonEncDec.toJsonTree(obj)
        }

        @JvmStatic
        fun getJsonObj(rawData:ByteArray):JsonObject?{
            val commandString = rawData.toString(Charsets.UTF_8).dropLast(1)
            return try {
                gsonEncDec.fromJson(commandString,JsonObject::class.java)
            }catch (e: JsonSyntaxException){
                Log.e("ExtConfigParser","error parsing the response: $e")
                Log.e("ExtConfigParser",commandString)
                null
            }
        }

        @JvmStatic
        fun extractCommandList(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("Commands")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).CommandList
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the Commands: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandInfo(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("Info")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).info
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the Info: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandHelp(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("Help")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).help
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the Help: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandCertificate(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("Certificate")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).certificate
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the Certificate: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandVersionFw(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("VersionFW")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).versionFw
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the VersionFW: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandSTM32UID(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("UID")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).stm32UID
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the STM32_UID: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandPowerStatus(obj: JsonObject?): String? {
            obj ?: return null
            if(obj.has("PowerStatus")){
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).powerStatus
                }catch (e: JsonSyntaxException){
                    Log.e("ExtConfigParser","error parsing the PowerStatus: $e")
                    Log.e("HExtConfigParser",obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractLCustomCommandList(obj: JsonObject?): List<CustomCommand>? {
            obj ?: return null
            if(obj.has("CustomCommands")){
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).CustomCommandList
                }catch (e: JsonSyntaxException){
                    Log.e("ExtConfigParser","error parsing the listCustomCommand: $e")
                    Log.e("HExtConfigParser",obj.toString())
                    null
                }
            }
            return null
        }
    }
}