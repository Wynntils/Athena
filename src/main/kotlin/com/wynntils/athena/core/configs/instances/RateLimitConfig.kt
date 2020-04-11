package com.wynntils.athena.core.configs.instances

import com.wynntils.athena.core.configs.annotations.Settings

@Settings(name = "ratelimit-config")
class RateLimitConfig {

    val user: Int = 800
    val timeout: Long = 600000

    val exceptions: List<String> = listOf("localhost")

}