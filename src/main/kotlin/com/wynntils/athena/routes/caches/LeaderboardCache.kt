package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.utils.JSONOrderedObject
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets

@CacheInfo(name = "leaderboard", refreshRate = 3600)
class LeaderboardCache: DataCache {

    override fun generateCache(): JSONOrderedObject {
        val result = JSONOrderedObject()

        // wynn leaderboards
        val wynnWoodCutting = getLeaderBoard("solo", "woodcutting")["data"] as JSONArray
        val wynnMining = getLeaderBoard("solo", "mining")["data"] as JSONArray
        val wynnFishing = getLeaderBoard("solo", "fishing")["data"] as JSONArray
        val wynnFarming = getLeaderBoard("solo", "farming")["data"] as JSONArray
        val wynnAlchemism = getLeaderBoard("solo", "alchemism")["data"] as JSONArray
        val wynnArmouring = getLeaderBoard("solo", "armouring")["data"] as JSONArray
        val wynnCooking = getLeaderBoard("solo", "cooking")["data"] as JSONArray
        val wynnJeweling = getLeaderBoard("solo", "jeweling")["data"] as JSONArray
        val wynnScribing = getLeaderBoard("solo", "scribing")["data"] as JSONArray
        val wynnTailoring = getLeaderBoard("solo", "tailoring")["data"] as JSONArray
        val wynnWeaponSmithing = getLeaderBoard("solo", "weaponsmithing")["data"] as JSONArray
        val wynnWoodWorking = getLeaderBoard("solo", "woodworking")["data"] as JSONArray
        val wynnOverall = getLeaderBoard("overall", "all")["data"] as JSONArray

        for (i in 99 downTo 91) {
            val uWc = wynnWoodCutting[i] as JSONObject
            val uMi = wynnMining[i] as JSONObject
            val uFi = wynnFishing[i] as JSONObject
            val uFa = wynnFarming[i] as JSONObject
            val uAl = wynnAlchemism[i] as JSONObject
            val uAr = wynnArmouring[i] as JSONObject
            val uCo = wynnCooking[i] as JSONObject
            val uJe = wynnJeweling[i] as JSONObject
            val uSc = wynnScribing[i] as JSONObject
            val uTa = wynnTailoring[i] as JSONObject
            val uWe = wynnWeaponSmithing[i] as JSONObject
            val uWo = wynnWoodWorking[i] as JSONObject
            val uOv = wynnOverall[i] as JSONObject

            result.getOrCreate<JSONOrderedObject>(uWc["uuid"] as String)["WOODCUTTING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uMi["uuid"] as String)["MINING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uFi["uuid"] as String)["FISHING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uFa["uuid"] as String)["FARMING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uAl["uuid"] as String)["ACLEHMISM"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uAr["uuid"] as String)["ARMOURING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uCo["uuid"] as String)["COOKING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uJe["uuid"] as String)["JEWELING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uSc["uuid"] as String)["SCRIBING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uTa["uuid"] as String)["TAILORING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uWe["uuid"] as String)["WEAPONSMITHING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uWo["uuid"] as String)["WOODWORKING"] = uWc["pos"]
            result.getOrCreate<JSONOrderedObject>(uOv["uuid"] as String)["OVERALL"] = uWc["pos"]
        }

        return result
    }

    private fun getLeaderBoard(type: String, prof: String): JSONObject  {
        val connection = URL("${apiConfig.wynnLeaderboards}$type/$prof").openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.setRequestProperty("apikey", apiConfig.wynnApiToken)
        connection.readTimeout = 20000
        connection.connectTimeout = 20000

        return connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON();
    }

}