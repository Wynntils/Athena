package com.wynntils.athena.routes

import com.wynntils.athena.core.enums.AccountType
import com.wynntils.athena.core.getOrCreate
import com.wynntils.athena.core.isAuthenticated
import com.wynntils.athena.core.isMinecraftUsername
import com.wynntils.athena.core.profiler.getSections
import com.wynntils.athena.core.routes.annotations.BasePath
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.database.objects.UserProfile
import com.wynntils.athena.mapper
import io.javalin.http.Context
import org.json.simple.JSONObject
import java.util.*

/**
 * All api related routes
 * Base Path: /api
 * Required Parameters: apiKey
 *
 * Routes:
 *  GET /getUser/:apiKey/:user
 *  GET /setAccountType/:apiKey/:user/:type
 *  GET /setCosmeticTexture/:apiKey/:user/:sha1
 *  GET /timings
 */
@BasePath("/api")
class ApiRoutes {

    /**
     * Returns information about the provided user
     */
    @Route("/getUser/:apiKey/:user", type = RouteType.GET)
    fun getUser(ctx: Context): JSONObject {
        val response = JSONObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(400)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val user = getUser(ctx.pathParam("user"))
        if (user == null) {
            ctx.status(400)

            response["message"] = "There's no users with the provided parameters."
            return response;
        }

        response["message"] = "Successfully reached player information."
        response["result"] = mapper.readValue(mapper.writeValueAsBytes(user), JSONObject::class.java)

        return response
    }

    /**
     * Sets the user account type
     */
    @Route(path = "/setAccountType/:apiKey/:user/:type", type = RouteType.GET)
    fun setUserAccount(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(400)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val user = getUser(ctx.pathParam("user"))
        if (user == null) {
            ctx.status(400)

            response["message"] = "There's no users with the provided parameters."
            return response;
        }

        val accountType = AccountType.valueOr(ctx.pathParam("type"))
        user.accountType = accountType
        user.asyncSave()

        response["message"] = "Successfully set player account type."
        return response
    }

    /**
     * Sets the user cosmetic texture based on it SHA-1
     */
    @Route(path = "/setCosmeticTexture/:apiKey/:user/:sha1", type = RouteType.GET)
    fun setCosmeticTexture(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(400)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val user = getUser(ctx.pathParam("user"))
        if (user == null) {
            ctx.status(400)

            response["message"] = "There's no users with the provided parameters."
            return response;
        }

        user.cosmeticInfo.capeTexture = ctx.pathParam("sha1")
        user.asyncSave()

        response["message"] = "Successfully set player cosmetic texture sha1."
        return response
    }

    /**
     * The overall performance calculated for each route and similar stuff
     */
    @Route(path = "/timings", type = RouteType.GET)
    fun timings(ctx: Context): JSONObject {
        val result = JSONObject()

        for (section in getSections()) {
            val instances = if (section.name.contains("-")) section.name.split("-") else listOf(section.name)

            var lastObject = result
            for (i in instances.indices) {
                if (i == instances.size-1) {
                    lastObject[instances[i]] = "${section.time.div(1000000L)}ms"
                    continue
                }

                lastObject = lastObject.getOrCreate(instances[i])
            }
        }

        return result
    }

    private fun getUser(userParam: String): UserProfile? {
        return when {
            userParam.startsWith("uuid-") ->
                DatabaseManager.getUserProfile(UUID.fromString(userParam.replace("uuid-", "")), false)
            userParam.isMinecraftUsername() -> DatabaseManager.getUsersProfiles(userParam).getOrNull(0)
            else -> DatabaseManager.getUserProfile(userParam)
        }
    }

}