package com.wynntils.athena.database

import com.github.benmanes.caffeine.cache.Caffeine
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

    // caches
    private val userProfiles = Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build<UUID, UserProfile>()
    private val reverseLookup = Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build<String, UUID>()

    fun getUserProfile(id: UUID, create: Boolean = true): UserProfile? {
        return userProfiles.get(id) {
            val requestResult = r.table("users").get(id.toString()).run(connection, UserProfile::class.java)
            getResult(requestResult).firstOrNull() ?: if (create) UserProfile(id) else null
        }
    }

    fun getUserProfile(token: String): UserProfile? {
        return reverseLookup.get(token) {
            val requestResult = r.table("users").getAll(token).optArg("index", "authToken")
                .limit(1).run(connection, UserProfile::class.java)

            val result = getResult(requestResult).firstOrNull() ?: return@get null

            userProfiles.put(result.id, result)
            return@get result.id
        }?.let { userProfiles.getIfPresent(it) }
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