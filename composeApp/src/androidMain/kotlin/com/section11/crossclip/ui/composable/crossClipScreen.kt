package com.section11.crossclip.ui.composable

import android.content.ClipData
import android.content.ClipDescription
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.section11.crossclip.domain.models.SharedString
import com.section11.crossclip.ui.dimens.DefaultPadding
import com.section11.crossclip.ui.theme.LocalSnackbarHostState
import com.section11.crossclip.ui.viewmodel.formatTimestamp
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    onSignIn: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign in to view your shared strings",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onSignIn) {
                Text("Sign in with Google")
            }
        }
    }
}

@Composable
fun SharedStringsListScreen(
    sharedStrings: List<SharedString>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit,
    onAddTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Clips (${sharedStrings.size})",
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = onRefresh) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sharedStrings) { sharedString ->
                SharedStringItem(
                    sharedString = sharedString,
                    onDelete = { onDelete(sharedString.id) },
                    Modifier.fillMaxWidth()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedStringItem(
    sharedString: SharedString,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHost = LocalSnackbarHostState.current

    Card(
        modifier = modifier.clickable {
            coroutineScope.launch {
                clipboardManager.setClipEntry(
                    ClipEntry(
                        ClipData(
                            "CrossClip Shared String",
                            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                            ClipData.Item(AnnotatedString(sharedString.content))
                        )
                    )
                )
                snackBarHost.showSnackbar("Copied to clipboard: ${sharedString.content.take(30)}...")
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(DefaultPadding)

        ) {
            Text(
                text = sharedString.content,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatTimestamp(sharedString.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sharedString.deviceInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
