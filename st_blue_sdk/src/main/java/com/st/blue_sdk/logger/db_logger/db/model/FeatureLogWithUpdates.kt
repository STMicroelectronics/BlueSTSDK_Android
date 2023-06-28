/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.logger.db_logger.db.model

import androidx.room.Embedded
import androidx.room.Relation

data class FeatureLogWithUpdates(
    @Embedded
    val featureLog: FeatureLog,
    @Relation(
        parentColumn = "id",
        entityColumn = "featureLogId"
    )
    val featureUpdateLog: List<FeatureUpdateLog>
)
