/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.fitness_activity

import com.st.blue_sdk.features.*
import com.st.blue_sdk.features.extended.fitness_activity.request.EnableActivityDetection
import com.st.blue_sdk.utils.NumberConversion
import kotlin.experimental.and

class FitnessActivity(
    name: String = NAME,
    type: Type = Type.EXTENDED,
    isEnabled: Boolean,
    identifier: Int
) : Feature<FitnessActivityInfo>(
    name = name,
    type = type,
    isEnabled = isEnabled,
    identifier = identifier
) {
    companion object {
        const val NAME = "Fitness Activity"
        const val NUMBER_BYTES = 2
    }

    override fun extractData(
        timeStamp: Long,
        data: ByteArray,
        dataOffset: Int
    ): FeatureUpdate<FitnessActivityInfo> {
        require(data.size - dataOffset >= NUMBER_BYTES) { "There are no $NUMBER_BYTES bytes available to read for $name feature" }

        val fitnessActivity = FitnessActivityInfo(
            activity = FeatureField(
                value = getActivityType(NumberConversion.byteToUInt8(data, dataOffset)),
                name = "Activity"
            ),
            count = FeatureField(
                value = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset + 1),
                name = "ActivityCounter"
            )
        )

        return FeatureUpdate(
            timeStamp = timeStamp, rawData = data, readByte = NUMBER_BYTES, data = fitnessActivity
        )
    }

    private fun getActivityType(activity: Short) = when ((activity and 0x0F).toInt()) {
        0x00 -> FitnessActivityType.NoActivity
        0x01 -> FitnessActivityType.BicepCurl
        0x02 -> FitnessActivityType.Squat
        0x03 -> FitnessActivityType.PushUp
        else -> FitnessActivityType.Error
    }

    private fun getActivityCode(activity: FitnessActivityType) = when (activity) {
        FitnessActivityType.BicepCurl -> 0x01.toByte()
        FitnessActivityType.Squat -> 0x02.toByte()
        FitnessActivityType.PushUp -> 0x03.toByte()
        else -> 0x00.toByte() // NoActivity and Error case...
    }

    override fun packCommandData(
        featureBit: Int?,
        command: FeatureCommand
    ): ByteArray? {
        return when (command) {
            is EnableActivityDetection -> {
                byteArrayOf(getActivityCode(command.activityType))
            }
            else -> null
        }
    }

    override fun parseCommandResponse(data: ByteArray): FeatureResponse? = null
}