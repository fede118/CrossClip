package com.section11.crossclip

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrossClipAndroidApp : Application() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        val firebase = FirebaseApp.getInstance()
        firebaseAuth = FirebaseAuth.getInstance(firebase)
        firebaseFirestore = FirebaseFirestore.getInstance(firebase)
    }
}
