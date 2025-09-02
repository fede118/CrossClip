package com.section11.crossclip.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.section11.crossclip.ui.composable.CrossClipComposableApp
import com.section11.crossclip.ui.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class CrossClipActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mainViewModel: MainViewModel = koinViewModel {
                parametersOf(this@CrossClipActivity)
            }
            val uiState by mainViewModel.uiState.collectAsState()
            val shareUiState by mainViewModel.shareUiState.collectAsState()

            CrossClipComposableApp(
                uiState,
                shareUiState,
                mainViewModel::onUiEvent,
                mainViewModel::onShareUiEvent
            )
        }
    }
}
