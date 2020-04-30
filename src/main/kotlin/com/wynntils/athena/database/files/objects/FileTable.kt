package com.wynntils.athena.database.files.objects

import com.wynntils.athena.core.profiler.profile
import com.wynntils.athena.database.files.data.FileReference
import com.wynntils.athena.database.files.enums.ActionResult
import java.io.File
import java.io.FileOutputStream

data class FileTable(

    val name: String,
    val path: File,

    private var files: HashSet<String>,
    private var loaded: Boolean = false

) {

    fun load() {
        if (loaded) return
        val files = path.list() ?: return

        profile("FileCabinet-FileTable-Load-${this.name.replace("-", "")}") { // performance profiling
            files.forEach { this.files.add(it) }
        }

        loaded = true
    }

    /**
     * Gets a file if it's exists
     *
     * @return the file or null if it doesn't exists
     */
    fun getFile(name: String): FileReference? {
        if (!files.contains(name)) return null

        return profile("FileCabinet-FileTable-Get-${this.name.replace("-", "")}") {
            FileReference(File(path, name), this)
        }
    }

    /**
     * Get or create a file if it doesn't exists
     *
     * @return the file reference
     */
    fun getOrCreateFile(name: String): FileReference {
        if (!files.contains(name)) insertFile(name)

        return getFile(name)!!
    }

    /**
     * Insert a file into the table
     * Writing can be done later via FileReference or in the method
     * @see FileReference
     *
     * @param name the file name
     * @param data optional | the file data in bytes
     * @param overlap optional | if it should override if exists
     * @return SUCCESS or ALREADY_EXISTS
     */
    fun insertFile(name: String, data: ByteArray? = null, overlap: Boolean = false): ActionResult {
        if (files.contains(name) && !overlap) return ActionResult.ALREADY_EXISTS
        files.add(name) // adds to the name cache

        profile("FileCabinet-FileTable-Insert-${this.name.replace("-", "")}") { // performance profiling
            if (data == null) return@profile

            val file = File(path, name)
            file.createNewFile()

            val writer = FileOutputStream(file)
            writer.write(data)
            writer.close()
        }

        return ActionResult.SUCCESS
    }

    /**
     * Deletes a file from the table
     *
     * @param name the file name
     * @return SUCCESS or INVALID_FILE
     */
    fun deleteFile(name: String): ActionResult {
        if (!files.contains(name)) return ActionResult.INVALID_FILE
        files.remove(name)

        File(path, name).delete() // wait if the file is being used somewhere
        return ActionResult.SUCCESS
    }

    /**
     * Returns if there's the provided file in the table
     *
     * @return if the file exists in the table
     */
    fun hasFile(name: String): Boolean {
        return files.contains(name)
    }

    /**
     * @return a list containing all files **names**
     */
    fun listFiles(): HashSet<String> {
        return files
    }

    /**
     * @return the amount of available files in the table
     */
    fun fileAmount(): Int {
        return files.size
    }

    /**
     * Warning: This might be a resource intensive task if a lot of files are present
     * @return the total disk side occupied by this table in bytes
     */
    fun totalSize(): Long {
        var sizeSum = 0L

        profile("FileCabinet-FileTable-TotalSize-${this.name.replace("-", "")}") { // performance profiling
            files.map { c -> getFile(c) }.forEach {
                sizeSum += it?.file?.length() ?: 0
            }
        }

        return sizeSum
    }

}