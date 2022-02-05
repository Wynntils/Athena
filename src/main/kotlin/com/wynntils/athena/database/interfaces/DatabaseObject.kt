package com.wynntils.athena.database.interfaces

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.wynntils.athena.database.DatabaseManager
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.save

@JsonIgnoreProperties(value = ["table"])
interface DatabaseObject {

    val table: String
    val _id: Any

    private fun save() = DatabaseManager.db.getCollection<DatabaseObject>(table).save(this)

    private fun delete() = DatabaseManager.db.getCollection<DatabaseObject>(table).deleteOneById(_id)

    fun asyncSave() = DatabaseManager.executor.submit { save() }

    fun asyncDelete() = DatabaseManager.executor.submit { delete() }

}