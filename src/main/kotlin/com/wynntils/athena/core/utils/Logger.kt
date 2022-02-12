package com.wynntils.athena.core.utils

import com.wynntils.athena.core.enums.AsciiColor
import com.wynntils.athena.core.fileDateFormat
import com.wynntils.athena.core.runAsync
import com.wynntils.athena.core.textDateFormat
import com.wynntils.athena.core.toPlainString
import com.wynntils.athena.database.files.FileCabinet
import com.wynntils.athena.database.files.objects.FileTable
import java.util.*
import java.util.concurrent.PriorityBlockingQueue

class Logger(

    val name: String,
    private val print: Boolean = true

) {

    private val table: FileTable
    private val fileName: String

    private val writeQueue = PriorityBlockingQueue<String>()

    init { // overall database setup
        if (!FileCabinet.hasDatabase("logs")) FileCabinet.createDatabase("logs")
        val db = FileCabinet.getDatabase("logs")!!

        fileName = "${name}_${fileDateFormat.format(Date())}.txt"

        if (!db.hasTable(name)) db.createTable(name)
        table = db.getTable(name)!!
    }

    private fun writeString(text: String) {
        writeQueue.offer(text)

        fun processQueue() {
            runAsync {
                if (writeQueue.isEmpty()) return@runAsync
                val file = table.getOrCreateFile(fileName)

                file.append(writeQueue.poll() + "\n")

                processQueue() // loop it back here
            }
        }

        processQueue()
    }

    fun info(message: String) {
        val finalMessage =
            "[INFO] [${textDateFormat.format(Date())}] " + AsciiColor.BLUE.ascii + message + AsciiColor.RESET.ascii
        if (print) println(finalMessage)

        writeString(AsciiColor.removeColors(finalMessage))
    }

    fun warn(message: String) {
        val finalMessage =
            "[WARN] [${textDateFormat.format(Date())}] " + AsciiColor.RED.ascii + message + AsciiColor.RESET.ascii
        if (print) println(finalMessage)

        writeString(AsciiColor.removeColors(finalMessage))
    }

    fun exception(message: String, ex: Exception) {
        ExternalNotifications.sendMessage(
            description = message,
            color = 16711680
        )

        warn("$message:\n" + ex.toPlainString())
    }

    fun debug(message: String) {
        val finalMessage =
            "[DEBUG] [${textDateFormat.format(Date())}] " + AsciiColor.YELLOW.ascii + message + AsciiColor.RESET.ascii
        if (print) println(finalMessage)

        writeString(AsciiColor.removeColors(finalMessage))
    }

}