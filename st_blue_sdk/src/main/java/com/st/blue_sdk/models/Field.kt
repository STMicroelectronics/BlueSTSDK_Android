/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.models;

data class Field(
    val unit: String,
    val name: String,
    val type: Type,
    val max: Number,
    val min: Number,
    val toPlot: Boolean = true
) {
    enum class Type {
        Float,
        Int64,
        UInt32,
        Int32,
        UInt16,
        Int16,
        UInt8,
        Int8,
        ByteArray
    }
}
