package com.wynntils.athena.core.cache.data

import com.wynntils.athena.core.cache.interfaces.DataCache
import com.wynntils.athena.core.utils.JSONOrderedObject

data class CacheContainer(

    val value: JSONOrderedObject,
    val hash: String,
    var nextRefresh: Long = 0,
    val holderReference: DataCache

)