package com.wynntils.athena.routes

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.cache.CacheManager
import com.wynntils.athena.core.getOrCreate
import com.wynntils.athena.core.routes.annotations.BasePath
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.toDashedUUID
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.generalLog
import com.wynntils.athena.routes.data.MinecraftFakeAuth
import io.javalin.http.Context
import org.json.simple.JSONObject

/**
 * Contains all Authentication Routes
 * Base Path: /auth
 *
 * Routes:
 *  GET /getPublicKey
 *  POST /responseEncryption
 */
@BasePath("/auth")
class AuthenticationRoutes {

    private val authManager = MinecraftFakeAuth()

    /**
     * Returns a JSONObject containing the public key
     */
    @Route(path = "/getPublicKey", type = RouteType.GET)
    fun getPublicKey(ctx: Context): JSONObject {
        val response = JSONObject()
        response["publicKeyIn"] = authManager.getPublicKey()

        return response
    }

    /**
     * Retrieves the client generated shared key to verify the authentication
     */
    @Route(path = "/responseEncryption", type = RouteType.POST)
    fun responseEncryption(ctx: Context): JSONObject {
        val response = JSONObject()
        val body = ctx.body().asJSON<JSONObject>()

        if (!body.containsKey("username") || !body.containsKey("key") || !body.containsKey("version")) {
            ctx.status(400)
            response["message"] = "Expecting parameters 'username', 'key' and 'version'."
            return response
        }

        if (body["username"] !is String || body["key"] !is String) {
            ctx.status(401)
            response["message"] = "Username or Key is null."
            return response
        }

        val profile = authManager.getGameProfile(body["username"] as String, body["key"] as String)
        if (profile == null) {
            ctx.status(401)
            response["message"] = "The provided username or key is invalid."
            return response
        }

        val user = DatabaseManager.getUserProfile(profile.id.toDashedUUID())!!
        user.updateAccount(profile.name, body["version"] as String)

        ctx.status(200)
        response["message"] = "Authentication code generated."
        response["authToken"] = user.authToken.toString()

        response["configFiles"] = user.getConfigFiles()

        val hashes = response.getOrCreate<JSONObject>("hashes")
        for(entry in CacheManager.getCaches()) {
            hashes[entry.key] = entry.value.hash
        }

        generalLog.info("${body["username"]} authenticated successfully using ${body["version"]}")
        return response
    }

}
