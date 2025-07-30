package com.section11.crossclip.ui.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.section11.crossclip.data.repository.AndroidFirebaseRepository
import com.section11.crossclip.ui.viewmodel.ShareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCrossClipScreen(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    // todo move viewModel creation
    val viewModel = remember {
        ShareViewModel(
            AndroidFirebaseRepository(
                context = context,
                credentialManager = CredentialManager.create(context),
                firebaseAuth = FirebaseAuth.getInstance(),
                firestore = FirebaseFirestore.getInstance(),
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
