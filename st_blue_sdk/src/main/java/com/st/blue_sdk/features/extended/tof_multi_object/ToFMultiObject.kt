/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.tof_multi_object

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.tof_multi_object.request.CommandPresenceRecognition
import com.st.blue_sdk.utils.NumberConversion

class ToFMultiObject(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<ToFMultiObjectInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "ToF Multi Object"
    }

    var nObjects = 0

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<ToFMultiObjectInfo> {
        val nObj = (data.size - dataOffset) / 2
        val presence =
            if (((data.size - dataOffset) % 2) == 1)
                NumberConversion.byteToUInt8(data, dataOffset + nObj * 2)
            else
                0

        val distanceObjs = mutableListOf<FeatureField<Short>>()

        nObjects = nObj

        //Fill the Objects' Distance
        for (objNum in 0 until nObj) {
            distanceObjs.add(
                FeatureField(
                    name = "Obj_${objNum}",
                    value = NumberConversion.LittleEndian.bytesToUInt16(
                        data,
                        dataOffset + objNum * 2,
                    ).toShort(),
                    unit = "mm",
                    min = 0,
                    max = 4000
                )
            )
        }

        val tofMultiObj = ToFMultiObjectInfo(
            nObjsFound = FeatureField(
                value = nObj.toShort(),
                name = "objects"
            ),
            distanceObjs = distanceObjs,
            presenceFound = FeatureField(
                value = presence,
                name = "presences"
            )
        )

        return FeatureUpdate(
            timeStamp = timeStamp,
            rawData = data,
            readByte = data.size - dataOffset,
            data = tofMultiObj
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is CommandPresenceRecognition -> {
                if (command.enable) {
                    packCommandRequest(
                        featureBit,
                        CommandPresenceRecognition.TOF_PRESENCE_ENABLE,
                        byteArrayOf()
                    )
                } else {
                    packCommandRequest(
                        featureBit,
                        CommandPresenceRecognition.TOF_PRESENCE_DISABLE,
                        byteArrayOf()
                    )
                }
            }
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}