package com.wynntils.athena.database.objects

import com.fasterxml.jackson.annotation.JsonIgnore
import com.wynntils.athena.core.currentTimeMillis
import com.wynntils.athena.core.enums.AccountType
import com.wynntils.athena.core.utils.ZLibHelper
import com.wynntils.athena.database.data.CosmeticInfo
import com.wynntils.athena.database.data.DiscordInfo
import com.wynntils.athena.database.files.FileCabinet
import com.wynntils.athena.database.files.objects.FileTable
import com.wynntils.athena.database.interfaces.DatabaseObject
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.HashMap

data class Users(
    override val _id: String,

    var username: String= "",
    var lastActivity: Long = currentTimeMillis(),
    var accountType: AccountType = AccountType.NORMAL,

    var authToken: String = UUID.randomUUID().toString(), // the user authentication token
    var password: String = "", // hashed user password

    var latestVersion: String = "", // last version user used
    var usedVersions: HashMap<String, Long> = HashMap(), // all versions + when they were used

    var cosmeticInfo: CosmeticInfo = CosmeticInfo(), // cosmetic stuff
    var discordInfo: DiscordInfo? = DiscordInfo("", ""), // discord stuff

    override val table: String = "users"
): DatabaseObject {

    @JsonIgnore
    private var configFiles: FileTable? = null

    private fun getConfigTable(): FileTable {
        if (configFiles == null) configFiles = FileCabinet.getOrCreateDatabase("userConfigs").getOrCreateTable(_id.toString());

        return configFiles!!
    }

    fun getConfigFiles(): Map<String, String> {
        val configMap = mutableMapOf<String, String>()

        for (name in (getConfigTable().listFiles())) {
            configMap[name] = ZLibHelper.inflate(getConfigTable().getFile(name)!!.retrieveBytes()).toString(StandardCharsets.UTF_8)
        }

        return configMap
    }

    fun getConfigAmount(): Int {
        return getConfigTable().fileAmount()
    }

    fun setConfig(name: String, data: ByteArray) {
        getConfigTable().insertFile(name, data, true)
    }

    fun updateAccount(name: String, version: String): String {
        authToken = UUID.randomUUID().toString()
        lastActivity = currentTimeMillis()
        username = name

        latestVersion = version
        usedVersions[version] = currentTimeMillis()

        asyncSave()

        return authToken
    }

    fun updateDiscord(id: String, username: String) {
        discordInfo = DiscordInfo(id, username)

        asyncSave()
    }

}