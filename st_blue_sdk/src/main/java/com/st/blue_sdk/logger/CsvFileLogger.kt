/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.logger

import com.st.blue_sdk.di.LogDirectoryPath
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.models.Node
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvFileLogger @Inject constructor(
    @param:LogDirectoryPath private val logDirectoryPath: String,
) : Logger {
    companion object {
        const val TAG = "CsvFileLogger"
        const val FILE_DATE_FORMAT = "yyyyMMdd_HHmmss"
        const val HEADER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        const val DATE_FORMAT = "dd/MM/yyyy HH:mm:ss.SSS"
    }

    private val formatterMap = mutableMapOf<String, Formatter>()

    private fun getOrCreateFormatter(
        feature: Feature<*>,
        update: FeatureUpdate<*>
    ): Formatter {
        return formatterMap[feature.name] ?: createWithHeader(
            feature = feature,
            update = update
        )
    }

    private fun createFile(path: String, fileName: String): File {
        val storageDir = File(path)
        if (storageDir.exists().not()) {
            storageDir.mkdirs()
        }
        return File(storageDir.toString(), fileName)
    }

    private fun createWithHeader(
        feature: Feature<*>,
        update: FeatureUpdate<*>
    ): Formatter {
        val dateFormatter = SimpleDateFormat(FILE_DATE_FORMAT, Locale.getDefault())
        val sessionPrefix = dateFormatter.format(startLog)
        val featureName = feature.name
        val file = createFile(
            path = logDirectoryPath,
            fileName = "${sessionPrefix}_$featureName.csv"
        )
        val fileFormatter = Formatter(file)
        val headerDateFormatter = SimpleDateFormat(HEADER_DATE_FORMAT, Locale.getDefault())
        val startLogDate = headerDateFormatter.format(startLog)
        fileFormatter.format("Log start on, $startLogDate\n")
        fileFormatter.format("Feature, $featureName\n")
        fileFormatter.format("NodeName, HostTimestamp (ms), ${update.logHeader}\n")
        formatterMap[featureName] = fileFormatter
        return fileFormatter
    }

    override var isEnabled: Boolean = false
        set(value) {
            if (value) {
                startLog = Date()
            } else {
                formatterMap.values.forEach { it.close() }
                formatterMap.clear()
            }

            field = value
        }

    override val id = TAG

    private var startLog = Date()

    override fun clear() {
        val logDir = File(logDirectoryPath)
        if (logDir.exists() && logDir.isDirectory) {
            logDir.deleteRecursively()
        }
    }

    override fun log(node: Node, feature: Feature<*>, update: FeatureUpdate<*>): Boolean {
        if (isEnabled) {
            val formatter = getOrCreateFormatter(feature, update)
            formatter.format("${node.friendlyName}, ")
            formatter.format("${update.notificationTime.time - startLog.time}, ")
            formatter.format(update.logValue.replace("\n", " "))
            formatter.format("\n")
            formatter.flush()
        }
        return isEnabled
    }
}