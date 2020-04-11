package com.wynntils.athena.core.cache.interfaces

import com.wynntils.athena.core.utils.JSONOrderedObject

interface DataCache {

    fun generateCache(): JSONOrderedObject

}