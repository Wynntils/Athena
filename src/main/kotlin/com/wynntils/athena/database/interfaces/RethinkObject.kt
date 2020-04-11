package com.wynntils.athena.database.interfaces

import com.rethinkdb.RethinkDB.r
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.gson

interface RethinkObject {

    val table: String
    val id: Any

    private fun save(): RethinkObject {
        val map = gson.fromJson<HashMap<*, *>>(gson.toJson(this), HashMap<Any, Any>().javaClass)
        map.remove("table")

        r.table(table).insert(map).optArg("conflict", "replace").runNoReply(DatabaseManager.connection)
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