package com.section11.crossclip

import android.app.Application
import com.google.firebase.FirebaseApp
import com.section11.crossclip.framework.di.androidFrameworkModule
import com.section11.crossclip.framework.di.androidRepositoryModule
import com.section11.crossclip.framework.di.androidViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CrossClipAndroidApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        startKoin {
            androidContext(this@CrossClipAndroidApp)
            modules(
                androidViewModelModule,
                androidRepositoryModule,
                androidFrameworkModule
            )
        }
    }
}
