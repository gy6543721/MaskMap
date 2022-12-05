package com.levilin.maskmap.data

import java.io.Serializable

data class MaskInfo(
    val features: List<Feature>,
    val type: String
): Serializable