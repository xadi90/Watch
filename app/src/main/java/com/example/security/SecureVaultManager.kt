package com.example.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Hardware-backed AES-256-GCM Vault Manager using Android KeyStore.
 * Securely encrypts media files into a private vault directory.
 */
class SecureVaultManager(private val context: Context) {

    private val keyStoreAlias = "WATCH_VAULT_AES_KEY"
    private val keyStoreType = "AndroidKeyStore"
    private val prefs = context.getSharedPreferences("watch_vault_secure_meta", Context.MODE_PRIVATE)

    private val vaultDir: File
        get() = File(context.filesDir, "secure_vault_encrypted").apply {
            if (!exists()) mkdirs()
        }

    init {
        ensureKeyStoreKey()
    }

    private fun ensureKeyStoreKey() {
        val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
        if (!keyStore.containsAlias(keyStoreAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                keyStoreType
            )
            val keyGenSpec = KeyGenParameterSpec.Builder(
                keyStoreAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()
            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
        return keyStore.getKey(keyStoreAlias, null) as SecretKey
    }

    fun isVaultSetup(): Boolean {
        return prefs.contains("pin_hash")
    }

    fun setVaultPin(pin: String): Boolean {
        if (pin.length < 4) return false
        val salt = "WATCH_SECURE_SALT_v1"
        val hash = sha256("$salt$pin")
        prefs.edit()
            .putString("pin_hash", hash)
            .putLong("created_at", System.currentTimeMillis())
            .putInt("failed_attempts", 0)
            .apply()
        return true
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString("pin_hash", null) ?: return false
        val salt = "WATCH_SECURE_SALT_v1"
        val hash = sha256("$salt$pin")
        val correct = hash == storedHash
        if (correct) {
            prefs.edit().putInt("failed_attempts", 0).apply()
        } else {
            val failed = prefs.getInt("failed_attempts", 0) + 1
            prefs.edit().putInt("failed_attempts", failed).apply()
        }
        return correct
    }

    fun getFailedAttempts(): Int = prefs.getInt("failed_attempts", 0)

    /**
     * Encrypts a source file into the private vault with AES-256-GCM.
     * The 12-byte IV is prepended to the output file.
     */
    fun encryptFileToVault(sourceFile: File, displayName: String, isVideo: Boolean): File? {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv // 12 bytes

            val encryptedName = "vault_${System.currentTimeMillis()}_${sourceFile.name}.enc"
            val destFile = File(vaultDir, encryptedName)

            FileOutputStream(destFile).use { fos ->
                fos.write(iv) // Write IV prefix
                CipherOutputStream(fos, cipher).use { cos ->
                    FileInputStream(sourceFile).use { fis ->
                        fis.copyTo(cos)
                    }
                }
            }

            // Save metadata entry
            saveVaultItemMetadata(
                encryptedName = encryptedName,
                originalName = displayName,
                durationMs = 0L,
                sizeBytes = sourceFile.length(),
                isVideo = isVideo
            )

            destFile
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decrypts a vault file to a temporary playback cache file for secure viewing.
     */
    fun decryptVaultFileForViewing(encryptedFile: File): File? {
        return try {
            val fis = FileInputStream(encryptedFile)
            val iv = ByteArray(12)
            val read = fis.read(iv)
            if (read != 12) {
                fis.close()
                return null
            }

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val tempFile = File.createTempFile("vault_view_", ".tmp", context.cacheDir)
            tempFile.deleteOnExit()

            CipherInputStream(fis, cipher).use { cis ->
                FileOutputStream(tempFile).use { fos ->
                    cis.copyTo(fos)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    fun listVaultItems(): List<com.example.model.VaultItem> {
        val rawSet = prefs.getStringSet("vault_items_meta", emptySet()) ?: emptySet()
        val items = mutableListOf<com.example.model.VaultItem>()
        for (raw in rawSet) {
            val parts = raw.split("|||")
            if (parts.size >= 7) {
                items.add(
                    com.example.model.VaultItem(
                        id = parts[0],
                        originalFileName = parts[1],
                        encryptedFileName = parts[2],
                        durationMs = parts[3].toLongOrNull() ?: 0L,
                        sizeBytes = parts[4].toLongOrNull() ?: 0L,
                        importDate = parts[5].toLongOrNull() ?: System.currentTimeMillis(),
                        isVideo = parts[6].toBoolean()
                    )
                )
            }
        }
        return items.sortedByDescending { it.importDate }
    }

    private fun saveVaultItemMetadata(
        encryptedName: String,
        originalName: String,
        durationMs: Long,
        sizeBytes: Long,
        isVideo: Boolean
    ) {
        val current = prefs.getStringSet("vault_items_meta", emptySet())?.toMutableSet() ?: mutableSetOf()
        val entry = "${System.currentTimeMillis()}|||$originalName|||$encryptedName|||$durationMs|||$sizeBytes|||${System.currentTimeMillis()}|||$isVideo"
        current.add(entry)
        prefs.edit().putStringSet("vault_items_meta", current).apply()
    }

    fun deleteVaultItem(item: com.example.model.VaultItem): Boolean {
        val file = File(vaultDir, item.encryptedFileName)
        if (file.exists()) file.delete()
        val current = prefs.getStringSet("vault_items_meta", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.removeAll { it.contains(item.encryptedFileName) }
        prefs.edit().putStringSet("vault_items_meta", current).apply()
        return true
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
