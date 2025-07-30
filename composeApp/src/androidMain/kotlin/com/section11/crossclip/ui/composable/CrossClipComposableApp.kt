package com.section11.crossclip.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.section11.crossclip.domain.models.SharedString
import com.section11.crossclip.ui.dimens.DefaultPadding
import com.section11.crossclip.ui.theme.CrossClipTheme
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.DismissAddStringScreen
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnAddStringTapped
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnDeleteString
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnRefreshSharedStrings
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnSignIn
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossClipComposableApp(
    uiState: MainUiState,
    onUiEvent: (MainUiEvents) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    CrossClipTheme(
        snackbarHostState = snackbarHostState
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                CenterAlignedTopAppBar(
                    title = { Text("CrossClip") },
                    modifier = Modifier,
                    colors = TopAppBarDefaults.topAppBarColors().copy(
                        containerColor = colorScheme.primaryContainer,
                    )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = DefaultPadding)
                ) {
                    when {
                        uiState.user == null -> {
                            SignInScreen(
                                onSignIn = { onUiEvent(OnSignIn) },
                                isLoading = uiState.isLoading,
                                modifier = Modifier.fillMaxSize()

                            )
                        }

                        uiState.error != null -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Error: ${uiState.error}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (uiState.retryAction != null) {
                                    Spacer(modifier = Modifier.height(DefaultPadding))
                                    Button(onClick = uiState.retryAction) {
                                        Text("Retry")
                                    }
                                } else {
                                    Text("Please try again later.")
                                }
                            }
                        }

                        else -> {
                            SharedStringsListScreen(
                                sharedStrings = uiState.sharedStrings,
                                isLoading = uiState.isLoading,
                                onRefresh = { onUiEvent(OnRefreshSharedStrings) },
                                onDelete = { onUiEvent(OnDeleteString(it)) },
                                onAddTap = { onUiEvent(OnAddStringTapped) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (uiState.showAddScreen) {
                        BasicAlertDialog(
                            onDismissRequest = { onUiEvent(DismissAddStringScreen) },
                            properties = DialogProperties(dismissOnClickOutside = true)
                        ) {
                            AddCrossClipScreen(
                                Modifier.fillMaxSize()
                            ) {
                                onUiEvent(DismissAddStringScreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CrossClipComposableAppPreview(modifier: Modifier = Modifier) {
    Surface {
        CrossClipComposableApp(
            uiState = MainUiState(
                user = null,
                isLoading = false,
                error = null,
                sharedStrings = List(10) { SharedString(
                    id = it.toString(),
                    content = "Shared String $it",
                    timestamp = System.currentTimeMillis()
                ) },
                showAddScreen = false
            ),
            onUiEvent = { }
        )
    }
}
