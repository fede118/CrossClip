package com.section11.crossclip.framework.di

import com.section11.crossclip.data.repository.SharedStringsRepository
import com.section11.crossclip.data.repository.WasmJsFirebaseRepository
import org.koin.dsl.module

val wasmJsRepositoryModule = module {
    single<SharedStringsRepository> {
        WasmJsFirebaseRepository(
            firebaseAuth = get<FirebaseAuthProvider>().instance,
            googleAuthProvider = get<GoogleAuthProviderWrapper>().instance
        )
    }
}