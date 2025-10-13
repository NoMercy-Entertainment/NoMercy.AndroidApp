package tv.nomercy.app.shared.api

import android.content.Context
import androidx.core.net.toUri
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher

object KeycloakConfig {

    fun getSuffix(): String {
        // For Android, we'll determine dev environment based on build type or other criteria
        // For now, using production environment. Can be made dynamic later.
        return "-dev" // or "-dev" for development
    }

    private fun getAuthBaseUrl(): String {
        val suffix = getSuffix()
        return "https://auth$suffix.nomercy.tv"
    }

    fun createAuthServiceConfig(): AuthorizationServiceConfiguration {
        val suffix = getSuffix()
        val baseUrl = "https://auth$suffix.nomercy.tv"

        return AuthorizationServiceConfiguration(
            "$baseUrl/realms/NoMercyTV/protocol/openid-connect/auth".toUri(), // authorization endpoint
            "$baseUrl/realms/NoMercyTV/protocol/openid-connect/token".toUri() // token endpoint
        )
    }

    fun createAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            createAuthServiceConfig(),
            "nomercy-ui", // clientId
            ResponseTypeValues.CODE,
            "tv.nomercy.app2://oauth".toUri() // redirectUri
        ).setScope("openid profile email")
            .build()
    }

    fun createAuthService(context: Context): AuthorizationService {
        val builder = AppAuthConfiguration.Builder()
        builder.setBrowserMatcher(
            BrowserAllowList(
                VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
                VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
            )
        )

        return AuthorizationService(context, builder.build())
    }

    fun isTv(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("android.software.leanback")
    }
}