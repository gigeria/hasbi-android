package com.hasbi.transactions.list

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

/**
 * Undo delete snackbar — shows for 5 seconds with Undo action.
 *
 * If user taps Undo, transaction is restored.
 * If 5 seconds pass, delete is permanent (until 30-day purge).
 */
@Composable
fun UndoDeleteSnackbar(
    showUndo: Boolean,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showUndo) {
        LaunchedEffect(showUndo) {
            delay(5000)
            onDismiss()
        }
        Snackbar(
            action = {
                androidx.compose.material3.TextButton(onClick = {
                    onUndo()
                    onDismiss()
                }) {
                    androidx.compose.material3.Text("UNDO", color = androidx.compose.ui.graphics.Color.White)
                }
            },
            containerColor = androidx.compose.ui.graphics.Color(0xFF1F2937),
            contentColor = androidx.compose.ui.graphics.Color.White
        ) {
            androidx.compose.material3.Text("Transaction deleted")
        }
    }
}
