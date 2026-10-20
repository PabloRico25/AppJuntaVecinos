package com.example.appjuntavecinos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun IconoDeError() {
    Icon(
        imageVector = Icons.Filled.Warning,
        contentDescription = "Error",
        tint = MaterialTheme.colorScheme.error
    )
}