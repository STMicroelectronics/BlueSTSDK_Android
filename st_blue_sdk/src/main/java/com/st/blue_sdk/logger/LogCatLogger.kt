/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.logger

import android.util.Log
import com.st.blue_sdk.BuildConfig
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.models.Node
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogCatLogger @Inject constructor() : Logger {
    companion object {
        const val TAG = "LogCatLogger"
    }

    private var startLog = Date()

    override var isEnabled = BuildConfig.DEBUG
        set(value) {
            if (value) {
                startLog = Date()
            }

            field = value
        }

    override val id = TAG

    override fun clear() = Unit

    override fun log(node: Node, feature: Feature<*>, update: FeatureUpdate<*>): Boolean {
        val time = System.currentTimeMillis() - startLog.time
        val friendlyName = node.friendlyName.replace("@", "_")
        if (isEnabled) {
            Log.d(
                TAG, "$time\n" +
                        "$friendlyName ${feature.name}\n" +
                        "${update.rawData.contentToString()}\n" +
                        "${update.data}"
            )
        }

        return isEnabled
    }
}
