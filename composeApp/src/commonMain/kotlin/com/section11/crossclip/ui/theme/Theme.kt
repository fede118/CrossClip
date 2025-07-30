package com.section11.crossclip.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided!")
}

@Composable
fun CrossClipTheme(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    content: @Composable () -> Unit
) {


    CompositionLocalProvider(
        LocalSnackbarHostState provides snackbarHostState
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}