package com.wynntils.athena.database

import com.rethinkdb.RethinkDB.r
import com.rethinkdb.net.Cursor
import com.wynntils.athena.core.configs.databaseConfig
import com.wynntils.athena.database.objects.GuildProfile
import com.wynntils.athena.database.objects.UserProfile
import com.wynntils.athena.gson
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object DatabaseManager {

    val connection = r.connection()
        .hostname(databaseConfig.ip)
        .port(databaseConfig.port)
        .db(databaseConfig.database)
        .user(databaseConfig.username, databaseConfig.password)
        .connect()!!

    val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun getUserProfile(id: UUID, create: Boolean = true): UserProfile? {
        val requestResult = r.table("users").get(id.toString()).run<HashMap<*, *>>(connection)
        if (requestResult != null) return gson.fromJson(gson.toJsonTree(requestResult), UserProfile::class.java)

        return if (create) UserProfile(id) else null
    }

    fun getUserProfile(token: String): UserProfile? {
        val requestResult = r.table("users").filter(r.hashMap("authToken", token))
            .limit(1).run<Cursor<HashMap<*, *>>>(connection)
        if (!requestResult.hasNext()) return null

        return gson.fromJson(gson.toJsonTree(requestResult.next()), UserProfile::class.java)
    }

    fun getUsersProfiles(name: String): List<UserProfile> {
        val result = ArrayList<UserProfile>()
        val requestResult = r.table("users").filter(r.hashMap("username", name)).run<Cursor<HashMap<*, *>>>(connection)

        while (requestResult.hasNext()) {
            result += gson.fromJson(gson.toJsonTree(requestResult.next()), UserProfile::class.java)
        }

        return result
    }

    fun getGuildProfile(name: String): GuildProfile? {
        val requestResult = r.table("guilds").get(name)
            .run<HashMap<*, *>>(connection) ?: return null

        return gson.fromJson(gson.toJsonTree(requestResult), GuildProfile::class.java)
    }

}