/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.gnss

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion

class GNSS(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<GNSSInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Global Navigation Satellite System"
        const val NUMBER_BYTES = 14
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<GNSSInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val gssInfo = GNSSInfo(
            latitude = FeatureField(
                value = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset) / (1e7f),
                max = 900000000f,
                min = -900000000f,
                unit = "Lat",
                name = "Latitude"
            ),
            longitude = FeatureField(
                value = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset + 4) / (1e7f),
                max = 1800000000f,
                min = -1800000000f,
                unit = "Lon",
                name = "Longitude"
            ),
            altitude = FeatureField(
                value = NumberConversion.LittleEndian.bytesToInt32(data, dataOffset + 8) / (1e3f),
                max = Int.MAX_VALUE.toFloat(),
                min = Int.MIN_VALUE.toFloat(),
                unit = "m",
                name = "Altitude"
            ),
            numSatellites = FeatureField(
                value = NumberConversion.byteToUInt8(data, dataOffset + 12).toInt(),
                name = "Num Satellites"
            ),
            signalQuality = FeatureField(
                value = NumberConversion.byteToUInt8(data, dataOffset + 13).toInt(),
                unit = "dB-Hz",
                name = "Sig Quality"
            )
        )

        return FeatureUpdate(
            timeStamp = timeStamp,
            rawData = data,
            readByte = NUMBER_BYTES,
            data = gssInfo
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}