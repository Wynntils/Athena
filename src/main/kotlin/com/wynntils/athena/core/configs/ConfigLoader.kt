package com.wynntils.athena.core.configs

import com.wynntils.athena.core.configs.annotations.Settings
import com.wynntils.athena.core.configs.instances.*
import com.wynntils.athena.getDataFolder
import com.wynntils.athena.mapper
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

private val configFolder = File(getDataFolder(), "configs")

// config loading (these are on the global scope, so they're available everywhere)
val generalConfig: GeneralConfig = loadConfig(GeneralConfig::class)!!
val databaseConfig: DatabaseConfig = loadConfig(DatabaseConfig::class)!!
val rateLimitConfig: RateLimitConfig = loadConfig(RateLimitConfig::class)!!
val webHookConfig: WebHookConfig = loadConfig(WebHookConfig::class)!!
val apiConfig: ApiConfig = loadConfig(ApiConfig::class)!!

/**
 * Loads and generates config files based on the class
 * The receipt class needs to have the Settings annotation
 */
private inline fun <reified T:Any> loadConfig(clazz: KClass<*>): T? {
    val ann = clazz.findAnnotation<Settings>() ?: return null

    configFolder.mkdirs()

    val configFile = File(configFolder, ann.name + ".config")
    if (!configFile.exists()) {
        val instance = clazz.createInstance() as T
        val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance)

        configFile.writeText(json, StandardCharsets.UTF_8)
        return instance
    }

    return mapper.readValue(configFile, T::class.java)
}