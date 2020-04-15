package com.wynntils.athena.routes

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.getOrCreate
import com.wynntils.athena.core.routes.annotations.BasePath
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.core.utils.ZLibHelper
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.routes.managers.CapeManager
import io.javalin.http.Context
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.util.*

/**
 * All user related routes
 * Base Path: /user
 * Required Parameters: TOKEN
 *
 * Routes:
 *  POST /updateDiscord
 *  POST /uploadConfigs
 */
@BasePath("/user")
class UserRoutes {

    /**
     * Updates the user Discord Information
     * Required Body: authToken, id, username
     */
    @Route(path = "/updateDiscord", type = RouteType.POST)
    fun updateDiscord(ctx: Context): JSONObject {
        val response = JSONObject()
        val body = ctx.body().asJSON<JSONObject>()

        if (!body.contains("authToken") || !body.contains("id") || !body.contains("username")) {
            ctx.status(400)

            response["message"] = "Expecting parameters 'authToken', 'id' and 'username'."
            return response
        }

        val user = DatabaseManager.getUserProfile(body["authToken"] as String)
        if (user == null) {
            ctx.status(401)

            response["message"] = "The provided Authorization Token is invalid."
            return response
        }

        user.updateDiscord(body["id"] as String, body["username"] as String)

        ctx.status(200)
        response["message"] = "Successfully updated ${user.username} Discord Information."
        return response
    }

    /**
     * Handle e stores User Configurations
     * Required Body: MULTIPART FORM (authToken; config)
     */
    @Route(path = "/uploadConfigs", type = RouteType.POST)
    fun uploadConfigs(ctx: Context): JSONObject {
        val response = JSONObject()
        if (!ctx.isMultipartFormData() || ctx.formParams("authToken").isEmpty() || ctx.uploadedFiles("config").isEmpty()) {
            ctx.status(400)

            response["message"] = "Expecting MultiPart Form, containing 'authToken' and 'config'."
            return response
        }

        val user = DatabaseManager.getUserProfile(ctx.formParams("authToken").first())
        if (user == null) {
            ctx.status(401)

            response["message"] = "The provided Authorization Token is invalid."
            return response
        }

        val uploadResult = response.getOrCreate<JSONArray>("results")
        for (file in ctx.uploadedFiles("config")) {
            val fileResult = JSONObject()
            uploadResult.add(fileResult)
            fileResult["name"] = file.filename

            val content = ZLibHelper.deflate(file.content.readBytes())

            if (content.size > 200000) { // bigger than 200kbp
                fileResult["message"] = "The provided configuration is bigger than 200 kilobytes."
                continue
            }
            if (user.getConfigAmount() >= 80) {
                fileResult["message"] = "User exceeded the configuration amount limit."
                continue
            }

            user.setConfig(file.filename, content)
            fileResult["message"] = "Configuration stored successfully."
        }

        return response
    }

    @Route(path = "/getCosmetics", type = RouteType.POST)
    fun getCosmetics(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.contains("uuid")) {
            ctx.status(400)

            response["message"] = "Expecting parameters 'uuid'."
            return response
        }

        val user = DatabaseManager.getUserProfile(UUID.fromString(body["uuid"] as String), false)
        if (user == null) {
            ctx.status(400)

            response["message"] = "The provided user does not exists."
            return response
        }

        val result = response.getOrCreate<JSONOrderedObject>("result")
        result["hasEars"] = user.cosmeticInfo.earsEnabled
        result["hasCape"] = user.cosmeticInfo.hasCape()
        result["hasElytra"] = user.cosmeticInfo.hasElytra()

        result["texture"] = CapeManager.getCapeAsBase64(user.cosmeticInfo.getFormattedTexture())

        return response
    }

}