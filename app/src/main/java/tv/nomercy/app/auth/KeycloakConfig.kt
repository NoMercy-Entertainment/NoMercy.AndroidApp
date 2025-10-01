package tv.nomercy.app.auth

import android.content.Context
import android.net.Uri
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher

object KeycloakConfig {
    
    private fun getSuffix(): String {
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
            Uri.parse("$baseUrl/realms/NoMercyTV/protocol/openid-connect/auth"), // authorization endpoint
            Uri.parse("$baseUrl/realms/NoMercyTV/protocol/openid-connect/token") // token endpoint
        )
    }
    
    fun createAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            createAuthServiceConfig(),
            "nomercy-ui", // clientId
            ResponseTypeValues.CODE,
            Uri.parse("tv.nomercy.app://oauth") // redirectUri
        ).setScope("openid profile email")
            .build()
    }
    
    fun createAuthService(context: Context): AuthorizationService {
        val builder = AppAuthConfiguration.Builder()
        builder.setBrowserMatcher(BrowserAllowList(
            VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
            VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
        ))
        
        return AuthorizationService(context, builder.build())
    }
}
