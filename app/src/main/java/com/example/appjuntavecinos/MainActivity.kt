package com.example.appjuntavecinos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.appjuntavecinos.navigation.AppNavigation
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // pantalla moderna de Android 15
        setContent {
            AppJuntaVecinosTheme {Surface(modifier = Modifier.fillMaxSize(),// Surface pinta un fondo
                color = MaterialTheme.colorScheme.background) {AppNavigation()}
            }
        }
    }
}