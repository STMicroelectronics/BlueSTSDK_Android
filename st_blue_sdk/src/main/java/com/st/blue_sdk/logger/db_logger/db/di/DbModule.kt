/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.logger.db_logger.db.di

import android.content.Context
import androidx.room.Room
import com.st.blue_sdk.logger.db_logger.db.LoggerDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideLoggerDB(@ApplicationContext context: Context): LoggerDB =
        Room.databaseBuilder(
            context,
            LoggerDB::class.java,
            "logger_db"
        ).fallbackToDestructiveMigration(true).build()

    @Provides
    @Singleton
    fun provideLoggerDao(db: LoggerDB) = db.loggerDao()
}
