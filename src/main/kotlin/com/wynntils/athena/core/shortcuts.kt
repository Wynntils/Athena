package com.wynntils.athena.core

import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.enums.AsciiColor
import io.javalin.core.util.Header
import io.javalin.http.Context
import org.json.simple.JSONAware
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.BufferedReader
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern
import javax.servlet.http.HttpServletResponse

val random = java.util.Random()

val fileDateFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm-ss")
val textDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
val logDateFormat = SimpleDateFormat("dd-MM-yyy")

private val executor: ExecutorService = Executors.newSingleThreadExecutor()

private val usernamePattern = Pattern.compile("[a-zA-Z0-9_]{1,16}")
private val uuidPattern = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")
private val userAgentPattern = Pattern.compile("(Chrome|Opera|Firefox|Safari|Edge)")
private val colorHexPattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})\$")

fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}

fun nanoTime(): Long {
    return System.nanoTime()
}

fun runAsync(runnable: () -> Unit) {
    executor.submit(runnable)
}

fun String.toDashedUUID(): UUID {
    return UUID.fromString(uuidPattern.matcher(this).replaceAll("$1-$2-$3-$4-$5"))
}

fun String.isMinecraftUsername(): Boolean {
    return usernamePattern.matcher(this).matches()
}

fun String.isColorHex(): Boolean {
    return colorHexPattern.matcher(this).matches()
}

inline fun <reified T : JSONAware> JSONObject.getOrCreate(key: String): T {
    return getOrPut(key) { T::class.java.getDeclaredConstructor().newInstance() } as T
}

fun <K : Any, V : Any?> HashMap<K, V>.cleanNull() {
    val it = iterator()
    while (it.hasNext()) {
        val next = it.next()
        if (next.value != null) continue

        it.remove()
    }
}

fun Exception.toPlainString(): String {
    val writer = StringWriter()
    val printer = PrintWriter(writer)

    printStackTrace(printer)
    return writer.toString()
}

// Context Stuff
fun Context.validIp(): String {
    if (headerMap().containsKey("X-Forwarded-For")) {
        val possibleIps = header("X-Forwarded-For")!!
        if (!possibleIps.contains(',')) return possibleIps

        return possibleIps.split(", ")[0]
    }

    return ip()
}

fun Context.generateRequestObject(): JSONObject {
    val request = JSONObject()
    request["ip"] = validIp()
    request["timeStamp"] = currentTimeMillis()

    return request
}

fun Context.redirectTo(location: String, responseCode: Int = HttpServletResponse.SC_MOVED_TEMPORARILY) {
    res.setHeader(Header.LOCATION, location)
    status(responseCode)
}

fun Context.toInputStream(input: String): InputStream {
    return input.byteInputStream(Charset.forName(res.characterEncoding))
}

fun Context.isAuthenticated(): Boolean {
    return generalConfig.apiKeys.contains(pathParam("apiKey"))
}

fun Context.isHuman(): Boolean {
    return userAgent() != null && userAgentPattern.matcher(userAgent().toString()).find()
}

// String and JSON Conversions

fun InputStream.toPlainString(): String {
    return bufferedReader(StandardCharsets.UTF_8).use(BufferedReader::readText)
}

inline fun <reified T : JSONAware> String.asJSON(): T {
    return JSONParser().parse(this) as T
}

// Math Stuff

fun maxLimit(max: Double, current: Double): Double {
    return if (current > max) max else current
}

fun maxLimit(max: Int, current: Int): Int {
    return if (current > max) max else current
}

fun printCoolLogo() {
    println(
        """
            ${AsciiColor.RED.ascii}
            [========================================] 
            [                 .'.                    ] Athena is the codename for the
            [                .oO;.                   ] Wynntils Account Management System.
            [                 ,KX0l                  ] 
            [                 'OMMK,                 ] It handles every single account,
            [                 :XMMX;                 ] socket connection, cape, configuration and more.
            [                ,0MMMO':c               ] every configuration and more.
            [               :KMMWXl'OK;              ] 
            [             'xNMMWx,,dWMd              ] This is not supposed to be run by third parties.
            [           .lKMMMWx;;xWMMO.             ] We (The Wynntils Team) are not going to help
            [          ;OWMMMXl:OWWMMMx. ;o.         ] you with setting up or with issues surrounding Athena.
            [        .oNMMMW0:cKMMMMMK;  :N0,        ] 
            [       .dWMMMWx,oNMMMMMK;   cWM0'       ] Athena is licensed over AGLP 3.0, check the
            [       ,KMMMWx'dWMMMMM0,   .kMMN:       ] repository license for more information.
            [       'OMMM0'cNMMMMMX;   .xWMMN:       ] 
            [        cNMMd.dMMMMMMXl.'c0WMMMO.       ] Copyright © Wynntils Team 2018 - 2020
            [         lXMx.oMMMMMMMWXNWMMMMX:        ] https://wynntils.com
            [          ;0K;'0MMMMMMMMMMMMMXc         ] https://github.com/Wynntils
            [           .ll.'xXMMMMMMMMMNx,          ] https://github.com/Wynntils/Athena
            [             ..  .:dxkOOkdc'            ]${AsciiColor.YELLOW.ascii} 
            [             Wynntils Athena            ] Use this at your own risk.
            [                  v3.0.0                ] And thanks for supporting Wynntils :)
            [========================================] 
            ${AsciiColor.RESET.ascii}""".trimIndent()
    )
}