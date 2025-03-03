package com.aravindprojects.passwordmanager.utils

import android.security.keystore.KeyProperties
import android.security.keystore.KeyGenParameterSpec
import android.util.Base64
import android.util.Log
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec

object CryptoUtils {
    private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "MyEncryptionKey"

    init {
        generateKeyIfNecessary()
    }

    private fun generateKeyIfNecessary() {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            try {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false) // Key persistence across app updates
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
                Log.d("CryptoUtils", "Generated new encryption key.")
            } catch (e: Exception) {
                Log.e("CryptoUtils", "Key generation failed: ${e.message}")
            }
        } else {
            Log.d("CryptoUtils", "Encryption key already exists.")
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val keyEntry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return keyEntry?.secretKey ?: throw IllegalStateException("Secret key not found in KeyStore")
    }

    fun encrypt(data: String): String {
        if (data.isEmpty()) return ""

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charset.forName("UTF-8")))

        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encryptedData: String): String {
        if (encryptedData.isEmpty()) return ""

        try {
            val decodedData = Base64.decode(encryptedData, Base64.NO_WRAP)
            if (decodedData.size < 17) {
                Log.e("CryptoUtils", "Invalid encrypted data length: ${decodedData.size}")
                throw IllegalArgumentException("Invalid encrypted data")
            }

            val iv = decodedData.sliceArray(0 until 16)
            val encryptedBytes = decodedData.sliceArray(16 until decodedData.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), IvParameterSpec(iv))

            return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("CryptoUtils", "Decryption failed: ${e.message}")
            throw e
        }
    }
}
