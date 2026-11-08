package com.motosmart.launcher

import android.app.Application
import com.motosmart.launcher.data.applications.ApplicationsRepository
import com.motosmart.launcher.data.battery.BatteryRepository
import com.motosmart.launcher.data.location.LocationRepository
import com.motosmart.launcher.data.music.MediaSessionRepository
import com.motosmart.launcher.data.preferences.SettingsRepository
import com.motosmart.launcher.data.sensors.CompassRepository
import com.motosmart.launcher.data.weather.WeatherRepository

class AppContainer(application: Application) {
    val settings = SettingsRepository(application)
    val applications = ApplicationsRepository(application)
    val weather = WeatherRepository(application)
    val music = MediaSessionRepository(application)
    val location = LocationRepository(application)
    val compass = CompassRepository(application)
    val battery = BatteryRepository(application)
}

class MotoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
