package com.wynntils.athena.core.cache.data

import com.wynntils.athena.core.cache.interfaces.DataCache
import org.json.simple.JSONAware

data class CacheContainer(

    val value: JSONAware,
    val hash: String,
    var nextRefresh: Long = 0,
    val holderReference: DataCache

)