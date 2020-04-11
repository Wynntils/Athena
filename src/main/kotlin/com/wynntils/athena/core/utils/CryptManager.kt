package com.wynntils.athena.core.utils

import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object CryptManager {

    fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(1024)

        return generator.generateKeyPair()
    }

    fun decryptData(key: Key, data: ByteArray): ByteArray {
        return cryptData(2, key, data)
    }

    fun decryptSharedKey(private: PrivateKey, secretEncrypted: ByteArray): SecretKey {
        return SecretKeySpec(decryptData(private, secretEncrypted), "AES")
    }

    private fun createCipher(opMode: Int, algorithm: String, key: Key): Cipher {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(opMode, key)

        return cipher
    }

    private fun cryptData(opMode: Int, key: Key, data: ByteArray): ByteArray {
        return createCipher(opMode, key.algorithm, key).doFinal(data)
    }

}