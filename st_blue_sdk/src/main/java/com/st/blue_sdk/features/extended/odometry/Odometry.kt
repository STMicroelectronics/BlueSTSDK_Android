/*
 * Copyright (c) 2023(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.odometry

import com.st.blue_sdk.features.*
// Removed NumberConversion import as it's no longer directly used in extractData for parsing X, Y, Z
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Odometry(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<OdometryInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val DATA_MAX = 1000f // Example: Max expected value for a component. Adjust if needed.
        const val DATA_MIN = -1000f // Example: Min expected value for a component. Adjust if needed.
        const val NUMBER_BYTES = 12 // 3 components (X, Y, Theta) * 4 bytes/float
        const val NAME = "Odometry"
        private val TAG = Odometry::class.java.simpleName
        const val COMMAND_ODOMETER_RESET: Byte = 0xf

    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<OdometryInfo> {
        // 1. Basic validation for the mandatory 12 bytes
        require(data.size - dataOffset >= NUMBER_BYTES) {
            "There are no $NUMBER_BYTES bytes available to read for $name feature. Data size: ${data.size}, offset: $dataOffset"
        }

        // 2. Use existing function to get X, Y, Theta
        val (xValue, yValue, thetaValue) = parseData(data, dataOffset)

        // 3. SEPARATELY read the 4th value (Magnetic Direction) if bytes exist
        var magValue: Float? = null
        var bytesRead = NUMBER_BYTES // Start with 12 bytes

        // Check if we have at least 4 more bytes (12 + 4 = 16 bytes total)
        if (data.size - dataOffset >= NUMBER_BYTES + 4) {
            magValue = ByteBuffer.wrap(data, dataOffset + NUMBER_BYTES, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .float

            bytesRead += 4 // We consumed 16 bytes now
        }

        //Log.i(TAG, "Extracted: X=$xValue, Y=$yValue, Theta=$thetaValue, Mag=$magValue")

        // 4. Create OdometryInfo (assuming you updated it to accept the optional 4th param)
        val odometry = OdometryInfo(
            x = FeatureField(
                value = xValue,
                max = DATA_MAX,
                min = DATA_MIN,
                unit = "m",
                name = "X"
            ),
            y = FeatureField(
                value = yValue,
                max = DATA_MAX,
                min = DATA_MIN,
                unit = "m",
                name = "Y"
            ),
            theta = FeatureField(
                value = thetaValue,
                max = DATA_MAX,
                min = DATA_MIN,
                unit = "radians",
                name = "Theta"
            ),
            // Pass the value we just read (or null if we didn't read it)
            magneticDirection = magValue?.let {
                FeatureField(
                    value = it,
                    max = 360f,     // Example max for degrees
                    min = 0f,
                    unit = "degrees",
                    name = "MagDir"
                )
            }
        )

        return FeatureUpdate(
            featureName = name,
            timeStamp = timeStamp,
            rawData = data,
            readByte = bytesRead, // dynamically updates to 12 or 16
            data = odometry
        )
    }

    // Your parseData function remains the same
    fun parseData(data: ByteArray, offset: Int): Triple<Float, Float, Float> {
        val buffer = ByteBuffer.wrap(data, offset, data.size - offset) // Use offset and limit buffer size
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        val x = buffer.getFloat() // Reads from current position (offset)
        // val xdash = buffer.getFloat(offset + 2) // This would be problematic if x already read 4 bytes.
        // If xdash is needed, parsing logic here needs care.
        // For now, assuming x, y, theta are sequential floats.

        val y = buffer.getFloat() // Reads next 4 bytes
        val theta = buffer.getFloat() // Reads next 4 bytes
        // Log.d(TAG, "ByteBuffer parsing - X: $x, Y: $y, Theta: $theta") // Optional: for finer debug
        return Triple(x, y, theta)
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? {
        return when (command) {
            is OdometryResetCommand -> packCommandRequest(
                featureBit,
                COMMAND_ODOMETER_RESET,
                command.payload
            )
            else -> null
        }
    }
    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}