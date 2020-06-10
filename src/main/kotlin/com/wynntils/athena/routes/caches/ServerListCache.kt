package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.currentTimeMillis
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.mapper
import org.json.simple.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets

@CacheInfo(name = "serverList", refreshRate = 30)
class ServerListCache: DataCache {

    private var firstSeem = HashMap<String, Long>()

    /**
     * Generates the cache based on the wynn online players api and calculates the server uptime
     * based on the first time it was seen
     */
    override fun generateCache(): JSONOrderedObject {
        val result = JSONOrderedObject()

        val connection = URL(apiConfig.wynnOnlinePlayers).openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.setRequestProperty("apikey", apiConfig.wynnApiToken)
        connection.readTimeout = 5000
        connection.connectTimeout = 5000

        val onlinePlayers =  connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON<JSONObject>()

        val servers = result.getOrCreate<JSONOrderedObject>("servers")

        // generating server data
        val validServers = ArrayList<String>()
        for(key in onlinePlayers.keys) {
            key as String
            if (key == "request") continue

            validServers += key
            val server = servers.getOrCreate<JSONOrderedObject>(key)

            server["firstSeen"] = firstSeem.getOrPut(key) { currentTimeMillis() }
            server["players"] = onlinePlayers[key]
        }

        // clean old servers
        val it = firstSeem.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (validServers.contains(next.key)) continue

            it.remove()
        }

        return result
    }

    /**
     * Loads server uptime persistent data
     */
    override fun loadPersistentData(input: String?) {
        if (input == null) return;

        firstSeem = mapper.readValue(input, HashMap::class.java) as HashMap<String, Long>
    }

    /**
     * Saves the server uptime inside the persistent data
     */
    override fun generatePersistentData(): String? {
        return mapper.writeValueAsString(firstSeem)
    }

}