package com.wynntils.athena.core.cache.interfaces

import org.json.simple.JSONAware

interface DataCache {

    fun generateCache(): JSONAware

}