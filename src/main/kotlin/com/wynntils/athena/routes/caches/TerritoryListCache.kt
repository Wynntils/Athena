package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.routes.managers.GuildManager
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets

@CacheInfo(name = "territoryList", refreshRate = 30)
class TerritoryListCache: DataCache {

    /**
     * Generates the cache based on merging Wynn and Scyu's Territory Data
     */
    override fun generateCache(): JSONOrderedObject {
        var connection = URL(apiConfig.scyuTerritories).openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.readTimeout = 5000
        connection.connectTimeout = 5000

        val scyuTerritories =  connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON<JSONArray>()

        connection = URL(apiConfig.wynnTerritories).openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.setRequestProperty("apikey", apiConfig.wynnApiToken)

        connection.readTimeout = 5000
        connection.connectTimeout = 5000

        val wynnTerritories = connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8)
            .asJSON<JSONObject>()["territories"] as JSONObject

        val response = JSONOrderedObject()
        val result = response.getOrCreate<JSONOrderedObject>("territories")
        for(territory in scyuTerritories) {
            territory as JSONObject

            val name = territory["name"] as String
            val wynn = wynnTerritories[name] as JSONObject

            val final = result.getOrCreate<JSONOrderedObject>(name)

            // location stuff
            val start = territory["start"].toString().split(",")
            val end = territory["end"].toString().split(",")

            val location = final.getOrCreate<JSONOrderedObject>("location")
            location["startX"] = start[0].toInt()
            location["startZ"] = start[1].toInt()
            location["endX"] = end[0].toInt()
            location["endZ"] = end[1].toInt()
            location["spawn"] = territory["spawnLocation"]

            val guild = GuildManager.getGuildData(wynn["guild"] as String)!!

            // merging stuff
            final["territory"] = wynn["territory"]
            final["guild"] = guild.id
            final["guildPrefix"] = guild.prefix
            final["guildColor"] = guild.color
            final["acquired"] = wynn["acquired"]
            final["attacker"] = wynn["attacker"]
            final["level"] = territory["level"]
            final["location"] = location
        }

        return response
    }

}