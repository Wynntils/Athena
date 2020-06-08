package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.exceptions.UnexpectedCacheResponse
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.configs.generalConfig
import org.json.simple.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets

@CacheInfo(name = "mapLocations", refreshRate = 86400)
class MapLocationsCache: DataCache {

    /**
     * A direct cache of wynn's map locations API
     */
    override fun generateCache(): JSONObject {
        var connection = URL(apiConfig.wynnMapLocations).openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.setRequestProperty("apikey", apiConfig.wynnApiToken)
        connection.readTimeout = 5000
        connection.connectTimeout = 5000

        val locations = connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON<JSONObject>();
        if (!locations.containsKey("locations")) throw UnexpectedCacheResponse()
        locations.remove("request")

        connection = URL(apiConfig.wynnMapLabels).openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.readTimeout = 5000
        connection.connectTimeout = 5000

        val labels = connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON<JSONObject>();

        locations["labels"] = labels["labels"]
        return locations
    }

}