package moe.yukoDotMoe.cuteanimegirl

import android.app.Application
import androidx.preference.PreferenceManager
import moe.yukoDotMoe.cuteanimegirl.service.VibratorService
import moe.yukoDotMoe.cuteanimegirl.storage.StorageManager
import org.osmdroid.config.Configuration

class MockGpsApp : Application() {
    companion object {
        lateinit var shared: MockGpsApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        shared = this
        StorageManager.initialise(this)
        VibratorService.initialise(this)

        // Initialize OSMDroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName
    }

}