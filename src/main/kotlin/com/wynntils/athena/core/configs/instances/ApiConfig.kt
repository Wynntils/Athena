package com.wynntils.athena.core.configs.instances

import com.wynntils.athena.core.configs.annotations.Settings

@Settings(name = "api-config")
data class ApiConfig (

    val mojangAuth: String = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s",

    val wynnTerritories: String = "https://api.wynncraft.com/public_api.php?action=territoryList",
    val scyuTerritories: String = "https://raw.githubusercontent.com/DevScyu/Wynn/master/territories.json",
    val wynnMapLocations: String = "https://api.wynncraft.com/public_api.php?action=mapLocations",
    val wynnItems: String = "https://api.wynncraft.com/public_api.php?action=itemDB&category=all",
    val wynnGuildInfo: String = "https://api.wynncraft.com/public_api.php?action=guildStats&command=",
    val wynnOnlinePlayers: String = "https://api.wynncraft.com/public_api.php?action=onlinePlayers"

)