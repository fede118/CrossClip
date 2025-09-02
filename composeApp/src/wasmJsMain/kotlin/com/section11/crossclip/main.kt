package com.section11.crossclip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import com.section11.crossclip.framework.di.commonModule
import com.section11.crossclip.framework.di.wasmJsFrameworkModule
import com.section11.crossclip.framework.di.wasmJsRepositoryModule
import com.section11.crossclip.ui.clipsList.ClipsWebListScreen
import com.section11.crossclip.ui.dimens.DefaultPadding
import com.section11.crossclip.ui.signIn.SignInWebScreen
import com.section11.crossclip.ui.theme.CrossClipTheme
import com.section11.crossclip.ui.viewmodel.MainViewModel
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnSignIn
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnSignOut
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiState
import kotlinx.browser.document
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        startKoin {
            modules(
                commonModule,
                wasmJsFrameworkModule,
                wasmJsRepositoryModule
            )
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        println("couldn't init KOIN: ${e.message}")
    }

    try {
        val mainViewModel: MainViewModel = get().get()
        ComposeViewport(document.body!!) {
            val uiState by mainViewModel.uiState.collectAsState()
            val shareUiState by mainViewModel.shareUiState.collectAsState()
            CrossClipWebApp(
                uiState = uiState,
                share = shareUiState,
                onUiEvent = mainViewModel::onUiEvent,
                onShareUiEvents = mainViewModel::onShareUiEvent
            )
        }

    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossClipWebApp(
    uiState: MainUiState,
    share: MainViewModel.ShareUiState,
    onUiEvent: (MainUiEvents) -> Unit,
    onShareUiEvents: (MainViewModel.ShareUiEvents) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    CrossClipTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF2563EB),
            secondary = Color(0xFF10B981),
            background = Color(0xFF18181B),
            surface = Color(0xFF23272F),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF8FAFC)
        ),
        snackbarHostState = snackbarHostState
    ) {
        Scaffold(
            modifier = Modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(DefaultPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DefaultPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CrossClip",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (uiState.user != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Welcome, ${uiState.user.displayName}",
                                        color = Color(0xFF6B7280),
                                        fontSize = 14.sp
                                    )

                                    Button(
                                        onClick = { onUiEvent(OnSignOut) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFEF4444)
                                        )
                                    ) {
                                        Text("Sign Out")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    when {
                        uiState.user == null -> {
                            SignInWebScreen(
                                onSignIn = { onUiEvent(OnSignIn) },
                                isLoading = uiState.isLoading
                            )
                        }

                        else -> {
                            ClipsWebListScreen(
                                mainUiState = uiState,
                                shareUiState = share,
                                onUiEvent = onUiEvent,
                                onShareUiEvents = onShareUiEvents
                            )
                        }
                    }
                }
            }
        }
    }
}
