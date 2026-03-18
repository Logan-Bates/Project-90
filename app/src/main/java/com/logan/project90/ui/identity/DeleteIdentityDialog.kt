package com.logan.project90.ui.identity

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.logan.project90.core.util.ValidationMessages
import com.logan.project90.domain.model.Identity

@Composable
fun DeleteIdentityDialog(
    identity: Identity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Identity") },
        text = { Text("${identity.name}\n\n${ValidationMessages.deleteIdentityConfirm}") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
