package com.wynntils.athena.routes

import com.wynntils.athena.core.*
import com.wynntils.athena.core.enums.AccountType
import com.wynntils.athena.core.profiler.getSections
import com.wynntils.athena.core.routes.annotations.BasePath
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.database.enums.TextureResolution
import com.wynntils.athena.database.objects.ApiKey
import com.wynntils.athena.database.objects.User
import com.wynntils.athena.routes.managers.GuildManager
import io.javalin.http.Context
import org.json.simple.JSONArray
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
 *  POST /updateCosmetics/:apiKey
 *  POST /setGuildColor/:apiKey
 *  POST /setUserPassword/:apiKey
 *  POST /getUserByPassword/:apiKey
 *  POST /createApiKey/:apiKey
 *  POST /changeApiKey/:apiKey
 *  POST /getUserConfig/:apikEY
 *  GET /timings
 */
@BasePath("/api")
class ApiRoutes {

    /**
     * Returns information about the provided user
     * Required Body: user
     */
    @Route("/getUser/{apiKey}", type = RouteType.POST)
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

        val result = response.getOrCreate<JSONOrderedObject>("result")
        result["uuid"] = user._id.toString()
        result["username"] = user.username
        result["accountType"] = user.accountType.toString()
        result["authToken"] = user.authToken.toString()

        val versions = result.getOrCreate<JSONOrderedObject>("versions")
        versions["latest"] = user.latestVersion
        versions["used"] = user.usedVersions

        val discord = result.getOrCreate<JSONOrderedObject>("discord")
        discord["username"] = user.discordInfo?.username ?: ""
        discord["id"] = user.discordInfo?.id ?: ""

        val cosmetics = result.getOrCreate<JSONOrderedObject>("cosmetics")
        cosmetics["texture"] = user.cosmeticInfo.capeTexture
        cosmetics["isElytra"] = user.cosmeticInfo.elytraEnabled
        cosmetics["maxResolution"] = user.cosmeticInfo.maxResolution.toString()
        cosmetics["allowAnimated"] = user.cosmeticInfo.allowAnimated

        val parts = cosmetics.getOrCreate<JSONOrderedObject>("parts")
        if (user.cosmeticInfo.parts.isNotEmpty()) {
            for (part in user.cosmeticInfo.parts) {
                parts[part.key] = part.value
            }
        }

