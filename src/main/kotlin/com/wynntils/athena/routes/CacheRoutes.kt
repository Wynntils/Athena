package com.wynntils.athena.routes

import com.wynntils.athena.core.cache.CacheManager
import com.wynntils.athena.core.currentTimeMillis
import com.wynntils.athena.core.getOrCreate
import com.wynntils.athena.core.routes.annotations.BasePath
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.utils.JSONOrderedObject
import io.javalin.http.Context
import org.json.simple.JSONObject

/**
 * Contains all Cache Routes
 * Base Path: /cache
 *
 * Routes:
 *  GET /get/:name
 *  GET /getHashes
 */
@BasePath("/cache")
class CacheRoutes {

    /**
     * Returns the cache based on the provided name
     */
    @Route(path = "/get/:name", type = RouteType.GET)
    fun getCache(ctx: Context): String {
        val cache = ctx.pathParam("name")
        if (!CacheManager.isCached(cache)) {
            ctx.status(404)

            val result = JSONOrderedObject()
            result["message"] = "There's not a cache with the provided name."

            return result.toJSONString()
        }

        ctx.header("timestamp", currentTimeMillis().toString())
        return CacheManager.getCache(cache)!!.value
    }

    /**
     * Returns a list of verification hashes for each cache available
     */
    @Route(path = "/getHashes", type = RouteType.GET)
    fun getHashes(ctx: Context): JSONObject {
        val response = JSONObject()
        response["message"] = "Successfully grabbed cache hashes."

        val result = response.getOrCreate<JSONObject>("result")
        for(entry in CacheManager.getCaches()) {
            result[entry.key] = entry.value.hash
        }

        return response
    }

}