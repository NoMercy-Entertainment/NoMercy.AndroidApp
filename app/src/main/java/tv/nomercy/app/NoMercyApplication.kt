package tv.nomercy.app

import android.app.Application
enum class Platform { Mobile, TV }

class NoMercyApplication : Application() {

    companion object {
        private const val TAG = "NoMercyApplication"
    }

    override fun onCreate() {
        super.onCreate()
    }
}