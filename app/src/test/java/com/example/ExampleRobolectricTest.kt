package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.DeveloperConfig
import com.example.security.SecureVaultManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WatchAppTest {

    @Test
    fun verifyAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WATCH", appName)
    }

    @Test
    fun verifyDeveloperIdentityImmutable() {
        assertEquals("WATCH", DeveloperConfig.APP_NAME)
        assertEquals("Xadi Ahmed", DeveloperConfig.DEVELOPER_NAME)
        assertEquals("xadiahmed23@gmail.com", DeveloperConfig.DEVELOPER_EMAIL)
        assertEquals("01878864913", DeveloperConfig.DEVELOPER_PHONE)

        val verification = DeveloperConfig.verifyIntegrity()
        assertTrue(verification.isAuthentic)
        assertTrue(verification.sha256Digest.isNotEmpty())
    }

    @Test
    fun verifySecureVaultPinHashing() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vaultManager = SecureVaultManager(context)

        val setupSuccess = vaultManager.setVaultPin("1234")
        assertTrue(setupSuccess)
        assertTrue(vaultManager.isVaultSetup())

        assertTrue(vaultManager.verifyPin("1234"))
        assertFalse(vaultManager.verifyPin("9999"))
        assertEquals(1, vaultManager.getFailedAttempts())
    }
}
