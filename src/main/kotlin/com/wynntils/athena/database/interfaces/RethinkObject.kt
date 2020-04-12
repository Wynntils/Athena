package com.wynntils.athena.database.interfaces

import com.rethinkdb.RethinkDB.r
import com.wynntils.athena.database.DatabaseManager

interface RethinkObject {

    val table: String
    val id: Any

    private fun save(): RethinkObject {
        r.table(table).insert(this).optArg("conflict", "replace").runNoReply(DatabaseManager.connection)
        return this
    }

    private fun delete(): RethinkObject {
        r.table(table).get(id).delete().runNoReply(DatabaseManager.connection)

        return this
    }

    fun asyncSave(): RethinkObject {
        DatabaseManager.executor.submit { save() }

        return this
    }

    fun asyncDelete(): RethinkObject {
        DatabaseManager.executor.submit { delete() }

        return this
    }

}