package com.st.blue_sdk.features.extended.scene_description.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SceneDescriptorData (
//    val objects: List<Object>?,
//    val obstacles: List<Obstacle>?,
//    val barcodes: List<Rcode>?,
//    val qrcodes: List<Rcode>?,

    @SerialName("tof_zones")
    val tofZones: List<List<Long>>?
)

@Serializable
data class Rcode (
    @SerialName("val")
    val rcodeVal: String,

    val bb: List<Long>
)

@Serializable
data class Object (
    val id: String,
    val confidence: Double,
    val bb: List<Long>
)

@Serializable
data class Obstacle (
    val edge: Long? = null,
    val cliff: Long? = null,
    val objects: List<Long>? = null
)
