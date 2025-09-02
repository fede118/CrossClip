package com.section11.crossclip.ui.clipsList

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.section11.crossclip.domain.models.SharedString
import com.section11.crossclip.ui.dimens.DefaultPadding
import com.section11.crossclip.ui.theme.LocalSnackbarHostState
import com.section11.crossclip.ui.viewmodel.MainViewModel
import com.section11.crossclip.ui.viewmodel.MainViewModel.MainUiEvents.OnDeleteString
import com.section11.crossclip.ui.viewmodel.MainViewModel.ShareUiEvents.OnDismiss
import com.section11.crossclip.ui.viewmodel.MainViewModel.ShareUiEvents.OnSaveTap
import com.section11.crossclip.ui.viewmodel.MainViewModel.ShareUiEvents.OnTextChange
import com.section11.crossclip.ui.viewmodel.formatTimestamp
import crossclip.composeapp.generated.resources.Res
import crossclip.composeapp.generated.resources.delete_icon
import crossclip.composeapp.generated.resources.refresh_icon
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipsWebListScreen(
    mainUiState: MainViewModel.MainUiState,
    shareUiState: MainViewModel.ShareUiState,
    onUiEvent: (MainViewModel.MainUiEvents) -> Unit,
    onShareUiEvents: (MainViewModel.ShareUiEvents) -> Unit,
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
                    text = "Clips (${mainUiState.sharedStrings.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = { onUiEvent(MainViewModel.MainUiEvents.OnRefreshSharedStrings) },
                    enabled = !mainUiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mainUiState.isLoading) Color(0xFF9CA3AF) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (mainUiState.isLoading) {
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
                        Text(if (mainUiState.isLoading) "Refreshing..." else "Refresh")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (mainUiState.sharedStrings.isEmpty() && !mainUiState.isLoading) {
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
                    verticalArrangement = Arrangement.spacedBy(DefaultPadding)
                ) {
                    items(mainUiState.sharedStrings) { sharedString ->
                        ClipWebItem(
                            sharedString = sharedString,
                            onDelete = { onUiEvent(OnDeleteString(sharedString.id))
                            }
                        )
                    }
                    item {
                        AddItemComposable(shareUiState, Modifier.padding(24.dp), onShareUiEvents)
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
fun AddItemComposable(
    shareUiState: MainViewModel.ShareUiState,
    modifier: Modifier = Modifier,
    onShareUiEvents: (MainViewModel.ShareUiEvents) -> Unit
) {
    Column(
        modifier = modifier,
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
                    value = shareUiState.textToShare,
                    onValueChange = { onShareUiEvents(OnTextChange(it)) },
                    label = { Text("Text to share") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { onShareUiEvents(OnDismiss) }) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onShareUiEvents(OnSaveTap) },
                        enabled = !shareUiState.isLoading && shareUiState.textToShare.isNotBlank()
                    ) {
                        if (shareUiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }

                if (shareUiState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = shareUiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}