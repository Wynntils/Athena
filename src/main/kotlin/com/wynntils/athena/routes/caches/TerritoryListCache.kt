package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.routes.managers.GuildManager
import org.json.simple.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets

@CacheInfo(name = "territoryList", refreshRate = 30)
class TerritoryListCache: DataCache {

    /**
     * Generates the cache based on Wynn's Territory Data
     */
    override fun generateCache(): JSONOrderedObject {
        val connection = URL(apiConfig.wynnTerritories).openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.setRequestProperty("apikey", apiConfig.wynnApiToken)

        connection.readTimeout = 5000
        connection.connectTimeout = 5000

        val wynnTerritories = connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8)
            .asJSON<JSONObject>()["territories"] as JSONObject

        val response = JSONOrderedObject()
        val result = response.getOrCreate<JSONOrderedObject>("territories")
        for(name in wynnTerritories.keys) {
            name as String

            val wynn = wynnTerritories[name] as JSONObject
            val final = result.getOrCreate<JSONOrderedObject>(name)

            val guild = GuildManager.getGuildData(wynn["guild"] as String)!!

            final["territory"] = wynn["territory"]
            final["guild"] = guild.id
            final["guildPrefix"] = guild.prefix
            final["guildColor"] = guild.color
            final["acquired"] = wynn["acquired"]
            final["attacker"] = wynn["attacker"]
            final["level"] = 1 // not used
            final["location"] = wynn["location"]
        }

        return response
    }

}