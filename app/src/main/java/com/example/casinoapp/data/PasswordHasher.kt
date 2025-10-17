package com.example.casinoapp.data.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256 // bits
    private const val ALGO = "PBKDF2WithHmacSHA256"

    fun generateSalt(bytes: Int = 16): String {
        val salt = ByteArray(bytes)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hash(password: CharArray, saltB64: String): String {
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance(ALGO)
        val key = skf.generateSecret(spec).encoded
        spec.clearPassword()
        return Base64.encodeToString(key, Base64.NO_WRAP)
    }

    fun verify(password: CharArray, saltB64: String, expectedHashB64: String): Boolean {
        val h = hash(password, saltB64)
        return constantTimeEquals(h, expectedHashB64)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val ba = a.toByteArray(); val bb = b.toByteArray()
        var diff = ba.size xor bb.size
        val len = minOf(ba.size, bb.size)
        for (i in 0 until len) diff = diff or (ba[i].toInt() xor bb[i].toInt())
        return diff == 0
    }
}
