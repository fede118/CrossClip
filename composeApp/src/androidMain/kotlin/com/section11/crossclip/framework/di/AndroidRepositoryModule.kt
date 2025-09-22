package com.section11.crossclip.framework.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.section11.crossclip.data.repository.AndroidFirebaseRepository
import com.section11.crossclip.data.repository.SharedStringsRepository
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val androidRepositoryModule = module {
    factory<SharedStringsRepository> { (activityContext: Context) ->
        AndroidFirebaseRepository(
            context = get<Context>(),
            credentialManager = get<CredentialManager> { parametersOf(activityContext) },
            firebaseAuth = get<FirebaseAuth>(),
            firestore = get<FirebaseFirestore>()
        )
    }
}