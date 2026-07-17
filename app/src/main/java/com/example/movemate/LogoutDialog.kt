package com.example.movemate

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun LogoutWarningDialog(
    onConfirmLogout: () -> Unit,
    onCancelLogout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelLogout,
        title = {
            Text(text = "Logout")
        },
        text = {
            Text(text = "Are you sure you want to logout?")
        },
        confirmButton = {
            Button(
                onClick = onConfirmLogout
            ) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelLogout
            ) {
                Text(text = "Cancel")
            }
        }
    )
}
