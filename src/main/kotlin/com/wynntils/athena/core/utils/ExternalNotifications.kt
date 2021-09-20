package com.wynntils.athena.core.utils

import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.configs.webHookConfig
import com.wynntils.athena.core.runAsync
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets

object ExternalNotifications {

    fun sendMessage(title: String? = null, description: String? = null, color: Int? = null, imageUrl: String? = null, footer: String? = null) {
        sendMessage(webHookConfig.discordLogUrl, title, description, color, imageUrl, footer)
    }

    fun sendCapeMessage(title: String? = null, description: String? = null, color: Int? = null, imageUrl: String? = null, footer: String? = null) {
        sendMessage(webHookConfig.discordCapesUrl, title, description, color, imageUrl, footer)
    }

    private fun sendMessage(url: String, title: String? = null, description: String? = null, color: Int? = null, imageUrl: String? = null, footer: String? = null) {
        runAsync {
            val body = JSONObject()
            body["username"] = webHookConfig.discordUsername
            body["avatar_url"] = webHookConfig.discordAvatar

            val embeds = JSONArray()

            val embed = JSONObject()
            embed["title"] = title
            embed["description"] = description
            embed["color"] = color

            if (imageUrl != null) {
                val imageObject = JSONObject()
                imageObject["url"] = imageUrl
                embed["image"] = imageObject
            }

            if (footer != null) {
                val footerObject = JSONObject()
                footerObject["text"] = footer
                embed["footer"] = footerObject
            }

            embeds.add(embed)
            body["embeds"] = embeds

            makeRequest(url, body.toString().toByteArray(StandardCharsets.UTF_8))
        }

        runAsync {
            val body = JSONObject()
            body["chat_id"] = webHookConfig.telegramChannel
            body["text"] = "*${title?:""}*\n${description?:""}\n${imageUrl?:""}"
            body["parse_mode"] = "markdown"

            makeRequest(webHookConfig.telegramUrl.format(webHookConfig.telegramKey), body.toString().toByteArray(StandardCharsets.UTF_8))
        }
    }

    private fun makeRequest(url: String, body: ByteArray) {
        val con = URL(url).openConnection()
        con.setRequestProperty("User-Agent", generalConfig.userAgent)
        con.setRequestProperty("Content-Length", "" + body.size)
        con.setRequestProperty("Content-Type", "application/json")
        con.doOutput = true

        val outputStream = con.getOutputStream()
        outputStream.write(body)
        outputStream.close()

        con.getInputStream().close() // close the input to validate the request
    }

}