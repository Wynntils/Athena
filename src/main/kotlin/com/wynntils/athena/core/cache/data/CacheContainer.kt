package com.wynntils.athena.core.cache.data

import com.wynntils.athena.core.cache.interfaces.DataCache

data class CacheContainer(

    val value: String,
    val hash: String,
    var nextRefresh: Long = 0,
    val holderReference: DataCache

)