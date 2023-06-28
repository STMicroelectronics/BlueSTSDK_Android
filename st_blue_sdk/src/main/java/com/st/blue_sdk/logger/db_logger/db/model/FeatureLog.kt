/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.logger.db_logger.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class FeatureLog(
    val nodeName: String,
    val featureName: String,
    val timestamp: Date
) {

    @PrimaryKey
    var id: Long = nodeName.hashCode() + featureName.hashCode() + timestamp.time
}
