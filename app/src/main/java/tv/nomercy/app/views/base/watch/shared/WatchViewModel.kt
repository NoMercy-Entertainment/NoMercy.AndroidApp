package tv.nomercy.app.views.base.watch.shared

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Color
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.webkit.JavascriptInterface

@SuppressLint("SetJavaScriptEnabled")
class WatchViewModel(application: Application) : AndroidViewModel(application) {

//    var webViewRef by mutableStateOf<WebView?>(null)
//    val customViewCallback = mutableStateOf<CustomViewCallback?>(null)
//    val customView = mutableStateOf<View?>(null)
//
//    private val webView: WebView by lazy {
//        WebView(getApplication()).apply {
//            // Correct: call on the View, not on settings
//            setLayerType(View.LAYER_TYPE_HARDWARE, null)
//            setBackgroundColor(Color.BLACK)
//            // ensure view will draw
//            setWillNotDraw(false)
//
//            settings.apply {
//                javaScriptEnabled = true
//                mediaPlaybackRequiresUserGesture = false
//                allowFileAccess = true
//                domStorageEnabled = true
//                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//                useWideViewPort = true
//                loadWithOverviewMode = true
//                cacheMode = WebSettings.LOAD_DEFAULT
//                setNeedInitialFocus(true)
//            }
//
//            webChromeClient = object : WebChromeClient() {
//                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
//                    customView.value = view
//                    customViewCallback.value = callback
//                }
//
//                override fun onHideCustomView() {
//                    customView.value = null
//                    customViewCallback.value?.onCustomViewHidden()
//                    customViewCallback.value = null
//                }
//            }
//
//            webViewClient = object : WebViewClient() {
//                override fun onPageFinished(view: WebView, url: String?) {
//                    super.onPageFinished(view, url)
//                    val js = """
//                        (function(){
//
//                        })();
//                    """.trimIndent()
//
//                    view.evaluateJavascript(js, null)
//                }
//            }
//        }
//    }
//
//    fun getOrCreateWebView() = webView
//
//    fun onPlayerReady(webView: WebView) {
//        viewModelScope.launch {
//            // Optional: JS bridge or setup hooks
//        }
//    }
//
//    fun onDispose(webView: WebView) {
//        viewModelScope.launch {
//            webView.evaluateJavascript("nmplayer()?.dispose();") { _ -> }
//        }
//    }
//
//    fun emitBack(webView: WebView) {
//        viewModelScope.launch {
//            webView.evaluateJavascript("nmplayer()?.emit('back-button-hyjack');") { _ -> }
//        }
//    }
}

class WebAppInterface(private val onBackEvent: () -> Unit) {
//    @JavascriptInterface
//    fun onHistoryBack() {
//        onBackEvent()
//    }
}

@SuppressLint("SetJavaScriptEnabled")
fun installHistoryListener(webView: WebView, onBackEvent: () -> Unit) {
//    webView.settings.javaScriptEnabled = true
//    webView.settings.domStorageEnabled = true
//    // Add bridge without stomping other client logic: wrap existing client if needed
//    webView.addJavascriptInterface(WebAppInterface(onBackEvent), "Android")
//    // Inject JS in onPageFinished (this function sets a client, but ViewModel's client already injects too;
//    // if you set a client here, consider delegating to the previous client)
//    webView.webViewClient = object : WebViewClient() {
//        override fun onPageFinished(view: WebView, url: String?) {
//            super.onPageFinished(view, url)
//            val js = """
//                (function(){
//                  function notifyAndroid() {
//                    try { if (window.Android && window.Android.onHistoryBack) window.Android.onHistoryBack(); } catch(e){}
//                  }
//                  window.addEventListener('popstate', function(e){ notifyAndroid(); });
//                  window.addEventListener('hashchange', function(e){ notifyAndroid(); });
//                  (function(history){
//                    var push = history.pushState;
//                    var replace = history.replaceState;
//                    history.pushState = function(){
//                      var ret = push.apply(history, arguments);
//                      window.dispatchEvent(new Event('pushstate'));
//                      return ret;
//                    };
//                    history.replaceState = function(){
//                      var ret = replace.apply(history, arguments);
//                      window.dispatchEvent(new Event('replacestate'));
//                      return ret;
//                    };
//                  })(window.history);
//                  window.addEventListener('pushstate', notifyAndroid);
//                  window.addEventListener('replacestate', notifyAndroid);
//                })();
//            """.trimIndent()
//            view.evaluateJavascript(js, null)
//        }
//    }
}