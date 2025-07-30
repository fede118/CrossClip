package com.section11.crossclip.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.credentials.CredentialManager
import com.section11.crossclip.CrossClipAndroidApp
import com.section11.crossclip.data.repository.AndroidFirebaseRepository
import com.section11.crossclip.ui.composable.CrossClipComposableApp
import com.section11.crossclip.ui.viewmodel.MainViewModel

class CrossClipActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firebaseFirestore = (application as CrossClipAndroidApp).firebaseFirestore
        val firebaseAuth = (application as CrossClipAndroidApp).firebaseAuth

        val viewModel = MainViewModel(
            AndroidFirebaseRepository(
                context = this,
                credentialManager = CredentialManager.create(this),
                firebaseAuth = firebaseAuth,
                firestore = firebaseFirestore,
            )
        )

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            CrossClipComposableApp(uiState, viewModel::onUiEvent)
        }
    }
}
