/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.logger.db_logger.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.st.blue_sdk.logger.db_logger.db.model.FeatureLog
import com.st.blue_sdk.logger.db_logger.db.model.FeatureUpdateLog

@Dao
interface LoggerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLog(featureLog: FeatureLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLog(featureUpdateLog: FeatureUpdateLog)
}
