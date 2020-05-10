package com.wynntils.athena.routes.managers

import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.enums.Hash
import com.wynntils.athena.core.utils.ExternalNotifications
import com.wynntils.athena.database.files.FileCabinet
import java.io.InputStream
import java.util.*

object CapeManager {

    val token = UUID.randomUUID().toString().replace("-", "")
    val database = FileCabinet.getOrCreateDatabase("capes")

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

}