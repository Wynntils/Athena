package com.wynntils.athena.routes

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.enums.Hash
import com.wynntils.athena.core.getOrCreate
import com.wynntils.athena.core.isHuman
import com.wynntils.athena.core.routes.annotations.BasePath
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.routes.managers.CapeManager
import io.javalin.http.Context
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO

/**
 * Contains all Capes Routes
 * Base Path: /capes
 *
 * Routes:
 *  GET /get/:id
 *  GET /user/:uuid
 *  GET /list
 *  GET /queue/get/:id
 *  GET /queue/approve/:token/:sha1
 *  GET /queue/ban/:token/:sha1
 *  POST /queue/upload/:token
 *  POST /delete/:token
 */
@BasePath("/capes")
class CapeRoutes {

    @Route(path = "/get/:id", type = RouteType.GET)
    fun getCape(ctx: Context): InputStream {
        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.contentType("image/png")

        return CapeManager.getCape(ctx.pathParam("id"))
    }

    @Route(path = "/user/:uuid", type = RouteType.GET)
    fun getUserCape(ctx: Context): InputStream {
        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.contentType("image/png")

        val user = DatabaseManager.getUserProfile(UUID.fromString(ctx.pathParam("uuid")))!!
        return CapeManager.getCape(user.cosmeticInfo.getFormattedTexture())
    }

    @Route(path = "/list", type = RouteType.GET)
    fun list(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()

        val result = response.getOrCreate<JSONArray>("result")
        CapeManager.listCapes().forEach { result.add(it) }

        return response;
    }

    @Route(path = "/delete/:token", type = RouteType.POST)
    fun delete(ctx: Context): JSONOrderedObject {
        val result = JSONOrderedObject()
        if (!verifyToken(ctx.pathParam("token"))) {
            ctx.status(400)

            result["message"] = "Invalid authorization token."
            return result
        }

        val body = ctx.body().asJSON<JSONObject>()
        if (!body.containsKey("sha-1")) {
            ctx.status(400)

            result["message"] = "Invalid body, expecting 'sha-1'."
            return result
        }

        val sha1 = body["sha-1"] as String
        if (!CapeManager.deleteCape(sha1)) {
            ctx.status(400)

            result["message"] = "The provided cape SHA-1 doesn't exists."
            return result
        }

        result["message"] = "The provided cape was deleted successfully."
        return result
    }

    @Route(path = "/queue/get/:id", type = RouteType.GET)
    fun getAnalyseCape(ctx: Context): InputStream {
        ctx.contentType("image/png")
        val id = ctx.pathParam("id")
        if (CapeManager.isApproved(id)) return CapeManager.getCape(id)

        return CapeManager.getQueuedCape(id)
    }

    @Route(path = "/queue/upload/:token", type = RouteType.POST)
    fun uploadCape(ctx: Context): JSONObject {
        val result = JSONObject()
        if (!verifyToken(ctx.pathParam("token"))) {
            ctx.status(400)
            result["message"] = "Invalid authorization token."
            return result
        }

        val capes = ctx.uploadedFiles("cape")
        if (capes.isEmpty()) {
            ctx.status(400)
            result["message"] = "There was no uploaded file with the name cape."
            return result
        }

        val capeResults = result.getOrCreate<JSONArray>("results")
        for (file in capes) {
            val fileResult = JSONObject()
            capeResults.add(fileResult)

            fileResult["name"] = file.filename
            if (file.size > 500000) { // 500kb
                fileResult["message"] = "The provided file excess the 500kb limit."
                continue
            }
            if (file.extension != ".png") {
                fileResult["message"] = "The provided file is not a PNG image."
                continue
            }

            val image = ImageIO.read(ByteArrayInputStream(file.content.readBytes()))

            // image checksum
            if (image == null) {
                fileResult["message"] = "The provided file is not a PNG image."
                continue
            }
            if (image.width % 64 != 0 || image.height % (image.width / 2) != 0) {
                fileResult["message"] = "The image needs to be multiple of 64x32."
                continue
            }

            CapeManager.maskCape(image)
            val bos = ByteArrayOutputStream()
            ImageIO.write(image, "png", bos)
            val maskedImage = bos.toByteArray();

            val hash = Hash.SHA1.hash(maskedImage)
            fileResult["sha-1"] = hash
            if (CapeManager.isApproved(hash)) {
                fileResult["message"] = "The provided cape is already approved."
                continue
            }
            if (CapeManager.isQueued(hash)) {
                fileResult["message"] = "The provided cape is already queued."
                continue
            }
            if (CapeManager.isBanned(hash)) {
                fileResult["message"] = "The provided cape is banned."
                continue
            }

            fileResult["message"] = "Added to the queue."
            CapeManager.queueCape(maskedImage)
        }

        return result
    }

    @Route(path = "/queue/approve/:token/:sha1", type = RouteType.GET)
    fun approveCape(ctx: Context): JSONObject {
        val result = JSONObject()
        if (!ctx.isHuman()) {
            result["message"] = "Hello bot! Unfortunately you're not a human!."
            return result
        }
        if (!verifyToken(ctx.pathParam("token"))) {
            result["message"] = "Invalid authorization token."
            return result
        }

        val sha1 = ctx.pathParam("sha1")
        if (!CapeManager.isQueued(sha1)) {
            result["message"] = "There's not a cape in the queue with the provided SHA-1."
            return result
        }

        CapeManager.approveCape(sha1)
        result["message"] = "Successfully approved the cape."
        return result
    }

    @Route(path = "/queue/ban/:token/:sha1", type = RouteType.GET)
    fun banCape(ctx: Context): JSONObject {
        val result = JSONObject()
        if (!ctx.isHuman()) {
            result["message"] = "Hello bot! Unfortunately you're not a human!."
            return result
        }
        if (!verifyToken(ctx.pathParam("token"))) {
            result["message"] = "Invalid authorization token."
            return result
        }

        val sha1 = ctx.pathParam("sha1")
        if (!CapeManager.isQueued(sha1)) {
            result["message"] = "There's not a cape in the queue with the provided SHA-1."
            return result
        }

        CapeManager.banCape(sha1)
        result["message"] = "Successfully banned the cape."
        return result
    }

    private fun verifyToken(token: String): Boolean {
        if (token == CapeManager.token) return true
        if (generalConfig.apiKeys.contains(token)) return true

        return false
    }

}