package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.database.enums.ProfessionType
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets

@CacheInfo(name = "leaderboard", refreshRate = 3600)
class LeaderboardCache: DataCache {

    override fun generateCache(): JSONOrderedObject {
        val result = JSONOrderedObject()

        fun generateProfile(input: Any?, profession: ProfessionType) {
            input as JSONObject
            val profile = result.getOrCreate<JSONOrderedObject>(input["uuid"] as String)
            profile["name"] = input["name"]
            profile["timePlayed"] = input["minPlayed"]

            val ranks = profile.getOrCreate<JSONOrderedObject>("ranks")
            ranks[profession.name] = input["pos"]
        }

        for (prof in ProfessionType.values()) {
            val output = getLeaderBoard(prof.leaderboard)["data"] as JSONArray

            for (i in 99 downTo 91) {
                generateProfile(output[i], prof)
            }
        }

        return result
    }

    private fun getLeaderBoard(leaderboard: String): JSONObject  {
        val connection = URL("${apiConfig.wynnLeaderboards}$leaderboard").openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.setRequestProperty("apikey", apiConfig.wynnApiToken)
        connection.readTimeout = 20000
        connection.connectTimeout = 20000

        return connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON();
    }

}