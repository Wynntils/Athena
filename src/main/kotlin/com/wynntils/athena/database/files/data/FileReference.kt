package com.wynntils.athena.database.files.data

import com.wynntils.athena.core.enums.Hash
import com.wynntils.athena.core.profiler.profile
import com.wynntils.athena.database.files.enums.ActionResult
import com.wynntils.athena.database.files.objects.FileTable
import com.wynntils.athena.mapper
import org.json.simple.JSONAware
import java.awt.image.BufferedImage
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream

data class FileReference(
    val file: File,
    val origin: FileTable
) {

    private var bytes: ByteArray? = null

    /**
     * Gets and stores on memory the file bytes
     *
     * @return a ByteArray with the file bytes
     */
    fun retrieveBytes(): ByteArray {
        if (bytes == null) bytes = file.readBytes()

        return bytes!!
    }

    /**
     * Generates a hash for the file based on the provided hash type
     *
     * @param hash the hash type
     * @return the hashed string of the file
     */
    fun getHash(hash: Hash): String {
        return hash.hash(retrieveBytes())
    }

    /**
     * Writes the provided ByteArray to the file
     *
     * @param data the data you want to write
     */
    fun write(data: ByteArray) {
        profile("FileCabinet-FileTable-FileReference-WriteBytes") { // performance profiling
            val writer = FileOutputStream(file)
            writer.write(data)
            writer.close()
        }
    }

    /**
     * Writes the provided String to the file
     *
     * @param data the data you want to write
     */
    fun write(data: String) {
        profile("FileCabinet-FileTable-FileReference-WriteString") {
            val writer = FileWriter(file)
            writer.write(data)
            writer.close()
        }
    }

    /**
     * Append a new string to the file
     *
     * @param data the data you want to append
     */
    fun append(data: String) {
        Files.write(file.toPath(), data.toByteArray(StandardCharsets.UTF_8), StandardOpenOption.APPEND)
    }

    /**
     * Copy the file to the provided table and delete the origin
     * FileReference may be released after this!
     *
     * @param destination the destination table
     * @return SUCCESS, ALREADY_EXISTS or INVALID_FILE
     */
    fun moveTo(destination: FileTable): ActionResult {
        if (copyTo(destination) == ActionResult.ALREADY_EXISTS)
            return ActionResult.ALREADY_EXISTS

        return origin.deleteFile(file.name)
    }

    /**
     * Copy the file to the provided table
     *
     * @param destination the destination table
     * @return SUCCESS or ALREADY_EXISTS
     */
    fun copyTo(destination: FileTable): ActionResult {
        return destination.insertFile(file.name, retrieveBytes())
    }

    /**
     * Converts the file bytes into an image object
     * @return a BufferedImage equivalent of the file
     */
    fun asImage(): BufferedImage? {
        return ImageIO.read(file)
    }

    /**
     * Converts the file input stream to an image input stream
     * @return the image input stream
     */
    fun asImageStream(): ImageInputStream {
        return ImageIO.createImageInputStream(file)
    }

    /**
     * Gets the file InputStream
     * @return the file InputStream
     */
    fun asInputStream(): InputStream {
        return FileInputStream(file)
    }

    /**
     * Converts the file bytes into an JsonElement of T
     * @return a JsonElement equivalent to T
     */
    inline fun <reified T : JSONAware> asJson(): T {
        return mapper.readValue(retrieveBytes(), T::class.java)
    }

    /**
     * Converts the file bytes into a Base64 string
     * @return a String containing the Base64
     */
    fun asBase64(): String {
        return Base64.getEncoder().encodeToString(retrieveBytes())
    }

    /**
     * Converts the file bytes into text
     * @return a string containing all the file text
     */
    fun asString(): String {
        return retrieveBytes().toString(StandardCharsets.UTF_8)
    }

}