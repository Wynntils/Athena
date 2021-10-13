package com.wynntils.athena.routes.caches

import com.wynntils.athena.core.asJSON
import com.wynntils.athena.core.cache.annotations.CacheInfo
import com.wynntils.athena.core.cache.exceptions.UnexpectedCacheResponse
import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.getOrCreate
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
        connection.setRequestProperty("apikey", apiConfig.wynnApiToken)
        connection.readTimeout = 20000
        connection.connectTimeout = 20000

        val input = connection.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON<JSONObject>()
        if (!input.containsKey("items")) throw UnexpectedCacheResponse()

        val changelog = URL(apiConfig.wynnItemChanges).openConnection()
        changelog.setRequestProperty("User-Agent", generalConfig.userAgent)
        changelog.readTimeout = 20000
        changelog.connectTimeout = 20000

        val changelogInput = changelog.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON<JSONObject>()
        val changes = changelogInput.getOrCreate<JSONObject>("changes")
        val newItems = changelogInput.getOrCreate<JSONArray>("items")

        val result = JSONOrderedObject()
        val items = result.getOrCreate<JSONArray>("items")

        val materialTypes = result.getOrCreate<JSONObject>("materialTypes")
        val translatedReferences = result.getOrCreate<JSONObject>("translatedReferences")

        val originalItems = input["items"] as JSONArray
        val itemsMap = HashMap<String, JSONOrderedObject>()
        for (i in originalItems) {
            val item = i as JSONObject

            // convert item and apply changes, if necessary
            val converted = ItemManager.convertItem(item)
            if (changes.containsKey(converted["displayName"])) {
                ItemManager.updateItem(converted, changes)
            }

            // store all item material types
            if (converted["itemInfo"] != null) {
                val itemInfo = converted["itemInfo"] as JSONOrderedObject
                val typeArray = materialTypes.getOrCreate<JSONArray>(itemInfo["type"] as String)

                val material = itemInfo["material"]
                if (material != null && !typeArray.contains(material)) typeArray.add(material)
            }

            if (item.containsKey("displayName")) translatedReferences[item["name"]] = item["displayName"]
            itemsMap[item["name"] as String] = converted
            items.add(converted)
        }

        // add new items from changelog
        for (i in newItems) {
            val item = i as JSONObject
            items.add(item)
        }

        val wynnBuilder = URL(apiConfig.wynnBuilderIDs).openConnection()
        wynnBuilder.setRequestProperty("User-Agent", generalConfig.userAgent)
        wynnBuilder.readTimeout = 20000
        wynnBuilder.connectTimeout = 20000

        val wynnBuilderInput = wynnBuilder.getInputStream().readBytes().toString(StandardCharsets.UTF_8).asJSON<JSONObject>()
        if (wynnBuilderInput.containsKey("items")) {
            val wynnBuilderItems = wynnBuilderInput["items"] as JSONArray
            for (i in wynnBuilderItems) {
                val wynnBuilderItem = i as JSONObject
                val item = itemsMap[wynnBuilderItem["name"] as String]
                item?.put("wynnBuilderID", wynnBuilderItem["id"])
            }
        }

        result["identificationOrder"] = ItemManager.getIdentificationOrder()
        result["internalIdentifications"] = ItemManager.getInternalIdentifications()
        result["majorIdentifications"] = ItemManager.getMajorIdentifications()

        return result
    }

}