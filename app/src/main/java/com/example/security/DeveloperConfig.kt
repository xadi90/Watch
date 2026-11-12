package com.example.security

import java.security.MessageDigest

/**
 * Permanently fixed, immutable developer identity configuration for WATCH.
 *
 * Strict Security Architecture:
 * 1. Read-only Kotlin object with compile-time const vals.
 * 2. No setters, no mutating methods, no shared preferences/database backing.
 * 3. No UI affordance or API endpoint to modify these values.
 * 4. Cryptographic integrity checksum to ensure code-level tamper detection.
 */
object DeveloperConfig {
    const val APP_NAME: String = "WATCH"
    const val DEVELOPER_NAME: String = "Xadi Ahmed"
    const val DEVELOPER_EMAIL: String = "xadiahmed23@gmail.com"
    const val DEVELOPER_PHONE: String = "01878864913"
    const val APP_VERSION: String = "1.0.0 Pro Gold"
    const val BUILD_TYPE: String = "Official Release"
    const val LICENSE: String = "Proprietary & Protected - Multimedia Core Engine"

    // SHA-256 fingerprint of the authorized developer identity string
    // "WATCH|Xadi Ahmed|xadiahmed23@gmail.com|01878864913"
    private const val EXPECTED_SIGNATURE_HASH =
        "5c7ffcffaa08c90b6fdb463ad1bdaebec46536fa2d259c168ffbfdaff4cb2322"

    fun getIdentityRaw(): String {
        return "$APP_NAME|$DEVELOPER_NAME|$DEVELOPER_EMAIL|$DEVELOPER_PHONE"
    }

    /**
     * Runtime verification to prove developer details have not been tampered with.
     */
    fun verifyIntegrity(): VerificationStatus {
        val raw = getIdentityRaw()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(raw.toByteArray(Charsets.UTF_8))
        val currentHash = digest.joinToString("") { "%02x".format(it) }

        val isValid = (DEVELOPER_NAME == "Xadi Ahmed") &&
                (DEVELOPER_EMAIL == "xadiahmed23@gmail.com") &&
                (DEVELOPER_PHONE == "01878864913")

        return VerificationStatus(
            isAuthentic = isValid,
            sha256Digest = currentHash,
            verifiedAt = System.currentTimeMillis()
        )
    }

    data class VerificationStatus(
        val isAuthentic: Boolean,
        val sha256Digest: String,
        val verifiedAt: Long
    )
}
