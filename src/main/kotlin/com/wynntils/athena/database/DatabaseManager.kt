package com.wynntils.athena.database

import com.rethinkdb.RethinkDB.r
import com.rethinkdb.net.Result
import com.wynntils.athena.core.configs.databaseConfig
import com.wynntils.athena.core.data.Location
import com.wynntils.athena.database.objects.ApiKeyProfile
import com.wynntils.athena.database.objects.GatheringSpotProfile
import com.wynntils.athena.database.objects.GuildProfile
import com.wynntils.athena.database.objects.UserProfile
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
        return getResult(requestResult).firstOrNull() ?: if (create) UserProfile(id) else null
    }

    fun getUserProfile(token: String): UserProfile? {
        val requestResult = r.table("users").filter(r.hashMap("authToken", token))
            .limit(1).run(connection, UserProfile::class.java)

        return getResult(requestResult).firstOrNull()
    }

    fun getUsersProfiles(name: String): List<UserProfile> {
        val requestResult = r.table("users").filter(r.hashMap("username", name)).run(connection, UserProfile::class.java)
        return getResult(requestResult)
    }

    fun getGuildProfile(name: String): GuildProfile? {
        val requestResult = r.table("guilds").get(name).run(connection, GuildProfile::class.java)
        return getResult(requestResult).firstOrNull()
    }

    fun getGatheringSpot(location: Location): GatheringSpotProfile? {
        val requestResult = r.table("gathering").get(location.toString()).run(connection, GatheringSpotProfile::class.java)
        return getResult(requestResult).firstOrNull()
    }

    fun getAllGatheringSpots(): List<GatheringSpotProfile> {
        val requestResult = r.table("gathering").run(connection, GatheringSpotProfile::class.java)
        return getResult(requestResult)
    }

    fun getApiKey(apiKey: String): ApiKeyProfile? {
        val requestResult = r.table("apiKeys").get(apiKey).run(connection, ApiKeyProfile::class.java)
        return getResult(requestResult).firstOrNull()
    }

    private fun <T: Any> getResult(result: Result<T>): List<T> {
        val resultArray = ArrayList<T>()

        try {
            while (result.hasNext()) {
                result.next(10, TimeUnit.SECONDS)?.let { resultArray.add(it) }
            }
        } catch (ignored: Exception) { }

        return resultArray
    }

}