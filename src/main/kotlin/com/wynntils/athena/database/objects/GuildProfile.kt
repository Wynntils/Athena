package com.wynntils.athena.database.objects

import com.wynntils.athena.database.interfaces.RethinkObject

data class GuildProfile(
    override val id: String,

    var prefix: String = "",
    var color: String = "",

    override val table: String = "guilds"
): RethinkObject