package com.wynntils.athena.database

import com.rethinkdb.RethinkDB.r
import com.wynntils.athena.core.configs.databaseConfig
import com.wynntils.athena.database.objects.GuildProfile
import com.wynntils.athena.database.objects.UserProfile
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object DatabaseManager {

    val connection = r.connection()
        .hostname(databaseConfig.ip)
        .port(databaseConfig.port)
        .db(databaseConfig.database)
        .user(databaseConfig.username, databaseConfig.password)
        .connect()

    val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun getUserProfile(id: UUID, create: Boolean = true): UserProfile? {
        val requestResult = r.table("users").get(id.toString()).run(connection, UserProfile::class.java)
        if (!requestResult.hasNext()) return if (create) UserProfile(id) else null

        return requestResult.first() ?: if (create) UserProfile(id) else null
    }

    fun getUserProfile(token: String): UserProfile? {
        val requestResult = r.table("users").filter(r.hashMap("authToken", token))
            .limit(1).run(connection, UserProfile::class.java)
        if (!requestResult.hasNext()) return null

        return requestResult.first()
    }

    fun getUsersProfiles(name: String): List<UserProfile> {
        val result = ArrayList<UserProfile>()
        val requestResult = r.table("users").filter(r.hashMap("username", name)).run(connection, UserProfile::class.java)

        while (requestResult.hasNext()) {
            result += requestResult.next()!!
        }

        return result
    }

    fun getGuildProfile(name: String): GuildProfile? {
        val requestResult = r.table("guilds").get(name).run(connection, GuildProfile::class.java)
        if (!requestResult.hasNext()) return null

        return requestResult.first()
    }

}