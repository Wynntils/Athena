package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.asSimpleJson
import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.exceptions.UnexpectedCacheResponse
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.routes.managers.ItemManager
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets

@CacheInfo(name = "itemList", refreshRate = 86400)
class ItemListCache: DataCache {

    /**
     * Cache the rearranged version of the Wynncraft Items
     */
    override fun generateCache(): JSONOrderedObject {
        val connection = URL(apiConfig.wynnItems).openConnection()
        connection.setRequestProperty("User-Agent", generalConfig.userAgent)
        connection.readTimeout = 20000
        connection.connectTimeout = 20000

        val input = connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asSimpleJson<JSONObject>()
        if (!input.containsKey("items")) throw UnexpectedCacheResponse()

        val result = JSONOrderedObject()
        val items = result.getOrCreate<JSONArray>("items")

        val originalItems = input["items"] as JSONArray
        for (i in originalItems) {
            val item = i as JSONObject
            items.add(ItemManager.convertItem(item))
        }

        result["identificationOrder"] = ItemManager.getIdentificationOrder()

        return result
    }

}