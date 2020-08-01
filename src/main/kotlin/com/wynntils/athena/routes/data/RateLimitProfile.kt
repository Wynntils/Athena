package com.wynntils.athena.routes.data

import com.wynntils.athena.core.configs.rateLimitConfig
import com.wynntils.athena.core.currentTimeMillis

data class RateLimitProfile(

    var maxRequests: Int,

    var requests: Int = 0,
    var releaseTime: Long = currentTimeMillis() + rateLimitConfig.timeout,

    val rateLimited: Boolean = false
) {

    /**
     * Increase the requests amount and rate limit if needed
     *
     * @return if the user was Rate Limited
     */
    fun increaseRequest(): Boolean {
        requests++
        if (!rateLimited && requests > maxRequests) return true

        return false
    }

    /**
     * @return if this object can be released
     */
    fun canBeRemoved(): Boolean {
        return currentTimeMillis() > releaseTime
    }

}