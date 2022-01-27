package com.wynntils.athena.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoDatabase
import com.wynntils.athena.core.configs.databaseConfig
import com.wynntils.athena.core.data.Location
import com.wynntils.athena.database.objects.ApiKey
import com.wynntils.athena.database.objects.GatheringSpot
import com.wynntils.athena.database.objects.Guilds
import com.wynntils.athena.database.objects.Users
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

    fun getUserProfile(id: UUID, create: Boolean = true): Users? {
        return db.getCollection<Users>().findOneById(id) ?: if (create) Users(id.toString()) else null
    }

    fun getUserProfile(token: String): Users? {
        return db.getCollection<Users>().findOne(Users::authToken eq token)
    }

    fun getUsersProfiles(name: String): List<Users> {
        return db.getCollection<Users>().find(Users::username eq name).toList()
    }

    fun getGuildProfile(name: String?): Guilds? {
        if (name == null) return null

        return db.getCollection<Guilds>().findOneById(name)
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