package com.example.appjuntavecinos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.appjuntavecinos.navigation.AppNavigation
import com.example.appjuntavecinos.ui.theme.AppJuntaVecinosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppJuntaVecinosTheme {
                AppNavigation()
            }
        }
    }
}
