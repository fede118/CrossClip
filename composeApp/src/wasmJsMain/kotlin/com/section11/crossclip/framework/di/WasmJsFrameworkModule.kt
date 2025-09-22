package com.section11.crossclip.framework.di

import com.section11.crossclip.data.repository.FirebaseApp
import com.section11.crossclip.data.repository.FirebaseAuth
import com.section11.crossclip.data.repository.Firestore
import com.section11.crossclip.data.repository.GoogleAuthProvider
import org.koin.dsl.module

class FirebaseAuthProvider(val instance: FirebaseAuth)
class GoogleAuthProviderWrapper(val instance: GoogleAuthProvider)

@JsName("getWindow")
external fun getWindow(): WindowWithFirebase

// Window interface to access Firebase
external interface WindowWithFirebase : JsAny {
    val firebaseApi: FirebaseApi
}

// Firebase global object
external interface FirebaseApi : JsAny {
    val app: FirebaseApp
    val auth: FirebaseAuth
    val firestore: Firestore
    fun GoogleAuthProvider(): GoogleAuthProvider
}

val wasmJsFrameworkModule = module {
    single<GoogleAuthProviderWrapper> {
        GoogleAuthProviderWrapper(getWindow().firebaseApi.GoogleAuthProvider())
    }
    single<FirebaseAuthProvider> {
        FirebaseAuthProvider(getWindow().firebaseApi.auth)
    }
}