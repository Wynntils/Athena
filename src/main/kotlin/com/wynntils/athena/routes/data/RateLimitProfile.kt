package com.wynntils.athena.routes.data

import com.wynntils.athena.core.configs.rateLimitConfig
import com.wynntils.athena.core.currentTimeMillis

data class RateLimitProfile(

    var requests: Int = 0,
    var lastRequest: Long = currentTimeMillis(),

    var rateLimited: Boolean = false

) {

    /**
     * Increase the requests amount and rate limit if needed
     *
     * @return if the user was Rate Limited
     */
    fun increaseRequest(): Boolean {
        if (currentTimeMillis() - lastRequest >= rateLimitConfig.timeout) {
            requests = 1
            rateLimited = false
            lastRequest = currentTimeMillis()
            return false
        }

        requests++
        if (rateLimited || requests < rateLimitConfig.user) return false

        rateLimited = true
        return true
    }

    /**
     * @return if this object can be released
     */
    fun canBeRemoved(): Boolean {
        return currentTimeMillis() - lastRequest >= rateLimitConfig.timeout
    }

}