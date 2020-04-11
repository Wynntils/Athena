package com.wynntils.athena.database.files.objects

import com.wynntils.athena.core.profiler.profile
import com.wynntils.athena.database.files.data.FileReference
import com.wynntils.athena.database.files.enums.ActionResult
import java.io.File

data class FileDatabase(

    val name: String,
    val path: File,

    private val tables: HashMap<String, FileTable> = HashMap(),
    private var loaded: Boolean = false

) {

    fun load(): FileDatabase {
        if (loaded) return this
        val folders = path.list() ?: return this

        profile("FileCabinet-FileDatabase-Load-$name") { // performance profiling
            folders.forEach { createTable(it) }
        }

        loaded = true
        return this
    }

    /**
     * Creates a new table
     *
     * @param name the table name
     * @return SUCCESS or ALREADY_EXISTS
     */
    fun createTable(name: String): ActionResult {
        if (tables.containsKey(name)) return ActionResult.ALREADY_EXISTS

        val folder = File(path, name)
        folder.mkdirs()

        val table = FileTable(name, folder, folder.list()?.asList() ?: ArrayList())
        table.load()

        tables[name] = table
        return ActionResult.SUCCESS
    }

    /**
     * @return a set containing all table names
     */
    fun getTableSet(): Set<String> {
        return tables.keys
    }

    /**
     * Gets, if possible, the provided table object
     * @see FileTable
     *
     * @return the FileTable object or null if not present
     */
    fun getTable(name: String): FileTable? {
        return tables.getOrDefault(name, null)
    }

    /**
     * Gets or create a table if it does not exists
     * @see FileTable
     *
     * @return the FileTable object
     */
    fun getOrCreateTable(name: String): FileTable {
        if (!hasTable(name)) createTable(name)

        return getTable(name)!!
    }

    /**
     * @return a boolean if the table exists in the current database
     */
    fun hasTable(name: String): Boolean {
        return tables.containsKey(name)
    }

    /**
     * @return the amount of available tables
     */
    fun tableSize(): Int {
        return tables.size
    }

    /**
     * Find all the files matching the specified name
     *
     * @param name matching name
     * @return a list containing all files that matches
     */
    fun findFiles(name: String, limit: Int = 10): List<FileReference> {
        val finalList = ArrayList<FileReference>()

        profile("FileCabinet-FileDatabase-FindFiles-${this.name}-$name") { // performance profiling
            var found = 0
            for (table in tables.values) {
                if (found >= limit || !table.hasFile(name)) continue

                found++
                finalList.add(table.getFile(name)!!)
            }
        }

        return finalList
    }

    /**
     * Warning: This might be a resource intensive task if a lot of tables and files are present
     * @return the total disk side occupied by this database in bytes
     */
    fun totalSize(): Long {
        var sizeSum = 0L

        profile("FileCabinet-FileDatabase-TotalSize-$name") { // performance profiling
            tables.values.forEach {
                sizeSum += it.totalSize()
            }
        }

        return sizeSum
    }

}