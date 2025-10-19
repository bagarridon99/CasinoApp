package com.example.casinoapp.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256 // bits
    private const val SALT_LEN = 16

    fun generateSalt(): String {
        val bytes = ByteArray(SALT_LEN)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(password: String, salt: String = generateSalt()): Pair<String, String> {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded
        val hashStr = Base64.encodeToString(hash, Base64.NO_WRAP)
        return hashStr to salt
    }

    fun verify(password: String, storedHash: String, storedSalt: String): Boolean {
        val (calcHash, _) = hash(password, storedSalt)
        return calcHash == storedHash
    }
}
