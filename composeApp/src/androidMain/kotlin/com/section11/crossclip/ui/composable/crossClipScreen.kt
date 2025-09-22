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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.section11.crossclip.R
import com.section11.crossclip.domain.models.SharedString
import com.section11.crossclip.ui.dimens.DefaultPadding
import com.section11.crossclip.ui.theme.CrossClipTheme
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
            text = stringResource(R.string.sign_in_prompt),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onSignIn) {
                Text(stringResource(R.string.sign_in_with_google))
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
                text = stringResource(R.string.your_clips_count, sharedStrings.size),
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = onRefresh) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh_content_description))
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
                ) { Text(stringResource(R.string.add_shared_string_button)) }
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

    val sharedStringLabel = stringResource(R.string.cross_clip_shared_string_label)
    val snackbarMessage = stringResource(R.string.copied_to_clipboard_message, sharedString.content.take(30))
    Card(
        modifier = modifier.clickable {
            coroutineScope.launch {
                clipboardManager.setClipEntry(
                    ClipEntry(
                        ClipData(
                            sharedStringLabel,
                            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                            ClipData.Item(AnnotatedString(sharedString.content))
                        )
                    )
                )
                snackBarHost.showSnackbar(snackbarMessage)
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
                Column(Modifier.weight(1f)) {
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
                        contentDescription = stringResource(R.string.delete_content_description),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_9")
@Composable
fun SharedStringsListScreenPreview_Empty() {
    CrossClipTheme {
        SharedStringsListScreen(
            sharedStrings = emptyList(),
            isLoading = false,
            onRefresh = {},
            onDelete = {},
            onAddTap = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SharedStringsListScreenPreview_WithItems() {
    CrossClipTheme {
        SharedStringsListScreen(
            sharedStrings = listOf(
                SharedString("1", "Hello", 1678886400000, "Android"),
                SharedString("2", "World", 1678886400000, "Android"),
                SharedString("3", "This is a longer string to see how it wraps", 1678886400000, "Desktop", deviceInfo = "Windows (Web)Mozilla/5.0 (Windows NT 10.0, Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36")
            ),
            isLoading = false,
            onRefresh = {},
            onDelete = {},
            onAddTap = {}
        )
    }
}
