package com.example.appjuntavecinos.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    var rol by remember { mutableStateOf("") }

    val destinos = if (rol == "DIRECTIVA") destinosDirectiva else destinosVecino

    val contenido: @Composable () -> Unit = {
        NavHost(navController = navController, startDestination = Rutas.LOGIN) {
            composable(Rutas.LOGIN) {
                LoginProvisional(
                    onEntrar = { rolElegido, rutaInicial ->
                        rol = rolElegido
                        navController.navigate(rutaInicial) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            (destinosVecino + destinosDirectiva).forEach { destino ->
                composable(destino.ruta) { PantallaProvisoria(destino.etiqueta) }
            }
        }
    }

    if (rutaActual == Rutas.LOGIN || rutaActual == null) {
        contenido()
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                destinos.forEach { destino ->
                    item(
                        icon = { Icon(destino.icono, contentDescription = destino.etiqueta) },
                        label = { Text(destino.etiqueta) },
                        selected = rutaActual == destino.ruta,
                        onClick = {
                            navController.navigate(destino.ruta) { launchSingleTop = true }
                        }
                    )
                }
            }
        ) {
            contenido()
        }
    }
}

@Composable
private fun LoginProvisional(onEntrar: (String, String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login (provisorio)")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onEntrar("VECINO", Rutas.MIS_RESERVAS) }) {
            Text("Entrar como vecino")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onEntrar("DIRECTIVA", Rutas.RESERVAS) }) {
            Text("Entrar como directiva")
        }
    }
}

@Composable
private fun PantallaProvisoria(titulo: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(titulo)
    }
}