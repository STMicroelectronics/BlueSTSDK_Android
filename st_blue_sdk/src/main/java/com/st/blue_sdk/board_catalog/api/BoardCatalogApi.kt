/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.api

import com.st.blue_sdk.board_catalog.models.BoardCatalog
import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET
import retrofit2.http.Url

interface BoardCatalogApi {

    //For Production Catalog
    @GET("catalog.json")
    suspend fun getFirmwares(): BoardCatalog

    @GET("chksum.json")
    suspend fun getDBVersion(): BoardCatalog

    //For Pre-Production Catalog
    @GET
    suspend fun getFirmwaresFromUrl(@Url url: String): BoardCatalog

    @GET
    suspend fun getDBVersionFromUrl(@Url url: String): BoardCatalog

    @GET
    suspend fun getDtmiModel(@Url url: String): List<JsonObject>
}