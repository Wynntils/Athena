package com.wynntils.athena.core.configs.instances

import com.wynntils.athena.core.configs.annotations.Settings

@Settings(name = "email-config")
data class EmailConfig(

    val host: String = "<insert-here>",
    val port: Int = 587,

    val user: String = "insert@here.com",
    val password: String = "insert-here"

)