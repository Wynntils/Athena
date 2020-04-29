package com.wynntils.athena.routes

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.enums.AccountType
import com.wynntils.athena.core.isAuthenticated
import com.wynntils.athena.core.isColorHex
import com.wynntils.athena.core.isMinecraftUsername
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
import org.mindrot.jbcrypt.BCrypt
import java.util.*

/**
 * All api related routes
 * Base Path: /api
 * Required Parameters: apiKey
 *
 * Routes:
 *  POST /getUser/:apiKey
 *  POST /setAccountType/:apiKey
 *  POST /setCosmeticTexture/:apiKey
 *  POST /setGuildColor/:apiKey
 *  POST /setUserPassword/:apiKey
 *  POST /getUserByPassword/:apiKey
 *  GET /timings
 */
@BasePath("/api")
class ApiRoutes {

    /**
     * Returns information about the provided user
     * Required Body: user
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
            return response
        }

        response["message"] = "Successfully reached player information."
        response["result"] = mapper.readValue(mapper.writeValueAsBytes(user), JSONObject::class.java)

        return response
    }

    /**
     * Sets the user account type
     * Required Body: user, type
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
     * Required Body: user, sha1
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

    /**
     * Sets guild territory color
     * Required Body: guild, color
     */
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
     * Sets the user password
     * Required Body: user, password
     */
    @Route(path = "/setUserPassword/:apiKey", type = RouteType.POST)
    fun setUserPassword(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("user") || !body.contains("password")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'user' and 'password'."
            return response
        }

        val user = getUser(body["user"] as String)
        if (user == null) {
            ctx.status(400)

            response["message"] = "There's no users with the provided parameters."
            return response;
        }

        user.password = BCrypt.hashpw(body["password"] as String, BCrypt.gensalt(12))
        user.asyncSave()

        response["message"] = "Successfully set user account password."
        return response
    }

    /**
     * Gets the user profile and check if the provided password is valid
     * Required Body: user, password
     */
    @Route(path = "/getUserByPassword/:apiKey", type = RouteType.POST)
    fun getUserByPassword(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("user") || !body.contains("password")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'user' and 'password'."
            return response
        }

        val user = getUser(body["user"] as String)
        if (user == null) {
            ctx.status(400)

            response["message"] = "There's no users with the provided parameters."
            return response;
        }

        if (user.password.isEmpty() || !BCrypt.checkpw(body["password"] as String, user.password)) {
            ctx.status(401)

            response["message"] = "The provided password for the selected user is invalid."
            return response;
        }

        val result = response.getOrCreate<JSONOrderedObject>("result")
        result["uuid"] = user.id.toString()
        result["username"] = user.username
        result["accountType"] = user.accountType.toString()
        result["authToken"] = user.authToken

        val versions = result.getOrCreate<JSONOrderedObject>("versions")
        versions["latest"] = user.latestVersion
        versions["used"] = user.usedVersions

        val discord = result.getOrCreate<JSONOrderedObject>("discord")
        discord["username"] = user.discordInfo?.username ?: ""
        discord["id"] = user.discordInfo?.id ?: ""

        val cosmetics = result.getOrCreate<JSONOrderedObject>("cosmetics")
        cosmetics["texture"] = user.cosmeticInfo.capeTexture
        cosmetics["isElytra"] = user.cosmeticInfo.elytraEnabled

        val parts = cosmetics.getOrCreate<JSONOrderedObject>("parts")
        parts["ears"] = user.cosmeticInfo.earsEnabled

        response["message"] = "Successfully found and validated user account."
        return response
    }

    /**
     * The overall performance calculated for each route and similar stuff
     */
    @Route(path = "/timings", type = RouteType.GET)
    fun timings(ctx: Context): JSONOrderedObject {
        val result = JSONOrderedObject()

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