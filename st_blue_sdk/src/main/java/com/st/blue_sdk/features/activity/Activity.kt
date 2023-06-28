/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.activity

import com.st.blue_sdk.features.*
import com.st.blue_sdk.utils.NumberConversion
import java.util.*
import kotlin.experimental.and

class Activity(
    name: String = NAME,
    type: Type = Type.STANDARD,
    isEnabled: Boolean,
    identifier: Int
) : Feature<ActivityInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {

    companion object {
        const val NAME = "Activity Recognition"
    }

    private fun getActivityType(activity: Short) = when ((activity and 0x0F).toInt()) {
        0x00 -> ActivityType.NoActivity
        0x01 -> ActivityType.Stationary
        0x02 -> ActivityType.Walking
        0x03 -> ActivityType.FastWalking
        0x04 -> ActivityType.Jogging
        0x05 -> ActivityType.Biking
        0x06 -> ActivityType.Driving
        0x07 -> ActivityType.Stairs
        0x08 -> ActivityType.AdultInCar
        else -> ActivityType.Error
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<ActivityInfo> {

        require(data.size - dataOffset > 0) { "There are no bytes available to read for $name feature" }

        val numberOfBytes = minOf(data.size - dataOffset, 2)

        val activityInfo =
            if (numberOfBytes == 1) {
                ActivityInfo(
                    activity = FeatureField(
                        max = ActivityType.Error,
                        min = ActivityType.NoActivity,
                        value = getActivityType(NumberConversion.byteToUInt8(data, dataOffset)),
                        name = "Activity"
                    ),
                    algorithm = FeatureField(
                        min = 0,
                        max = 0xFF,
                        value = ActivityInfo.ALGORITHM_NOT_DEFINED,
                        name = "Algorithm"
                    ),
                    date = FeatureField(
                        value = Date(),
                        name = "Date"
                    )
                )
            } else {
                ActivityInfo(
                    activity = FeatureField(
                        max = ActivityType.Error,
                        min = ActivityType.NoActivity,
                        value = getActivityType(NumberConversion.byteToUInt8(data, dataOffset)),
                        name = "Activity"
                    ),
                    algorithm = FeatureField(
                        min = 0,
                        max = 0xFF,
                        value = NumberConversion.byteToUInt8(data, dataOffset + 1),
                        name = "Algorithm"
                    ),
                    date = FeatureField(
                        value = Date(),
                        name = "Date"
                    )
                )
            }

        return FeatureUpdate(
            timeStamp = timeStamp, rawData = data, readByte = numberOfBytes, data = activityInfo
        )
    }

    override fun packCommandData(featureBit: Int?, command: FeatureCommand): ByteArray? = null

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}