package com.section11.crossclip.framework.di

import android.app.Activity
import androidx.credentials.CredentialManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module

val androidFrameworkModule = module {
    single<FirebaseApp> { FirebaseApp.getInstance() }
    single<FirebaseAuth> { FirebaseAuth.getInstance(get<FirebaseApp>()) }
    single<FirebaseFirestore> { FirebaseFirestore.getInstance(get<FirebaseApp>()) }
    single<CredentialManager> { (activity: Activity) -> CredentialManager.create(activity) }
}