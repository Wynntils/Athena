package com.wynntils.athena.core.configs.instances

import com.wynntils.athena.core.configs.annotations.Settings

@Settings(name = "webhook-config")
class WebHookConfig {

    val discordUrl: String = "your-url"
    val discordUsername: String = "Athena"
    val discordAvatar: String = "https://cdn.wynntils.com/athena_logo_1600x1600.png"

    val telegramUrl: String = "https://api.telegram.org/bot%s/sendMessage"
    val telegramKey: String = "your-key"
    val telegramChannel: String = "your-channel"

}