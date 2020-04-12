package com.wynntils.athena.core.configs.instances

import com.wynntils.athena.core.configs.annotations.Settings

@Settings(name = "database-config")
data class DatabaseConfig(

    val ip: String = "localhost",
    val port: Int = 28015,
    val database: String = "Athena",
    val username: String = "test",
    val password: String = "test"

)