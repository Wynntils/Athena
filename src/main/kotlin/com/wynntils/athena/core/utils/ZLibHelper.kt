package com.wynntils.athena.core.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

object ZLibHelper {

    fun deflate(bytes: ByteArray): ByteArray {
        val byteOutput = ByteArrayOutputStream()
        val deflater = DeflaterOutputStream(byteOutput)

        deflater.write(bytes)
        deflater.flush()
        deflater.close()

        return byteOutput.toByteArray()
    }

    fun inflate(byte: ByteArray): ByteArray {
        val byteInput = ByteArrayInputStream(byte)
        val inflater = InflaterInputStream(byteInput)

        return inflater.readBytes()
    }

}