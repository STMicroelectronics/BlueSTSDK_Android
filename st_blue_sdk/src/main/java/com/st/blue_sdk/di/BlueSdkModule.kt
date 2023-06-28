/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.di

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Environment
import android.util.Log
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.BlueManagerImpl
import com.st.blue_sdk.logger.CsvFileLogger
import com.st.blue_sdk.logger.LogCatLogger
import com.st.blue_sdk.logger.Logger
import com.st.blue_sdk.logger.db_logger.DbLogger
import com.st.blue_sdk.services.*
import com.st.blue_sdk.services.audio.AudioService
import com.st.blue_sdk.services.audio.AudioServiceImpl
import com.st.blue_sdk.services.audio.codec.factory.AudioCodecManagerProvider
import com.st.blue_sdk.services.audio.codec.factory.AudioCodecManagerProviderImpl
import com.st.blue_sdk.services.calibration.CalibrationService
import com.st.blue_sdk.services.calibration.CalibrationServiceImpl
import com.st.blue_sdk.services.ota.OtaService
import com.st.blue_sdk.services.ota.OtaServiceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module(includes = [BlueSdkModule.WithProvides::class])
@InstallIn(SingletonComponent::class)
abstract class BlueSdkModule {

    @Binds
    abstract fun bindBlueManager(blueManager: BlueManagerImpl): BlueManager

    @Binds
    abstract fun bindNodeServiceConsumer(consumer: NodeServiceStore): NodeServiceConsumer

    @Binds
    abstract fun bindNodeServiceProducer(producer: NodeServiceStore): NodeServiceProducer

    @Binds
    abstract fun bindNodeServerProducer(producer: NodeServerStore): NodeServerProducer

    @Binds
    abstract fun bindNodeServerConsumer(consumer: NodeServerStore): NodeServerConsumer

    @Binds
    abstract fun bindOtaService(otaService: OtaServiceImpl): OtaService

    @Binds
    abstract fun bindAudioService(audioService: AudioServiceImpl): AudioService

    @Binds
    abstract fun bindCalibrationService(calibrationService: CalibrationServiceImpl): CalibrationService

    @Binds
    abstract fun bindAudioCodecManagerProvider(audioCodecManagerProviderImpl: AudioCodecManagerProviderImpl): AudioCodecManagerProvider

    @Module
    @InstallIn(SingletonComponent::class)
    object WithProvides {

        private val TAG = BlueSdkModule::class.simpleName

        private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "CoroutineExceptionHandler got", exception)
        }

        private val appCoroutineScope =
            CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

        @Provides
        @Singleton
        fun provideBLeScanner(@ApplicationContext context: Context): BluetoothManager {
            return context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }

        @Provides
        @Singleton
        fun provideCoroutineScope(): CoroutineScope {
            return appCoroutineScope
        }

        @Provides
        @Singleton
        @LogDirectoryPath
        fun provideLogDirectoryPath(@ApplicationContext context: Context): String =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/STMicroelectronics/logs"
//            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
//                .toString() + "/STMicroelectronics/logs"

        @Provides
        @JvmStatic
        @IntoSet
        fun bindDbLogger(dbLogger: DbLogger): Logger = dbLogger

        @Provides
        @JvmStatic
        @IntoSet
        fun bindFileLogger(fileLogger: CsvFileLogger): Logger = fileLogger

        @Provides
        @JvmStatic
        @IntoSet
        fun bindLogCatLogger(logCatLogger: LogCatLogger): Logger = logCatLogger
    }
}