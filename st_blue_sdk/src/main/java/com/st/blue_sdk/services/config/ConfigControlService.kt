/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.config

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureCommand
import com.st.blue_sdk.features.FeatureResponse
import kotlinx.coroutines.flow.Flow

interface ConfigControlService {

    val configControlUpdates: Flow<ByteArray>

    suspend fun init()

    fun hasBleConfigService(): Boolean

    suspend fun writeFeatureCommand(
        featureCommand: FeatureCommand,
        feature: Feature<*>,
        writeTimeout: Long = 1000,
        responseTimeout: Long = 2000,
        retry: Int = 0,
        retryDelay: Long = 250
    ): FeatureResponse?
}