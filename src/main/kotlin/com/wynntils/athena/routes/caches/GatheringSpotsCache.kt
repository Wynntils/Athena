package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.database.DatabaseManager
import com.wynntils.athena.database.enums.GatheringType
import org.json.simple.JSONArray
import org.json.simple.JSONObject

@CacheInfo(name = "gatheringSpots", refreshRate = 3600) // refreshes every 1 hour
class GatheringSpotsCache: DataCache {

    override fun generateCache(): JSONOrderedObject {
        val result = JSONOrderedObject()

        val woodCutting = result.getOrCreate<JSONArray>("woodCutting")
        val mining = result.getOrCreate<JSONArray>("woodCutting")
        val farming = result.getOrCreate<JSONArray>("woodCutting")
        val fishing = result.getOrCreate<JSONArray>("woodCutting")

        val spots = DatabaseManager.getAllGatheringSpots()
        if (spots.isEmpty()) return result

        for (spot in spots) {
            val obj = JSONObject()

            obj["type"] = spot.material
            obj["lastSeen"] = spot.lastSeen
            obj["reliability"] = spot.calculateReliability()
            obj["location"] = spot.getLocation()

            when (spot.type) {
                GatheringType.WOODCUTTING -> woodCutting.add(obj)
                GatheringType.MINING -> mining.add(obj)
                GatheringType.FARMING -> farming.add(obj)
                GatheringType.FISHING -> fishing.add(obj)
            }
        }

        return result
    }

}