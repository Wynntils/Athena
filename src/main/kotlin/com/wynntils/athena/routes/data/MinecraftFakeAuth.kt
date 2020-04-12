package com.wynntils.athena.routes.data

import com.wynntils.athena.core.configs.apiConfig
import com.wynntils.athena.core.data.GameProfile
import com.wynntils.athena.core.enums.Hash
import com.wynntils.athena.core.toPlainString
import com.wynntils.athena.core.utils.CryptManager
import com.wynntils.athena.errorLog
import com.wynntils.athena.gson
import java.math.BigInteger
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import javax.crypto.SecretKey
import javax.net.ssl.HttpsURLConnection
import javax.xml.bind.DatatypeConverter

class MinecraftFakeAuth {

    lateinit var keyPair: KeyPair

    init {
        try {
            keyPair = CryptManager.generateKeyPair()
        } catch (ex: Exception) {
            errorLog.warn("Caught an error while generating MC Auth:\n" + ex.toPlainString())
        }
    }

    /**
     * Converts the public key to string
     *
     * @return the public key converted to string
     */
    fun getPublicKey(): String {
        return DatatypeConverter.printHexBinary(keyPair.public.encoded).toLowerCase()
    }

    /**
     * Verifies if the provided username and key were validated through Mojang
     *
     * @param username the username
     * @param key the client shared key generated based on our public key
     * @return the user GameProfile if authenticated, null otherwise
     */
    fun getGameProfile(username: String, key: String): GameProfile? {
        try {
            val encrypted = DatatypeConverter.parseHexBinary(key) // converts the string to a ByteArray
            val sharedKey = CryptManager.decryptSharedKey(
                keyPair.private,
                encrypted
            ) // decrypts the client sent shared key using our private key

            val verificationKey = BigInteger(getServerHash(sharedKey)).toString(16) // converts the server hash to string
            val url = apiConfig.mojangAuth.format(username, verificationKey)

            val connection = URL(url).openConnection() as HttpsURLConnection // open connection to Mojang server api

            val result = connection.inputStream.toPlainString()
            connection.inputStream.close()
            if (!result.contains("{")) return null // just a simple verification to check if it's a valid json

            return gson.fromJson(result, GameProfile::class.java) // converts the json result to GameProfile
        } catch (ex: java.lang.Exception) {
            return null
        }
    }

    /**
     * Generates the Mojang server hash pattern
     *
     * @param key the client shared secret key
     * @return a byte array with the server hash
     */
    private fun getServerHash(key: SecretKey): ByteArray {
        val serverId = "".toByteArray(StandardCharsets.ISO_8859_1)

        return Hash.SHA1.digest(arrayOf(serverId, key.encoded, keyPair.public.encoded))
    }

}