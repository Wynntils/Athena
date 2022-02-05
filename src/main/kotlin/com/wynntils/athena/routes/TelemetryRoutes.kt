package com.wynntils.athena.routes

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.currentTimeMillis
import com.wynntils.athena.core.data.Location
import com.wynntils.athena.core.routes.annotations.BasePath
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.database.enums.GatheringMaterial
import com.wynntils.athena.database.enums.ProfessionType
import com.wynntils.athena.database.objects.GatheringSpot
import io.javalin.http.Context
import org.json.simple.JSONObject

@BasePath("/telemetry")
class TelemetryRoutes {

    @Route(path = "/sendGatheringSpot", type = RouteType.POST)
    fun sendGatheringSpot(ctx: Context): JSONOrderedObject {
        val response = JSONOrderedObject()
        val body = ctx.body().asJSON<JSONObject>()

        if (!body.contains("authToken") || !body.contains("spot")) {
            ctx.status(400)

            response["message"] = "Expecting parameters 'authToken', 'spot'."
            return response
        }

        val spotData = body["spot"] as JSONObject
        if (!spotData.contains("type") || !spotData.contains("material") || !spotData.contains("x") || !spotData.containsKey(
                "y"
            ) || !spotData.containsKey("z")
        ) {
            response["message"] = "The 'spot' object is expecting the parameters 'type', 'material', 'x', 'y,' 'z'."
            return response
        }

        val user = DatabaseManager.getUserProfile(body["authToken"] as String)
        if (user == null) {
            ctx.status(401)

            response["message"] = "The provided Authorization Token is invalid."
            return response
        }

        val location =
            Location((spotData["x"] as Long).toInt(), (spotData["y"] as Long).toInt(), (spotData["z"] as Long).toInt())
        val spot = DatabaseManager.getGatheringSpot(location)
            ?: GatheringSpot(
                location.toString(),
                ProfessionType.valueOf(spotData["type"] as String),
                GatheringMaterial.valueOf(spotData["material"] as String)
            )

        spot.users.add(user._id)
        spot.lastSeen = currentTimeMillis()

        spot.asyncSave()

        response["message"] = "Successfully registered the provided Gathering Spot."
        return response
    }

}