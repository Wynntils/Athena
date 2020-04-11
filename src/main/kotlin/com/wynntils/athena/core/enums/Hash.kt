package com.wynntils.athena.core.enums

import java.security.MessageDigest

enum class Hash(
    private val algorithm: String
) {

    MD5("MD5"),
    SHA1("SHA1"),
    SHA256("SHA-256"),
    SHA512("SHA-512");

    fun digest(input: ByteArray): ByteArray {
        val md = MessageDigest.getInstance(algorithm)
        return md.digest(input)
    }

    fun digest(input: Array<ByteArray>): ByteArray {
        val md = MessageDigest.getInstance(algorithm)
        input.forEach { md.update(it) }

        return md.digest()
    }

    fun hash(input: ByteArray): String {
        return digest(input).fold("", { str, it -> str + "%02x".format(it) })
    }

}