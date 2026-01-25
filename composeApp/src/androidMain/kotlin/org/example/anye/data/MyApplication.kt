package org.example.anye.data

import android.app.Application
import androidx.room.Room
import org.example.anye.data.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Starte Koin aus commonMain
        initKoin {
            // Plattformspezifische Konfiguration hinzufügen
            androidLogger()
            androidContext(this@MyApplication)
        }

    }
}

