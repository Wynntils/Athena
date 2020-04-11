package com.wynntils.athena.core.cache.annotations

annotation class CacheInfo(

    val name: String,
    val refreshRate: Int = 0, // in seconds, 0 if should not refresh
    val async: Boolean = true

)