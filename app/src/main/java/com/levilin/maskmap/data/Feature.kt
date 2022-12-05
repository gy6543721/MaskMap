package com.levilin.maskmap.data

import java.io.Serializable

data class Feature(
    val geometry: Geometry,
    val properties: Properties,
    val type: String
): Serializable