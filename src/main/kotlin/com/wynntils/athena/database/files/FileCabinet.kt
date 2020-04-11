package com.wynntils.athena.database.files

import com.wynntils.athena.core.profiler.profile
import com.wynntils.athena.database.files.enums.ActionResult
import com.wynntils.athena.database.files.objects.FileDatabase
import com.wynntils.athena.getDataFolder
import java.io.File

object FileCabinet {

    private val databaseLocation = File(getDataFolder(), "database")
    private var databaseList = HashMap<String, FileDatabase>()

    fun loadDatabases() {
        profile("FileCabinet-Load") {
            if (!databaseLocation.exists()) databaseLocation.mkdirs()

            databaseLocation.listFiles()?.forEach {
                databaseList[it.name] = FileDatabase(it.name, it).load()
            }
        }
    }

    /**
     * Creates a new database and load it
     *
     * @param name
     * @return SUCCESS or ALREADY_EXISTS
     */
    fun createDatabase(name: String): ActionResult {
        if (databaseList.containsKey(name)) return ActionResult.ALREADY_EXISTS

        val folder = File(databaseLocation, name)
        if (!folder.exists()) folder.mkdirs()

        databaseList[name] = FileDatabase(name, folder).load()
        return ActionResult.SUCCESS
    }

    /**
     * Gets a database object if it exists
     * @see FileDatabase
     * @return the FileDatabase object or null
     */
    fun getDatabase(name: String): FileDatabase? {
       return databaseList.getOrDefault(name, null)
    }

    /**
     * Gets or create a file database
     * @see FileDatabase
     * @return the FileDatabase object
     */
    fun getOrCreateDatabase(name: String): FileDatabase {
        if (!hasDatabase(name)) createDatabase(name)

        return getDatabase(name)!!
    }

    /**
     * @return a boolean if the provided database exists
     */
    fun hasDatabase(name: String): Boolean {
        return databaseList.containsKey(name)
    }

}