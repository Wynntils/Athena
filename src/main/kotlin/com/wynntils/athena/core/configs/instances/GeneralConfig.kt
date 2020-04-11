package com.wynntils.athena.core.configs.instances

import com.wynntils.athena.core.configs.annotations.Settings

@Settings(name = "general-config")
class GeneralConfig {

    val port: Int = 8888
    val fallbackUrl: String = "https://wynntils.com"
    val baseUrl: String = "http://localhost:8888/"
    val apiKeys: List<String> = listOf("my-default-api-key")
    val routeThreads: Int = 5
    val userAgent = "WynntilsAthena/2.0.0"

}