package tv.nomercy.app.shared.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

data class DeviceAuthResponse(
    @SerializedName("device_code") val deviceCode: String,
    @SerializedName("user_code") val userCode: String,
    @SerializedName("verification_uri") val verificationUri: String,
    @SerializedName("verification_uri_complete") val verificationUriComplete: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("interval") val interval: Int
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("id_token") val idToken: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("error_description") val errorDescription: String?
)

class DeviceAuthClient(private val authUrl: String, private val clientId: String) {

    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun startDeviceFlow(): DeviceAuthResponse? = withContext(Dispatchers.IO) {
        val url = "$authUrl/realms/NoMercyTV/protocol/openid-connect/auth/device"
        val formBody = FormBody.Builder()
            .add("client_id", clientId)
            .add("scope", "openid profile email")
            .build()

        val request = Request.Builder().url(url).post(formBody).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseBody = response.body?.string()
                gson.fromJson(responseBody, DeviceAuthResponse::class.java)
            }
        } catch (e: IOException) {
            Log.e("DeviceAuthClient", "Error starting device flow", e)
            null
        }
    }

    suspend fun pollForToken(deviceCode: String): TokenResponse? = withContext(Dispatchers.IO) {
        val url = "$authUrl/realms/NoMercyTV/protocol/openid-connect/token"
        val formBody = FormBody.Builder()
            .add("client_id", clientId)
            .add("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
            .add("device_code", deviceCode)
            .build()

        val request = Request.Builder().url(url).post(formBody).build()

        try {
            client.newCall(request).execute().use { response ->
                // Don'''t throw for non-successful responses, as it can be part of the flow (e.g., authorization_pending)
                val responseBody = response.body?.string()
                gson.fromJson(responseBody, TokenResponse::class.java)
            }
        } catch (e: IOException) {
            Log.e("DeviceAuthClient", "Error polling for token", e)
            null
        }
    }
}