package com.mustafakoceerr.justrelax

import android.app.Application
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Reads the merged manifest, so permissions that libraries add are included. The app shows no ads and
 * the Play data safety form says the advertising ID is not collected; the manifest must agree.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ManifestPermissionsTest {

    @Test
    fun noAdvertisingPermissions() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val requested = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions.orEmpty().toSet()

        val advertising = requested intersect setOf(
            "com.google.android.gms.permission.AD_ID",
            "android.permission.ACCESS_ADSERVICES_AD_ID",
            "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
        )
        assertTrue(advertising.isEmpty(), "Advertising permissions in the manifest: $advertising")
    }
}
