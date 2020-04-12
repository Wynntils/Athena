package com.wynntils.athena.core

import com.google.gson.JsonElement
import com.wynntils.athena.core.configs.generalConfig
import com.wynntils.athena.core.enums.AsciiColor
import com.wynntils.athena.gson
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

val fileDateFormat = SimpleDateFormat("dd-MM-yyyy_hh-mm-ss")
val textDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")

private val executor: ExecutorService = Executors.newSingleThreadExecutor()

private val usernamePattern = Pattern.compile("[a-zA-Z0-9_]{1,16}")
private val uuidPattern = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")
private val userAgentPattern = Pattern.compile("(Chrome|Opera|Firefox|Safari|Edge)")

fun currentTimeMillis():Long {
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

inline fun <reified T: JSONAware> JSONObject.getOrCreate(key: String): T {
    return getOrPut(key, { T::class.java.newInstance() }) as T
}

fun <K: Any, V: Any?> HashMap<K, V>.cleanNull() {
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

fun Context.validIp(): String {
    return if (headerMap().containsKey("X-Real-IP")) header("X-Real-IP")!! // proxies use this header
    else ip()
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

fun InputStream.toPlainString(): String {
    return bufferedReader(StandardCharsets.UTF_8).use(BufferedReader::readText)
}

inline fun <reified T: JsonElement> String.asJson(): T {
    return gson.fromJson(this, T::class.java)
}

inline fun <reified T: JSONAware> String.asSimpleJson(): T {
    return JSONParser().parse(this) as T
}

fun printCoolLogo() {
    println(AsciiColor.RED.ascii + "\n" +
        "[========================================] \n" +
        "[                 .'.                    ] Athena is the codename for the\n" +
        "[                .oO;.                   ] Wynntils Account Management System.\n" +
        "[                 ,KX0l                  ] \n" +
        "[                 'OMMK,                 ] It handles every single account,\n" +
        "[                 :XMMX;                 ] socket connection, cape, configuration and more.\n" +
        "[                ,0MMMO':c               ] every configuration and more.\n" +
        "[               :KMMWXl'OK;              ] \n" +
        "[             'xNMMWx,,dWMd              ] This is not suspposed to be run by third parties.\n" +
        "[           .lKMMMWx;;xWMMO.             ] We (The Wynntils Team) are not going to help\n" +
        "[          ;OWMMMXl:OWWMMMx. ;o.         ] you with setting up or with issues surrounding Athena.\n" +
        "[        .oNMMMW0:cKMMMMMK;  :N0,        ] \n" +
        "[       .dWMMMWx,oNMMMMMK;   cWM0'       ] Athena is licensed over AGLP 3.0, check the\n" +
        "[       ,KMMMWx'dWMMMMM0,   .kMMN:       ] repository license for more information.\n" +
        "[       'OMMM0'cNMMMMMX;   .xWMMN:       ] \n" +
        "[        cNMMd.dMMMMMMXl.'c0WMMMO.       ] Copyright © Wynntils Team 2018 - 2020\n" +
        "[         lXMx.oMMMMMMMWXNWMMMMX:        ] https://wynntils.com\n" +
        "[          ;0K;'0MMMMMMMMMMMMMXc         ] https://github.com/Wynntils\n" +
        "[           .ll.'xXMMMMMMMMMNx,          ] https://github.com/Wynntils/Athena\n" +
        "[             ..  .:dxkOOkdc'            ] \n" + AsciiColor.YELLOW.ascii +
        "[             Wynntils Athena            ] Use this at your own risk.\n" +
        "[                  v2.0.0                ] And thanks for supporting Wynntils :)\n" +
        "[========================================] \n" + AsciiColor.RESET.ascii
    )
}