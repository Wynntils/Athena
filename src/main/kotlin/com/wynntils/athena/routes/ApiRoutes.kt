package com.wynntils.athena.routes

import com.wynntils.athena.core.*
import com.wynntils.athena.core.enums.AccountType
import com.wynntils.athena.core.profiler.getSections
import com.wynntils.athena.core.routes.annotations.BasePath
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.database.objects.UserProfile
import com.wynntils.athena.mapper
import com.wynntils.athena.routes.managers.GuildManager
import io.javalin.http.Context
import org.json.simple.JSONObject
import java.util.*

/**
 * All api related routes
 * Base Path: /api
 * Required Parameters: apiKey
 *
 * Routes:
 *  POST /getUser/:apiKey/:user
 *  POST /setAccountType/:apiKey/:user/:type
 *  POST /setCosmeticTexture/:apiKey/:user/:sha1
 *  POST /setGuildColor
 *  GET /timings
 */
@BasePath("/api")
class ApiRoutes {

    /**
     * Returns information about the provided user
     */
    @Route("/getUser/:apiKey", type = RouteType.POST)
    fun getUser(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("user")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'user' param."
            return response
        }

        val user = getUser(body["user"] as String)
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
    @Route(path = "/setAccountType/:apiKey", type = RouteType.POST)
    fun setUserAccount(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("user") || !body.containsKey("type")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'user' and 'type'."
            return response
        }

        val user = getUser(body["user"] as String)
        if (user == null) {
            ctx.status(400)

            response["message"] = "There's no users with the provided parameters."
            return response;
        }

        val accountType = AccountType.valueOr(body["type"] as String)
        user.accountType = accountType
        user.asyncSave()

        response["message"] = "Successfully set player account type."
        return response
    }

    /**
     * Sets the user cosmetic texture based on it SHA-1
     */
    @Route(path = "/setCosmeticTexture/:apiKey", type = RouteType.POST)
    fun setCosmeticTexture(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("user") || !body.containsKey("sha1")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'user' and 'sha1'."
            return response
        }

        val user = getUser(body["user"] as String)
        if (user == null) {
            ctx.status(400)

            response["message"] = "There's no users with the provided parameters."
            return response;
        }

        user.cosmeticInfo.capeTexture = body["sha1"] as String
        user.asyncSave()

        response["message"] = "Successfully set player cosmetic texture sha1."
        return response
    }

    @Route(path = "/setGuildColor/:apiKey", type = RouteType.POST)
    fun setGuildColor(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("guild") || !body.containsKey("color")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'guild' and 'color'."
            return response
        }

        val guild = GuildManager.getGuildData(body["guild"] as String)
        if (guild == null) {
            ctx.status(400)

            response["message"] = "There's not a guild with the provided name."
            return response;
        }

        val color = body["color"] as String
        if (!color.isColorHex()) {
            ctx.status(400)

            response["message"] = "The provided color is not in the HEX format."
            return response;
        }

        guild.color = color
        guild.asyncSave()

        response["message"] = "Successfully set guild color."
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