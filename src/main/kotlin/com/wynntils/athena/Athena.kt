package com.wynntils.athena

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.rethinkdb.RethinkDB
import com.wynntils.athena.core.cache.CacheManager
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.enums.AsciiColor
import com.wynntils.athena.core.printCoolLogo
import com.wynntils.athena.core.profiler.getSectionMs
import com.wynntils.athena.core.routes.registerRoutes
import com.wynntils.athena.core.routes.setupExceptions
import com.wynntils.athena.core.utils.ExternalNotifications
import com.wynntils.athena.core.utils.Logger
import com.wynntils.athena.database.files.FileCabinet
import com.wynntils.athena.routes.*
import com.wynntils.athena.routes.caches.*
import io.javalin.Javalin
import java.io.File

lateinit var server: Javalin

val mapper = ObjectMapper()
    .registerModule(KotlinModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

val generalLog = Logger("general")
val errorLog = Logger("error")

val cacheDatabase = FileCabinet.getOrCreateDatabase("caches")
val templateDatabase = FileCabinet.getOrCreateDatabase("templates")

private fun main() {
    RethinkDB.setResultMapper(mapper) // updates jackson mapper to support kotlin data classes

    printCoolLogo()

    FileCabinet.loadDatabases()
    generalLog.info("File Database Loaded in " + getSectionMs("FileCabinet-Load") + "ms!")

    generalLog.info("WebServer Starting...")
    server = Javalin.create {
        it.defaultContentType = "application/json"
        it.showJavalinBanner = false
    }.start(generalConfig.port)
    server.setupExceptions()

    generalLog.info("WebServer Started, Registering Routes...")

    // routes
    server.registerRoutes(AuthenticationRoutes::class)
    server.registerRoutes(IORoutes::class)
    server.registerRoutes(CapeRoutes::class)
    server.registerRoutes(ApiRoutes::class)
    server.registerRoutes(UserRoutes::class)
    server.registerRoutes(CacheRoutes::class)
    server.registerRoutes(TelemetryRoutes::class)

    generalLog.info("Starting data caches refresh...")
    // caches
    CacheManager.refreshCache(MapLocationsCache())
    CacheManager.refreshCache(LeaderboardCache())
    CacheManager.refreshCache(TerritoryListCache())
    CacheManager.refreshCache(ServerListCache())
    CacheManager.refreshCache(GatheringSpotsCache())
    CacheManager.refreshCache(ItemListCache())
    CacheManager.refreshCache(LeaderboardCache())

    generalLog.info(AsciiColor.GREEN + "All done, startup sequence completed!")
    ExternalNotifications.sendMessage(description = "Athena is online!", color = 65280)
}

fun getDataFolder(): File {
    return File(System.getProperty("user.dir"), "data")
}