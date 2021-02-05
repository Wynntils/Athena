package com.wynntils.athena.routes.managers

import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.enums.Hash
import com.wynntils.athena.core.utils.ExternalNotifications
import com.wynntils.athena.database.files.FileCabinet
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO

object CapeManager {

    val token = UUID.randomUUID().toString().replace("-", "")
    val database = FileCabinet.getOrCreateDatabase("capes")

    private val capeMask = ImageIO.read(javaClass.getResource("cape_mask.png"))
    private val maskPixels = capeMask.getRGB(0, 0, capeMask.width, capeMask.height, null, 0, capeMask.width)

    /**
     * Get's the approved cape image file based on the cape SHA-1
     *
     * @param sha1 the cape identification SHA-1
     * @return an InputStream with the cape data
     */
    fun getCape(sha1: String): InputStream {
        val table = database.getOrCreateTable("approved")

        return table.getFile(sha1)?.asInputStream() ?: table.getFile("defaultCape")!!.asInputStream()
    }

    /**
     * @return if the provided sha-1 is registered
     */
    fun hasCape(sha1: String): Boolean {
        return database.getOrCreateTable("approved").hasFile(sha1)
    }

    /**
     * Tries to delete the provided cape
     * @return if the cape was deleted successfully
     */
    fun deleteCape(sha1: String): Boolean {
        if (!hasCape(sha1)) return false

        database.getOrCreateTable("approved").deleteFile(sha1)
        return true
    }

    /**
     * Get's the approved cape image base64 based on the cape SHA-1
     *
     * @param sha1 the cape identification SHA-1
     * @return a Base64 string with the cape data
     */
    fun getCapeAsBase64(sha1: String): String? {
        val table = database.getOrCreateTable("approved")

        return table.getFile(sha1)?.asBase64()
    }

    /**
     * List all approved capes
     *
     * @return a HashSet containing all approved capes SHA-1
     */
    fun listCapes(): HashSet<String> {
        return database.getOrCreateTable("approved").listFiles()
    }

    /**
     * Get's the queued cape image file based on the cape SHA-1
     *
     * @param sha1 the cape identification SHA-1
     * @return an InputStream with the cape data
     */
    fun getQueuedCape(sha1: String): InputStream {
        val table = database.getOrCreateTable("queue")

        return table.getFile(sha1)?.asInputStream() ?: getCape("defaultCape")
    }

    /**
     * Insert a cape to the analyse process
     *
     * @param data the cape byte array
     */
    fun queueCape(data: ByteArray) {
        val sha1 = Hash.SHA1.hash(data)

        val table = database.getOrCreateTable("queue")
        table.insertFile(sha1, data, false)

        // send webhook confirmation
        ExternalNotifications.sendMessage(
            title = "A new cape needs approval!",
            description = "➡️ **Choose:** [Approve](${generalConfig.baseUrl}capes/queue/approve/$token/$sha1) " +
                    "or [Ban](${generalConfig.baseUrl}capes/queue/ban/$token/$sha1)\n**SHA-1:** $sha1",
            color = 16776960,
            imageUrl = "${generalConfig.baseUrl}capes/queue/get/$sha1"
        )
    }

    /**
     * Approves a cape based on it SHA-1
     *
     * @param sha1 the cape identification SHA-1
     */
    fun approveCape(sha1: String) {
        if (!isQueued(sha1)) return

        val approvedTable = database.getOrCreateTable("approved")
        val queueTable = database.getOrCreateTable("queue")

        queueTable.getFile(sha1)!!.moveTo(approvedTable)

        // queue message
        ExternalNotifications.sendMessage(
            title = "A cape was approved",
            description = "➡️ **SHA-1**: $sha1",
            color = 65280
        )
    }

    /**
     * Bans a cape based on it SHA-1
     *
     * @param sha1 the cape identification SHA-1
     */
    fun banCape(sha1: String) {
        if (!isQueued(sha1)) return

        val bannedTable = database.getOrCreateTable("banned")
        val queueTable = database.getOrCreateTable("queue")

        queueTable.getFile(sha1)!!.moveTo(bannedTable)

        // queue message
        ExternalNotifications.sendMessage(
            title = "A cape was banned",
            description = "➡️ **SHA-1**: $sha1",
            color = 16711680
        )
    }

    /**
     * @parama sha1 the SHA-1 cape identification
     * @return if the provided cape is approved
     */
    fun isApproved(sha1: String): Boolean {
        val table = database.getOrCreateTable("approved")

        return table.hasFile(sha1)
    }

    /**
     * @parama sha1 the SHA-1 cape identification
     * @return if the provided cape is banned
     */
    fun isBanned(sha1: String): Boolean {
        val table = database.getOrCreateTable("banned")

        return table.hasFile(sha1)
    }

    /**
     * @parama sha1 the SHA-1 cape identification
     * @return if the provided cape is queued
     */
    fun isQueued(sha1: String): Boolean {
        val table = database.getOrCreateTable("queue")

        return table.hasFile(sha1)
    }

    /**
     * Masks an image, only where the mask contains white will the image contain visible pixels.
     * This operation occurs inline
     */
    fun maskCape(cape: BufferedImage) {
        val scale = cape.width / capeMask.width
        val capePixels = cape.getRGB(0, 0, cape.width, cape.height, null, 0, cape.width)

        for (y in 0 until cape.height) {
            for (x in 0 until cape.width) {
                capePixels[x + y * cape.width] = capePixels[x + y * cape.width] and maskPixels[((x / scale) + ((y / scale) * capeMask.width)) % maskPixels.size]
            }
        }

        cape.setRGB(0, 0, cape.width, cape.height, capePixels, 0, cape.width)
    }
}