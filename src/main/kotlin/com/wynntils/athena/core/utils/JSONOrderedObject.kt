/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package com.wynntils.athena.core.utils

import org.json.simple.JSONAware
import org.json.simple.JSONStreamAware
import org.json.simple.JSONValue
import java.io.IOException
import java.io.Writer

/**
 * A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
 *
 * @author FangYidong<fangyidong></fangyidong>@yahoo.com.cn>
 */
class JSONOrderedObject : LinkedHashMap<Any?, Any?>, MutableMap<Any?, Any?>, JSONAware,
    JSONStreamAware {
    constructor() : super()

    /**
     * Allows creation of a JSONObject from a Map. After that, both the
     * generated JSONObject and the Map can be modified independently.
     *
     * @param map
     */
    constructor(map: Map<*, *>?) : super(map)

    @Throws(IOException::class)
    override fun writeJSONString(out: Writer) {
        writeJSONString(this, out)
    }

    inline fun <reified T: JSONAware> getOrCreate(key: String): T {
        return getOrPut(key, { T::class.java.newInstance() }) as T
    }

    fun cleanNull() {
        val it = iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (next.value != null) continue

            it.remove()
        }
    }

    override fun toJSONString(): String {
        return toJSONString(this)
    }

    override fun toString(): String {
        return toJSONString()
    }

    companion object {
        private const val serialVersionUID = -503443796854799292L

        /**
         * Encode a map into JSON text and write it to out.
         * If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top level.
         *
         * @see org.json.simple.JSONValue.writeJSONString
         * @param map
         * @param out
         */
        @Throws(IOException::class)
        fun writeJSONString(map: Map<*, *>?, out: Writer) {
            if (map == null) {
                out.write("null")
                return
            }
            var first = true
            val iter: Iterator<*> = map.entries.iterator()
            out.write('{'.toInt())
            while (iter.hasNext()) {
                if (first) first = false else out.write(','.toInt())
                val entry = iter.next() as Map.Entry<*, *>
                out.write('\"'.toInt())
                out.write(escape(entry.key.toString()))
                out.write('\"'.toInt())
                out.write(':'.toInt())
                JSONValue.writeJSONString(entry.value, out)
            }
            out.write('}'.toInt())
        }

        /**
         * Convert a map to JSON text. The result is a JSON object.
         * If this map is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
         *
         * @see org.json.simple.JSONValue.toJSONString
         * @param map
         * @return JSON text, or "null" if map is null.
         */
        fun toJSONString(map: Map<*, *>?): String {
            if (map == null) return "null"
            val sb = StringBuffer()
            var first = true
            val iter: Iterator<*> = map.entries.iterator()
            sb.append('{')
            while (iter.hasNext()) {
                if (first) first = false else sb.append(',')
                val entry = iter.next() as Map.Entry<*, *>

                toJSONString(entry.key.toString(), entry.value, sb)
            }
            sb.append('}')
            return sb.toString()
        }

        private fun toJSONString(key: String?, value: Any?, sb: StringBuffer): String {
            sb.append('\"')
            if (key == null) sb.append("null") else escape(key, sb)
            sb.append('\"').append(':')
            sb.append(JSONValue.toJSONString(value))
            return sb.toString()
        }

        private fun escape(s: String, sb: StringBuffer) {
            for (element in s) {
                when (element) {
                    '"' -> sb.append("\\\"")
                    '\\' -> sb.append("\\\\")
                    '\b' -> sb.append("\\b")
                    '\n' -> sb.append("\\n")
                    '\r' -> sb.append("\\r")
                    '\t' -> sb.append("\\t")
                    '/' -> sb.append("\\/")
                    else ->                 //Reference: http://www.unicode.org/versions/Unicode5.1.0/
                        if (element in '\u0000'..'\u001F' || element in '\u007F'..'\u009F' || element in '\u2000'..'\u20FF') {
                            val ss = Integer.toHexString(element.toInt())
                            sb.append("\\u")
                            var k = 0
                            while (k < 4 - ss.length) {
                                sb.append('0')
                                k++
                            }
                            sb.append(ss.toUpperCase())
                        } else {
                            sb.append(element)
                        }
                }
            }
        }

        fun toString(key: String?, value: Any): String {
            val sb = StringBuffer()
            toJSONString(key, value, sb)
            return sb.toString()
        }

        /**
         * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
         * It's the same as JSONValue.escape() only for compatibility here.
         *
         * @see org.json.simple.JSONValue.escape
         * @param s
         * @return
         */
        fun escape(s: String?): String {
            return JSONValue.escape(s)
        }

    }

}