package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.exceptions.UnexpectedCacheResponse
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.routes.managers.IngredientManager
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets

@CacheInfo(name = "ingredientList", refreshRate = 86400)
class IngredientListCache : DataCache {

    /**
     * Cache the rearranged version of the Wynncraft Ingredients
     */
    override fun generateCache(): JSONOrderedObject {
        val connection = URL(apiConfig.wynnIngredients).openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.setRequestProperty("apikey", apiConfig.wynnApiToken)
        connection.readTimeout = 20000
        connection.connectTimeout = 20000

        val input = connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON<JSONObject>()
        if (!input.containsKey("data")) throw UnexpectedCacheResponse()

        val result = JSONOrderedObject()
        val items = result.getOrCreate<JSONArray>("ingredients")

        val originalIngredients = input["data"] as JSONArray
        for (i in originalIngredients) {
            val ingredient = i as JSONObject

            // convert and add
            val converted = IngredientManager.convertIngredient(ingredient)
            items.add(converted)
        }

        result["headTextures"] = IngredientManager.getHeadTextures()

        return result
    }

}