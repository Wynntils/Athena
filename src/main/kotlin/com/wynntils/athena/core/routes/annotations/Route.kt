package com.wynntils.athena.core.routes.annotations

import com.wynntils.athena.core.routes.enums.RouteType

/**
 * Represents the function Route information
 *
 * @param path the route path
 * @param type the route request type
 * @param errorCode the error code that you want to handle (only used while handling type ERROR!)
 * @param ignoreBasePath if the class BasePath annotation should be ignored
 */
annotation class Route(

    val path: String = "*",
    val type: RouteType,
    val errorCode: Int = 0,
    val ignoreBasePath: Boolean = false,
    val ignoreRateLimit: Boolean = false

)