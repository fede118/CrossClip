package com.section11.crossclip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.ui.window.DialogProperties
import com.section11.crossclip.domain.models.SharedString
import com.section11.crossclip.data.repository.WasmJsFirebaseRepository
import com.section11.crossclip.ui.dimens.DefaultPadding
import com.section11.crossclip.ui.theme.CrossClipTheme
import com.section11.crossclip.ui.theme.LocalSnackbarHostState
import com.section11.crossclip.ui.viewmodel.MainViewModel
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.*
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiState
import com.section11.crossclip.ui.viewmodel.ShareViewModel
import com.section11.crossclip.ui.viewmodel.formatTimestamp
import crossclip.composeapp.generated.resources.Res
import crossclip.composeapp.generated.resources.delete_icon
import crossclip.composeapp.generated.resources.refresh_icon
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val viewModel = MainViewModel(WasmJsFirebaseRepository())

    ComposeViewport(document.body!!) {
        val uiState by viewModel.uiState.collectAsState()

        CrossClipWebApp(uiState, viewModel::onUiEvent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossClipWebApp(uiState: MainUiState, onUiEvent: (MainUiEvents) -> Unit) {
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
                                sharedStrings = uiState.sharedStrings,
                                isLoading = uiState.isLoading,

                                onRefresh = { onUiEvent(OnRefreshSharedStrings) },
                                onDelete = { onUiEvent(OnDeleteString(it)) },
                                onAddTap = { onUiEvent(OnAddStringTapped) }
                            )
                        }
                    }
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

@Composable
fun SignInWebScreen(
    onSignIn: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sign in to view your clips",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = onSignIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4)
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        "Sign in with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipsWebListScreen(
    sharedStrings: List<SharedString>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit,
    onAddTap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clips (${sharedStrings.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoading) Color(0xFF9CA3AF) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                painterResource(Res.drawable.refresh_icon),
                                contentDescription = "Refresh",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(if (isLoading) "Refreshing..." else "Refresh")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (sharedStrings.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No clips yet.",
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Share some text from your Android device to see it here!",
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sharedStrings) { sharedString ->
                        ClipWebItem(
                            sharedString = sharedString,
                            onDelete = { onDelete(sharedString.id) }
                        )
                    }
                    item {
                        Button(
                            modifier = Modifier.fillMaxWidth().padding(DefaultPadding),
                            onClick =  { onAddTap() }
                        ) { Text("Add shared string") }
                    }
                }
            }
        }
    }
}

@Composable
fun ClipWebItem(
    sharedString: SharedString,
    onDelete: () -> Unit
) {
    val snackBarHost = LocalSnackbarHostState.current
    val rememberCoroutine = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                window.navigator.clipboard.writeText(sharedString.content)
                rememberCoroutine.launch {
                    snackBarHost.showSnackbar("Copied to clipboard!")
                }
        },
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = sharedString.content,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatTimestamp(sharedString.timestamp),
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = sharedString.deviceInfo,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.delete_icon),
                        contentDescription = "Delete",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun AddCrossClipScreen(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    // TODO: move viewModel creation to DI framework
    val viewModel = remember {
        ShareViewModel(
            WasmJsFirebaseRepository(
            )
        )
    }
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.onDismiss) {
        onDismiss()
    }

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Save to Shared Strings",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.textToShare,
                    onValueChange = { viewModel.updateText(it) },
                    label = { Text("Text to share") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { viewModel.onCancel() }) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { viewModel.saveSharedString() },
                        enabled = !uiState.isLoading && uiState.textToShare.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
