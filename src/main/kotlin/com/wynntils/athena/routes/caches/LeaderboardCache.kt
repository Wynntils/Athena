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
        val wynnOverall = getLeaderBoard("overall", "all")["data"] as JSONArray

        val woodCutting = result.getOrCreate<JSONArray>("WOODCUTTING")
        val mining = result.getOrCreate<JSONArray>("MINING")
        val fishing = result.getOrCreate<JSONArray>("FISHING")
        val farming = result.getOrCreate<JSONArray>("FARMING")
        val alchemism = result.getOrCreate<JSONArray>("ALCHEMISM")
        val armouring = result.getOrCreate<JSONArray>("ARMOURING")
        val cooking = result.getOrCreate<JSONArray>("COOKING")
        val jeweling = result.getOrCreate<JSONArray>("JEWELING")
        val scribing = result.getOrCreate<JSONArray>("SCRIBING")
        val tailoring = result.getOrCreate<JSONArray>("TAILORING")
        val weaponSmithing = result.getOrCreate<JSONArray>("WEAPONSMITHING")
        val overall = result.getOrCreate<JSONArray>("OVERALL")

        for (i in 100 downTo 91) {
            woodCutting.add(generateProfile(wynnWoodCutting[i]))
            mining.add(generateProfile(wynnMining[i]))
            fishing.add(generateProfile(wynnFishing[i]))
            farming.add(generateProfile(wynnFarming[i]))
            alchemism.add(generateProfile(wynnAlchemism[i]))
            armouring.add(generateProfile(wynnArmouring[i]))
            cooking.add(generateProfile(wynnCooking[i]))
            jeweling.add(generateProfile(wynnJeweling[i]))
            scribing.add(generateProfile(wynnScribing[i]))
            tailoring.add(generateProfile(wynnTailoring[i]))
            weaponSmithing.add(generateProfile(wynnWeaponSmithing[i]))
            overall.add(generateProfile(wynnOverall[i]))
        }

        return result
    }

    private fun generateProfile(input: Any?): JSONOrderedObject {
        input as JSONObject
        val classObj = input["class"] as JSONObject

        val result = JSONOrderedObject()
        result["uuid"] = input["uuid"]
        result["name"] = input["name"]
        result["level"] = classObj["level"]
        result["xp"] = classObj["xp"]
        result["position"] = input["pos"]

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