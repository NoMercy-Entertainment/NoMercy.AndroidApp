package tv.nomercy.app.components

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
    onDispose: () -> Unit = {},
    onWebViewCreated: (WebView) -> Unit = {}
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
                override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    injectMediaSessionPolyfill(view)
                }

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

        // Notify caller about created WebView
        onWebViewCreated(webView)

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

// Add this function to inject MediaSession API simulation
fun injectMediaSessionPolyfill(webView: WebView) {
    val js = """
        (function() {
            if (window.navigator.mediaSession) {
                console.log("MediaSession API already exists");
                return;
            }

            // Create MediaMetadata constructor
            class MediaMetadata {
                constructor(metadata = {}) {
                    this.title = metadata.title || '';
                    this.artist = metadata.artist || '';
                    this.album = metadata.album || '';
                    this.artwork = metadata.artwork || [];
                }
            }

            // Create MediaSession implementation
            class MediaSession {
                constructor() {
                    this.metadata = null;
                    this.playbackState = 'none';
                    this._actionHandlers = new Map();
                    this._positionState = {
                        duration: 0,
                        playbackRate: 1.0,
                        position: 0
                    };
                }

                setActionHandler(action, handler) {
                    if (handler === null) {
                        this._actionHandlers.delete(action);
                    } else {
                        this._actionHandlers.set(action, handler);
                    }
                }

                setPositionState(state) {
                    this._positionState = {
                        duration: state.duration || 0,
                        playbackRate: state.playbackRate || 1.0,
                        position: state.position || 0
                    };
                }
            }

            // Create the mediaSession instance
            const mediaSession = new MediaSession();
            
            // Expose MediaMetadata globally
            window.MediaMetadata = MediaMetadata;
            
            // Expose mediaSession on navigator
            Object.defineProperty(window.navigator, 'mediaSession', {
                value: mediaSession,
                writable: false,
                configurable: false
            });

            console.log("MediaSession API polyfill injected successfully");
        })();
    """.trimIndent()

    webView.evaluateJavascript(js, null)
}