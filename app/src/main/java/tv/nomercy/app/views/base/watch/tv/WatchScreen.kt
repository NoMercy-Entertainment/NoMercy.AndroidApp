package tv.nomercy.app.views.base.watch.tv

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import tv.nomercy.app.MainActivity
import tv.nomercy.app.components.DisposableWebView
import tv.nomercy.app.shared.api.KeycloakConfig.getSuffix
import tv.nomercy.app.shared.stores.GlobalStores

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WatchScreen(type: String?, id: String?, navController: NavHostController) {

    val activity = LocalActivity.current as Activity
    val context = LocalContext.current

    DisposableEffect(activity) {
        (activity as? MainActivity)?.setImmersive(true)

        onDispose {
            (activity as? MainActivity)?.setImmersive(false)
        }
    }

    val authStore = GlobalStores.getAuthStore(context)
    val serverConfigStore = GlobalStores.getServerConfigStore(context)

    val accessToken = authStore.accessToken.collectAsState().value
    val currentServer = serverConfigStore.currentServer.collectAsState()

    val serverApiUrl = currentServer.value?.serverApiUrl
    val serverBaseUrl = currentServer.value?.serverBaseUrl
    val playlistUrl = serverApiUrl?.let { "$it$type/$id/watch" }

    var prefix = getSuffix().replace("-", "")
    if(prefix != "") {
        prefix += "."
    }

    val url = remember(type, id, serverBaseUrl, playlistUrl) {
        buildString {
            append("https://${prefix}nomercy.tv/player-embed?")
            append("serverBaseUrl=$serverBaseUrl")
            append("&playlistUrl=$playlistUrl")
            append("&accessToken=$accessToken")
            append("&tvMode=true")
        }
    }

    // remember a reference to the WebView created inside DisposableWebView
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    // handle Android back button: use WebView history if available, otherwise pop nav
    BackHandler {
        val webView = webViewRef.value
        webView?.evaluateJavascript("""
            (function() {
                nmplayer().emit('back-button');
            })();
        """.trimIndent(), null)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DisposableWebView(
            url = url,
            onBackEvent = {
                navController.popBackStack()
            },
            onPageFinished = { url -> Log.d("WebView", "Finished: $url") },
            onDispose = {
            },
            modifier = Modifier.fillMaxSize(),
            onWebViewCreated = { webView -> webViewRef.value = webView },
        )
    }
}
