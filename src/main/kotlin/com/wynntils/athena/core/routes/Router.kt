package com.wynntils.athena.core.routes

import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.generateRequestObject
import com.wynntils.athena.core.profiler.profile
import com.wynntils.athena.core.redirectTo
import com.wynntils.athena.core.routes.annotations.BasePath
import com.wynntils.athena.core.routes.annotations.Route
import com.wynntils.athena.core.routes.enums.RouteType
import com.wynntils.athena.core.toInputStream
import com.wynntils.athena.core.utils.Logger
import com.wynntils.athena.core.validIp
import com.wynntils.athena.errorLog
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.RedirectResponse
import org.json.simple.JSONObject
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

val routeLogger = Logger("routes", false)
private val executor = Executors.newFixedThreadPool(generalConfig.routeThreads)

fun Javalin.registerRoutes(clazz: KClass<*>) {
    val instance = clazz.createInstance()

    val basePath = clazz.findAnnotation<BasePath>()?.path ?: ""
    clazz.functions.forEach {
        val ann = it.findAnnotation<Route>() ?: return@forEach
        if (it.parameters.size != 2) return@forEach // 0 = instance, 1 = ctx -> 2 parameters

        fun triggerContext(ctx: Context, log: Boolean = true) {
            if (ann.type != RouteType.ERROR && (ctx.resultFuture() != null || ctx.resultStream() != null)) return // allow middlewares to block the output
            if (log) routeLogger.info("[${ann.type}] ${ctx.validIp() } -----> ${ann.path}")

            fun parseResponse(): InputStream? {
                try {
                    val result = it.call(instance, ctx) ?: return null

                    if (result is InputStream) return result
                    if (result is String) return ctx.toInputStream(result)
                    if (result !is JSONObject) return ctx.toInputStream(result.toString())

                    result["request"] = ctx.generateRequestObject()
                    return ctx.toInputStream(result.toString())
                } catch (rd: RedirectResponse) {
                    ctx.redirectTo(rd.message ?: generalConfig.fallbackUrl)
                    return null
                } catch (ex: Exception) {
                    ctx.status(200)

                    errorLog.exception("Caught an error while running route [${ann.type}] $basePath${ann.path}", ex)
                    return ctx.toInputStream("{\"message\":\"an error occurred while trying to get this route\"}")
                }
            }

            fun getAsyncResponse() = CompletableFuture<InputStream>().apply {
                executor.submit {
                    val response = parseResponse() ?: return@submit
                    complete(response)
                }
            }

            fun getSyncResponse() {
                val response = parseResponse() ?: return
                ctx.result(response)
            }

            profile("Routes-${ann.type}-${ann.path}") {
                if (ann.type.isHTTPRequest) ctx.result(getAsyncResponse())
                else getSyncResponse()
            }
        }

        val path = if (ann.ignoreBasePath) ann.path else "${basePath}${ann.path}"
        when (ann.type) {
            RouteType.GET -> get(path) { ctx -> triggerContext(ctx) }
            RouteType.POST -> post(path) { ctx -> triggerContext(ctx) }
            RouteType.MIDDLE_WARE -> before(path) { ctx -> triggerContext(ctx, false) }
            RouteType.AFTER_WARE -> after(path) { ctx -> triggerContext(ctx, false) }
            RouteType.ERROR -> error(ann.errorCode) { ctx -> triggerContext(ctx, false) }
        }
    }
}

fun Javalin.setupExceptions() {
    exception(Exception::class.java) { e, ctx ->
        run {
            errorLog.exception("Caught an error while running route ${ctx.fullUrl()}", e)
        }
    }
}