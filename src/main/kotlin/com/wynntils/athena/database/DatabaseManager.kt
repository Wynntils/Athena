package com.wynntils.athena.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoDatabase
import com.wynntils.athena.core.configs.databaseConfig
import com.wynntils.athena.core.data.Location
import com.wynntils.athena.database.objects.ApiKey
import com.wynntils.athena.database.objects.GatheringSpot
import com.wynntils.athena.database.objects.Guild
import com.wynntils.athena.database.objects.User
import org.bson.UuidRepresentation
import org.litote.kmongo.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object DatabaseManager {

    private val client = KMongo.createClient(
        MongoClientSettings.builder()
            .applyConnectionString(ConnectionString("mongodb://${databaseConfig.username}:${databaseConfig.password}@${databaseConfig.ip}:${databaseConfig.port}"))
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .build()
    )
    val db: MongoDatabase = client.getDatabase(databaseConfig.database);

    val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun getUserProfile(id: UUID, create: Boolean = true): User? {
        return db.getCollection<User>().findOneById(id) ?: if (create) User(id) else null
    }

    fun getUserProfile(token: String): User? {
        return db.getCollection<User>().findOne(User::authToken eq token)
    }

    fun getUsersProfiles(name: String): List<User> {
        return db.getCollection<User>().find(User::username eq name).toList()
    }

    fun getGuildProfile(name: String?): Guild? {
        if (name == null) return null

        return db.getCollection<Guild>().findOneById(name)
    }

    fun getGatheringSpot(location: Location): GatheringSpot? {
        return db.getCollection<GatheringSpot>().findOneById(location)
    }

    fun getAllGatheringSpots(): List<GatheringSpot> {
        return db.getCollection<GatheringSpot>().find().toList()
    }

    fun getApiKey(apiKey: String): ApiKey? {
        return db.getCollection<ApiKey>().findOneById(apiKey)
    }
}