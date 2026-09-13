package br.com.ide

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre

@HiltAndroidApp
class IdeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        MapLibre.getInstance(this)
    }
}