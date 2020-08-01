package com.wynntils.athena.routes

import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.configs.rateLimitConfig
import com.wynntils.athena.core.currentTimeMillis
import com.wynntils.athena.core.redirectTo
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.routes.rateLimitIgnoredRoutes
import com.wynntils.athena.core.routes.routeLogger
import com.wynntils.athena.core.validIp
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.database.objects.ApiKeyProfile
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
        if (rateLimitIgnoredRoutes.contains(ctx.path())) return null
        clearProfiles() // removes old entries

        var entry: String = ctx.validIp()
        var maxLimit: Int = rateLimitConfig.user
        var apiKey: ApiKeyProfile? = null

        // check for API Keys
        if (ctx.header("W-ApiKey") != null) {
            apiKey = DatabaseManager.getApiKey(ctx.header("W-ApiKey")!!)
            if (apiKey != null) {
                apiKey.addRequest()

                entry = apiKey.id
                maxLimit = apiKey.maxLimit
            }
        }

        if (rateLimitConfig.exceptions.contains(entry)) return null
        val profile = limitProfiles.getOrPut(entry) { RateLimitProfile(maxLimit) }

        if (profile.increaseRequest()) {
            routeLogger.warn("[RateLimit] $entry was rate limited for 10 minutes.")
            apiKey?.sendRateLimitWarning()
        }

        // add default RateLimit headers
        ctx.header("W-RateLimit-Max", maxLimit.toString())
        ctx.header("W-RateLimit-Current", profile.requests.toString())
        ctx.header("W-RateLimit-Release", (profile.releaseTime - currentTimeMillis()).toString())

        if (!profile.rateLimited) return null

        val resultObject = JSONObject()
        resultObject["message"] = "You're being rate limited!"

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