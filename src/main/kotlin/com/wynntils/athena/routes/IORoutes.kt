package com.wynntils.athena.routes

import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.configs.rateLimitConfig
import com.wynntils.athena.core.redirectTo
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.routes.routeLogger
import com.wynntils.athena.core.validIp
import com.wynntils.athena.routes.data.RateLimitProfile
import io.javalin.http.Context
import org.json.simple.JSONObject
import java.util.concurrent.ConcurrentHashMap

class IORoutes {

    private val limitProfiles = ConcurrentHashMap<String, RateLimitProfile>()

    /**
     * Handles all the RateLimits
     */
    @Route(path = "*", type = RouteType.MIDDLE_WARE)
    fun rateLimits(ctx: Context): JSONObject? {
        clearProfiles() // removes old entries
        if (rateLimitConfig.exceptions.contains(ctx.validIp()))  return null
        val profile = limitProfiles.getOrPut(ctx.validIp()) { RateLimitProfile() }

        if (profile.increaseRequest())
            routeLogger.warn("[RateLimit] " + ctx.validIp() + " was rate limited for 10 minutes.")

        if (!profile.rateLimited) return null

        val resultObject = JSONObject()
        resultObject["message"] = "You're being rate limited!"
        resultObject["requests"] = profile.requests

        ctx.status(429)
        return resultObject
    }

    /**
     * Redirects the user to the fallback url if 404
     */
    @Route(type = RouteType.ERROR, errorCode = 404)
    fun handle404(ctx: Context) {
        ctx.redirectTo(generalConfig.fallbackUrl)
    }

    private fun clearProfiles() {
        val it = limitProfiles.iterator()
        while (it.hasNext()) {
            if (!it.next().value.canBeRemoved()) continue

            it.remove()
        }
    }

}