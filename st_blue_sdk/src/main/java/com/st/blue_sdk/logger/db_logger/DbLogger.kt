/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.logger.db_logger

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.logger.Logger
import com.st.blue_sdk.logger.db_logger.db.LoggerDB
import com.st.blue_sdk.logger.db_logger.db.LoggerDao
import com.st.blue_sdk.logger.db_logger.db.model.FeatureLog
import com.st.blue_sdk.logger.db_logger.db.model.FeatureUpdateLog
import com.st.blue_sdk.models.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbLogger @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val db: LoggerDB,
    private val loggerDao: LoggerDao
) : Logger {
    companion object {
        const val TAG = "DbLogger"
    }

    private var logJob: Job? = null

    override var isEnabled: Boolean = false
        set(value) {
            if (value) {
                startLog = Date()
            } else {
                logJob?.cancel()
            }
            field = value
        }

    override val id = TAG

    private var startLog = Date()

    private fun String.logPrettify() =
        this.replace("%", "%%")
            .replace("\n", " ")
            .replace("\t", " ")

    override fun clear() {
        coroutineScope.launch {
            db.clearAllTables()
        }
    }

    override fun log(node: Node, feature: Feature<*>, update: FeatureUpdate<*>): Boolean {
        if (isEnabled) {
            logJob?.cancel()
            logJob = coroutineScope.launch {
                val featureLog = FeatureLog(
                    nodeName = node.friendlyName,
                    featureName = feature.name,
                    timestamp = startLog
                )
                loggerDao.addLog(featureLog)
                loggerDao.addLog(
                    FeatureUpdateLog(
                        featureLogId = featureLog.id,
                        timestamp = update.notificationTime,
                        hostTimestamp = update.notificationTime.time - startLog.time,
                        data = update.data.toString().logPrettify(),
                        rawData = update.rawData.contentToString()
                    )
                )
            }
        }
        return isEnabled
    }
}