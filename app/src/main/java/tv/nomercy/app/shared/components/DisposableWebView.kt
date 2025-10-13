package tv.nomercy.app.shared.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DisposableWebView(
    url: String,
    modifier: Modifier = Modifier,
    onBackEvent: () -> Unit = {},
    onPageFinished: (String?) -> Unit = {},
    onDispose: () -> Unit = {}
) {
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    val hasHandledBackEvent = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        // Setup WebView
        webView.apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(Color.BLACK)

            settings.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
                allowFileAccess = true
                domStorageEnabled = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setNeedInitialFocus(true)
            }

            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    // Optional fullscreen handling
                }

                override fun onHideCustomView() {
                    // Optional fullscreen handling
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    super.onPageFinished(view, url)
                    injectHistoryListener(view)
                    onPageFinished(url)
                }
            }

            addJavascriptInterface(object {
                @JavascriptInterface
                fun onHistoryBack() {
                    Handler(Looper.getMainLooper()).post {
                        if (!hasHandledBackEvent.value) {
                            hasHandledBackEvent.value = true
                            onBackEvent()
                        }
                    }
                }
            }, "Android")
        }

        webView.loadUrl(url)

        attachAndFixWebView(webView)

        onDispose {
            safelyDisposeWebView(webView)
            onDispose()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}

fun attachAndFixWebView(wv: WebView) {
    try { wv.onResume() } catch (_: Throwable) {}
    try { wv.resumeTimers() } catch (_: Throwable) {}

    wv.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    wv.setWillNotDraw(false)

    wv.isFocusable = true
    wv.isFocusableInTouchMode = true

    wv.post {
        try { wv.requestFocus(View.FOCUS_DOWN) } catch (_: Throwable) {}
        try { wv.requestFocusFromTouch() } catch (_: Throwable) {}
        try { (wv.parent as? ViewGroup)?.requestLayout() } catch (_: Throwable) {}
        try { wv.invalidate() } catch (_: Throwable) {}
        try { wv.postInvalidateOnAnimation() } catch (_: Throwable) {}
    }
}

fun safelyDisposeWebView(webView: WebView) {
    try {
        webView.apply {
            loadUrl("about:blank")
            stopLoading()
            removeAllViews()
            removeJavascriptInterface("Android")
            destroy()
            webChromeClient = null
            webViewClient = object : WebViewClient() {}
        }
    } catch (_: Throwable) {
        // Ignore disposal errors
    }
}

fun injectHistoryListener(webView: WebView) {
    val js = """
        (function() {
            function notifyAndroid() {
                try {
                    if (window.Android && typeof window.Android.onHistoryBack === 'function') {
                        window.Android.onHistoryBack();
                    }
                } catch (e) {}
            }

            // Override history.back
            try {
                const originalBack = window.history.back;
                window.history.back = function() {
                    notifyAndroid();
                    return originalBack.apply(window.history, arguments);
                };
            } catch (e) {}

            // Listen for navigation events
            try { window.addEventListener('popstate', notifyAndroid); } catch (e) {}
            try { window.addEventListener('hashchange', notifyAndroid); } catch (e) {}
        })();
    """.trimIndent()

    webView.evaluateJavascript(js, null)
}
