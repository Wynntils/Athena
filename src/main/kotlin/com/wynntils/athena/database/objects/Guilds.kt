package com.wynntils.athena.database.objects

import com.wynntils.athena.database.interfaces.DatabaseObject

data class Guilds(
    override val _id: String,

    var prefix: String = "",
    var color: String = "",

    override val table: String = "guilds"
): DatabaseObject