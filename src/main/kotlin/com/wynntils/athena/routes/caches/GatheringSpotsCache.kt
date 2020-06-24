package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.getOrCreate
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
        val mining = result.getOrCreate<JSONArray>("mining")
        val farming = result.getOrCreate<JSONArray>("farming")
        val fishing = result.getOrCreate<JSONArray>("fishing")

        val spots = DatabaseManager.getAllGatheringSpots()
        if (spots.isEmpty()) return result

        for (spot in spots) {
            val reliability = spot.calculateReliability()

            // delete if not reliable anymore
            if (spot.shouldRemove() || reliability == 0) {
                spot.asyncDelete()
                continue
            }

            // does not display nodes with less than 50 of reliability
            if (reliability < 50) continue

            val obj = JSONObject()

            obj["type"] = spot.material.toString()
            obj["lastSeen"] = spot.lastSeen
            obj["reliability"] = reliability

            val location = obj.getOrCreate<JSONOrderedObject>("location")
            location["x"] = spot.getLocation().x
            location["y"] = spot.getLocation().y
            location["z"] = spot.getLocation().z

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