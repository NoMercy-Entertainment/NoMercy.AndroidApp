
package tv.nomercy.app.mobile.screens.base.watch

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tv.nomercy.app.mobile.entrypoint.MainActivity
import tv.nomercy.app.shared.stores.GlobalStores
import tv.nomercy.app.shared.ui.SystemUiController
import android.view.View
import androidx.core.view.WindowCompat

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WatchScreen(type: String?, id: String?, navController: NavHostController) {

    val viewModel: WatchViewModel = viewModel()
    val webView = viewModel.getOrCreateWebView()
    val activity = LocalActivity.current as Activity
    val context = LocalContext.current

    DisposableEffect(activity) {
        SystemUiController.lockOrientationLandscape(activity)
        (activity as? MainActivity)?.setImmersive(true)

        onDispose {
            SystemUiController.lockOrientationPortrait(activity)
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

    val url = remember(type, id, serverBaseUrl, playlistUrl) {
        buildString {
            append("https://dev.nomercy.tv/player-embed?")
            append("serverBaseUrl=$serverBaseUrl")
            append("&playlistUrl=$playlistUrl")
            append("&accessToken=$accessToken")
        }
    }

    fun attachAndFixWebView(wv: WebView) {
        try { wv.onResume() } catch (_: Throwable) {}
        try { wv.resumeTimers() } catch (_: Throwable) {}

        // ensure drawing and focus
        try { wv.setLayerType(View.LAYER_TYPE_HARDWARE, null) } catch (_: Throwable) {}
        try { wv.setWillNotDraw(false) } catch (_: Throwable) {}

        wv.isFocusable = true
        wv.isFocusableInTouchMode = true

        wv.post {
            try { wv.requestFocus(View.FOCUS_DOWN) } catch (_: Throwable) {}
            try { wv.requestFocusFromTouch() } catch (_: Throwable) {}
            try { (wv.parent as? ViewGroup)?.requestLayout() } catch (_: Throwable) {}
            try { wv.invalidate() } catch (_: Throwable) {}
            try { wv.postInvalidateOnAnimation() } catch (_: Throwable) {}
            // also invalidate the activity root to encourage redraw
            try { (activity.window?.decorView)?.post { activity.window?.decorView?.invalidate() } } catch (_: Throwable) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                (webView.parent as? ViewGroup)?.removeView(webView)

                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true

                // install history listener (note: it sets a WebViewClient; avoid clobbering if you also set one in VM)
                installHistoryListener(webView) {
                    navController.popBackStack()
                }

                attachAndFixWebView(webView)
                webView
            },
            update = { wv ->
                if (wv.url.isNullOrEmpty()) {
                    wv.loadUrl(url)
                } else {
                    attachAndFixWebView(wv)
                }
                viewModel.onPlayerReady(wv)
            },
            modifier = Modifier.fillMaxSize(),
        )
    }

    BackHandler {
        viewModel.emitBack(webView)
        viewModel.onDispose(webView)
        navController.popBackStack()
    }
}