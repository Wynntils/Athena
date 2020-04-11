package com.wynntils.athena.core.configs.instances

import com.wynntils.athena.core.configs.annotations.Settings

@Settings(name = "api-config")
class ApiConfig {

    val mojangAuth = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s"

    val wynnTerritories = "https://api.wynncraft.com/public_api.php?action=territoryList"
    val scyuTerritories = "https://raw.githubusercontent.com/DevScyu/Wynn/master/territories.json"
    val wynnMapLocations = "https://api.wynncraft.com/public_api.php?action=mapLocations"
    val wynnItems = "https://api.wynncraft.com/public_api.php?action=itemDB&category=all"
    val wynnGuildInfo = "https://api.wynncraft.com/public_api.php?action=guildStats&command="
    val wynnOnlinePlayers = "https://api.wynncraft.com/public_api.php?action=onlinePlayers"

}