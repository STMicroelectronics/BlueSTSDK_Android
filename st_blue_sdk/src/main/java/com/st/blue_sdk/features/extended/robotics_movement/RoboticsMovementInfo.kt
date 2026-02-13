/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.features.extended.robotics_movement

import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.robotics_movement.request.RoboticsActionBits
import com.st.blue_sdk.logger.Loggable

data class RoboticsMovementInfo(
    val commandId : Short,
    val action: List<RoboticsActionBits>?,
    val data: Any?
): Loggable {

    // Dynamic Header: "Command ID, Actions, [Dynamic Field Names...]"
    override val logHeader: String = buildString {
        append("Command ID, Actions")
        (data as? List<*>)?.filterIsInstance<FeatureField<*>>()?.let { fields ->
            if (fields.isNotEmpty()) {
                append(", ")
                append(fields.joinToString(", ") { it.name })
            }
        }
    }

    // Dynamic Value: "0x10, ARM|DISARM, [Dynamic Field Values...]"
    override val logValue: String = buildString {
        append("$commandId, ")
        // Use pipe separator for actions to keep single CSV column
        append(action?.joinToString("|") ?: "None")

        (data as? List<*>)?.filterIsInstance<FeatureField<*>>()?.let { fields ->
            if (fields.isNotEmpty()) {
                append(", ")
                append(fields.joinToString(", ") { it.value.toString() })
            }
        }
    }

    // Extracts CommandID and any numeric data (X, Y, Theta) for graphing
    override val logDoubleValues: List<Double> = buildList {
        add(commandId.toDouble())
        (data as? List<*>)?.filterIsInstance<FeatureField<*>>()?.forEach { item ->
            if (item.value is Number) {
                add(item.value.toDouble())
            }
        }
    }

    override fun toString(): String {
        val sampleValue = StringBuilder()

        // Print the Command ID
        sampleValue.append("\tCommand ID = $commandId\n")

        // Print Actions if they exist
        action?.let {
            sampleValue.append("\tActions = ${it.joinToString()}\n")
        }

        // Print Data payload
        (data as? List<*>)?.forEach { item ->
            if (item is FeatureField<*>) {
                val unitStr = if (item.unit != null) " ${item.unit}" else ""
                sampleValue.append("\t${item.name} = ${item.value}$unitStr\n")
            }
        }

        return sampleValue.toString()
    }
}