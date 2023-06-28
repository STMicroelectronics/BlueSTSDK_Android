/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.di

import android.content.Context
import android.content.SharedPreferences
import com.st.blue_sdk.board_catalog.BoardCatalogRepo
import com.st.blue_sdk.board_catalog.BoardCatalogRepoImpl
import com.st.blue_sdk.di.BlueSdkModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module(includes = [BlueSdkModule.WithProvides::class])
@InstallIn(SingletonComponent::class)
abstract class BoardCatalogModule {

    @Binds
    abstract fun bindBoardCatalogRepo(boardCatalogRepoImpl: BoardCatalogRepoImpl): BoardCatalogRepo

    @Module
    @InstallIn(SingletonComponent::class)
    object WithProvides {

        private const val PREF_FILE_NAME = "st_prefs"

        @Singleton
        @Preferences
        @Provides
        fun providePreferences(@ApplicationContext context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        }
    }
}
