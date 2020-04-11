package com.wynntils.athena.core.routes.enums

enum class RouteType(

    val isHTTPRequest: Boolean

) {

    GET(true),
    POST(true),
    MIDDLE_WARE(false),
    AFTER_WARE(false),
    ERROR(false)

}