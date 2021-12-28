package com.wynntils.athena.database.interfaces

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.wynntils.athena.database.DatabaseManager
import org.litote.kmongo.Id
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.getCollection

@JsonIgnoreProperties(value = [ "table" ])
interface DatabaseObject {

    val table: String
    val _id: Any

    private fun save(): DatabaseObject{
//        r.table(table).insert(this).optArg("conflict", "replace").runNoReply(DatabaseManager.connection)
        DatabaseManager.db.getCollection<DatabaseObject>(table).insertOne(this)

        return this
    }

    private fun delete(): DatabaseObject {
        DatabaseManager.db.getCollection<DatabaseObject>(table)
            .deleteOneById(_id)

        return this
    }

    fun asyncSave(): DatabaseObject {
        DatabaseManager.executor.submit { save() }

        return this
    }

    fun asyncDelete(): DatabaseObject {
        DatabaseManager.executor.submit { delete() }

        return this
    }

}