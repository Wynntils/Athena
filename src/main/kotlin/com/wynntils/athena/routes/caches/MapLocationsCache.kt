package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.asSimpleJson
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
        val connection = URL(apiConfig.wynnMapLocations).openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.readTimeout = 5000
        connection.connectTimeout = 5000

        val result = connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asSimpleJson<JSONObject>();
        if (!result.containsKey("locations")) throw UnexpectedCacheResponse()

        return result;
    }

}