@file:UseSerializers(BoardFotaTypeSerializer::class, BootLoaderTypeSerializer::class)

package com.st.blue_sdk.board_catalog.models


import com.st.blue_sdk.board_catalog.api.serializers.BoardFotaTypeSerializer
import com.st.blue_sdk.board_catalog.api.serializers.BootLoaderTypeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class FotaDetails(
    @SerialName("partial_fota")
    val partialFota: Int? = 0,
    @SerialName("type")
    val type: BoardFotaType? = BoardFotaType.NO,
    @SerialName("max_chunk_length")
    var maxChunkLength: Int? = 0,
    @SerialName("max_divisor_constraint")
    var maxDivisorConstraint: Int? = 0,
    @SerialName("fw_url")
    val fwUrl: String? = null,
    @SerialName("bootloader_type")
    val bootloaderType: BootLoaderType? = BootLoaderType.NONE,
)
