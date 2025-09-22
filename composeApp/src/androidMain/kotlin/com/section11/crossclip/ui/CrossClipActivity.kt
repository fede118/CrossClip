package com.section11.crossclip.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.section11.crossclip.ui.composable.CrossClipComposableApp
import com.section11.crossclip.ui.viewmodel.MainViewModel
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnIntentWithString
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val TEXT_PLANE_TYPE = "text/plain"

class CrossClipActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            mainViewModel = koinViewModel {
                parametersOf(this@CrossClipActivity)
            }
            val uiState by mainViewModel.uiState.collectAsState()
            val shareUiState by mainViewModel.shareUiState.collectAsState()

            LaunchedEffect(Unit) {
                handleIntent(intent)
            }

            CrossClipComposableApp(
                uiState,
                shareUiState,
                mainViewModel::onUiEvent,
                mainViewModel::onShareUiEvent
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (::mainViewModel.isInitialized) {
            if (intent?.action == Intent.ACTION_SEND && intent.type == TEXT_PLANE_TYPE) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                    mainViewModel.onUiEvent(OnIntentWithString(sharedText))
                }
            }
        }
    }
}
