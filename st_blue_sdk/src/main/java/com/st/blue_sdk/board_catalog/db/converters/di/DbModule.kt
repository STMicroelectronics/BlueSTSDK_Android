/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.db.converters.di

import android.content.Context
import androidx.room.Room
import com.st.blue_sdk.board_catalog.db.BoardCatalogDB
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
    fun provideBoardCatalogDB(@ApplicationContext context: Context): BoardCatalogDB =
        Room.databaseBuilder(
            context,
            BoardCatalogDB::class.java,
            "blue_st_sdk_db"
            ).fallbackToDestructiveMigration(true).build()

    @Provides
    @Singleton
    fun provideBoardCatalogDao(db: BoardCatalogDB) = db.boardCatalogDao()
}
