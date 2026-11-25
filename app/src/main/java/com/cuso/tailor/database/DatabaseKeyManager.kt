package com.cuso.tailor.database

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import androidx.core.content.edit

object DatabaseKeyManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "cuso_room_db_master_key"
    private const val PREF_NAME = "cuso_db_sec_prefs"
    private const val PREF_ENC_KEY = "enc_db_key"
    private const val PREF_IV = "enc_db_iv"

    fun getPassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedKey = prefs.getString(PREF_ENC_KEY, null)
        val savedIv = prefs.getString(PREF_IV, null)

        if (savedKey != null && savedIv != null) {
            try {
                return decryptKey(
                    Base64.decode(savedKey, Base64.NO_WRAP),
                    Base64.decode(savedIv, Base64.NO_WRAP)
                )
            } catch (_: Exception) {
                // Keystore key corrupted/invalidated -> clear old key & DB so app won't crash loop
                prefs.edit { clear() }
                val dbFile = context.getDatabasePath("cusotailor_db")
                if (dbFile.exists()) {
                    dbFile.delete()
                    java.io.File("${dbFile.path}-wal").delete()
                    java.io.File("${dbFile.path}-shm").delete()
                    java.io.File("${dbFile.path}-journal").delete()
                }
            }
        }

        // First time: generate 32-byte random passphrase
        val rawKey = ByteArray(32)
        SecureRandom().nextBytes(rawKey)

        val (encrypted, iv) = encryptKey(rawKey)
        prefs.edit {
            putString(PREF_ENC_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(PREF_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
        }

        return rawKey
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun encryptKey(data: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateMasterKey())
        return cipher.doFinal(data) to cipher.iv
    }

    private fun decryptKey(encrypted: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateMasterKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted)
    }
}