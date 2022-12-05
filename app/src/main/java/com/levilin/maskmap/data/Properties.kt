package com.levilin.maskmap.data

import java.io.Serializable

data class Properties(
    val address: String,
    val available: String,
    val county: String,
    val cunli: String,
    val custom_note: String,
    val id: String,
    val mask_adult: Int,
    val mask_child: Int,
    val name: String,
    val note: String,
    val phone: String,
    val service_periods: String,
    val town: String,
    val updated: String,
    val website: String
): Serializable