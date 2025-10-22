package com.example.casinoapp.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Objeto encargado de generar, encriptar y verificar contraseñas.
 * Utiliza PBKDF2 con HmacSHA256, un algoritmo seguro para hashing de contraseñas.
 */
object PasswordHasher {
    private const val ITERATIONS = 120_000 // Iteraciones para fortalecer el hash.
    private const val KEY_LENGTH = 256 // Longitud del hash (bits).
    private const val SALT_LEN = 16    // Longitud del "salt" aleatorio.

    /**
     * Genera un salt aleatorio codificado en Base64.
     * El salt evita que hashes iguales se repitan entre usuarios.
     */
    fun generateSalt(): String {
        val bytes = ByteArray(SALT_LEN)
        SecureRandom().nextBytes(bytes) // Genera bytes aleatorios seguros.
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Crea un hash de la contraseña combinándola con un salt.
     * Retorna un Pair(hashCodificado, salt).
     */
    fun hash(password: String, salt: String = generateSalt()): Pair<String, String> {
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        // PBEKeySpec define cómo se derivará la clave (contraseña, salt, iteraciones, longitud)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded // Deriva el hash binario
        val hashStr = Base64.encodeToString(hash, Base64.NO_WRAP)
        return hashStr to salt
    }

    /**
     * Verifica si una contraseña ingresada coincide con el hash almacenado.
     * Se vuelve a calcular el hash con el mismo salt y se compara.
     */
    fun verify(password: String, storedHash: String, storedSalt: String): Boolean {
        val (calcHash, _) = hash(password, storedSalt)
        return calcHash == storedHash
    }
}
