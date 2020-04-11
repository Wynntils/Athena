package com.wynntils.athena.core.data

data class GameProfile(

    val name: String,
    val id: String,
    val properties: List<Property>

)

data class Property(

    val name: String,
    val value: String

)