        response["message"] = "Successfully found user account."
        return response
    }

    /**
     * Sets the user account type
     * Required Body: user, type
     */
    @Route(path = "/setAccountType/{apiKey}", type = RouteType.POST)
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
     * Required Body: user, cosmeticObject
     */
    @Route(path = "/updateCosmetics/{apiKey}", type = RouteType.POST)
    fun setCosmeticTexture(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("user") || !body.contains("cosmetics")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'user' and 'cosmetics'."
            return response
        }

        val user = getUser(body["user"] as String)
        if (user == null) {
            ctx.status(400)

            response["message"] = "There's no users with the provided parameters."
            return response;
        }

        val cosmetics = body.getOrCreate<JSONObject>("cosmetics")
        if (cosmetics.contains("texture"))
            user.cosmeticInfo.capeTexture = cosmetics["texture"] as String
        if (cosmetics.contains("isElytra"))
            user.cosmeticInfo.elytraEnabled = cosmetics["isElytra"] as Boolean
        if (cosmetics.containsKey("maxResolution"))
            user.cosmeticInfo.maxResolution = TextureResolution.valueOf(cosmetics["maxResolution"] as String)
        if (cosmetics.containsKey("allowAnimated"))
            user.cosmeticInfo.allowAnimated = cosmetics["allowAnimated"] as Boolean

        if (cosmetics.contains("parts")) {
            val parts = cosmetics.getOrCreate<JSONObject>("parts")
            for (part in parts.keys) {
                user.cosmeticInfo.parts[part as String] = parts[part] as Boolean
            }
        }

        user.asyncSave()

        response["message"] = "Updated users cosmetics successfully."
        return response
    }

    /**
     * Sets guild territory color
     * Required Body: guild, color
     */
    @Route(path = "/setGuildColor/{apiKey}", type = RouteType.POST)
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
     * Required Body: token, password
     */
    @Route(path = "/setUserPassword/{apiKey}", type = RouteType.POST)
    fun setUserPassword(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("token") || !body.contains("password")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'token' and 'password'."
            return response
        }

        val user = DatabaseManager.getUserProfile(body["token"] as String)
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
    @Route(path = "/getUserByPassword/{apiKey}", type = RouteType.POST)
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
        result["uuid"] = user._id.toString()
        result["username"] = user.username
        result["accountType"] = user.accountType.toString()
        result["authToken"] = user.authToken.toString()

        val versions = result.getOrCreate<JSONOrderedObject>("versions")
        versions["latest"] = user.latestVersion
        versions["used"] = user.usedVersions

        val discord = result.getOrCreate<JSONOrderedObject>("discord")
        discord["username"] = user.discordInfo?.username ?: ""
        discord["id"] = user.discordInfo?.id ?: ""

        val cosmetics = result.getOrCreate<JSONOrderedObject>("cosmetics")
        cosmetics["texture"] = user.cosmeticInfo.capeTexture
        cosmetics["isElytra"] = user.cosmeticInfo.elytraEnabled
        cosmetics["maxResolution"] = user.cosmeticInfo.maxResolution.toString()
        cosmetics["allowAnimated"] = user.cosmeticInfo.allowAnimated

        val parts = cosmetics.getOrCreate<JSONOrderedObject>("parts")
        if (user.cosmeticInfo.parts.isNotEmpty()) {
            for (part in user.cosmeticInfo.parts) {
                parts[part.key] = part.value
            }
        }

        response["message"] = "Successfully found and validated user account."
        return response
    }

    /**
     * Registers a new API key
     * Required Body: name, description, adminContact, maxLimit
     */
    @Route(path = "/createApiKey/{apiKey}", type = RouteType.POST)
    fun createApiKey(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("name") || !body.contains("description") || !body.contains("adminContact") || !body.contains("maxLimit")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'name', 'description', 'adminContact' and 'maxLimit'."
            return response
        }

        val contactForm = ArrayList<String>()
        (body["adminContact"] as JSONArray).forEach { contactForm.add(it as String) }

        val apiKey = ApiKey(UUID.randomUUID().toString(),
            body["name"] as String,
            body["description"] as String,
            contactForm,
            (body["maxLimit"] as Long).toInt()
        )

        apiKey.asyncSave()

        response["message"] = "Successfully created an API Key."
        response["apiKey"] = apiKey._id

        return response
    }

    /**
     * Changes an API Key max limit
     * Required Body: key, maxLimit
     */
    @Route(path = "/changeApiKey/{apiKey}", type = RouteType.POST)
    fun changeApikey(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("key") || !body.contains("maxLimit")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'key' and 'maxLimit'."
            return response
        }

        val apiKey = DatabaseManager.getApiKey(body["key"] as String);
        if (apiKey == null) {
            ctx.status(400)

            response["message"] = "The provied API Key does not exists."
            return response
        }

        apiKey.maxLimit = (body["maxLimit"] as Long).toInt()
        response["message"] = "Successfully changed API Key."
        response["apiKey"] = apiKey._id

        return response
    }

    /**
     * Requests a cloud user configuration
     * Required Body: user, configName
     */
    @Route(path = "/getUserConfig/{apiKey}", type = RouteType.POST)
    fun getUserConfig(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        if (!ctx.isAuthenticated()) {
            ctx.status(401)

            response["message"] = "Invalid API Authorization Key."
            return response;
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("user") || !body.contains("configName")) {
            ctx.status(400)

            response["message"] = "Invalid body, expecting 'user' and 'configName'."
            return response

        }

        val user = getUser(body["user"] as String)
        if (user == null) {
            ctx.status(400)

            response["message"] = "There's no users with the provided parameters."
            return response;
        }

        val configFiles = user.getConfigFiles()
        if (body["configName"]!! == "list") {
            response["messsage"] = "Successfully located found all user configs."

            val result = response.getOrCreate<JSONArray>("result");
            result.addAll(configFiles.keys)
            return response
        }

        if (!configFiles.containsKey(body["configName"])) {
            response["message"] = "The provided config does not exists."
            return response
        }

        response["messsage"] = "Successfully located user '${body["configName"]}' configuration."
        response["result"] = (configFiles[body["configName"]] as String).asJSON()
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


    private fun getUser(userParam: String): User? {
        return when {
            userParam.startsWith("uuid-") ->
                DatabaseManager.getUserProfile(UUID.fromString(userParam.replace("uuid-", "")), false)
            userParam.isMinecraftUsername() -> DatabaseManager.getUsersProfiles(userParam).getOrNull(0)
            else -> DatabaseManager.getUserProfile(userParam)
        }
    }

}