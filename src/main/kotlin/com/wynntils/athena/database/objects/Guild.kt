package com.wynntils.athena.database.objects

import com.wynntils.athena.database.interfaces.DatabaseObject

data class Guild(
    override val _id: String,

    var prefix: String = "",
    var color: String = "",

    override val table: String = "guild"
): DatabaseObject