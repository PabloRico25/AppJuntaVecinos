package com.example.appjuntavecinos.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appjuntavecinos.ui.screens.LoginScreen
import com.example.appjuntavecinos.viewmodel.LoginViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val loginViewModel: LoginViewModel = viewModel()
    val sesion by loginViewModel.sesion.collectAsState()
    val destinos = if (sesion?.rol == "DIRECTIVA") destinosDirectiva else destinosVecino

    val contenido: @Composable () -> Unit = {
        NavHost(navController = navController, startDestination = Rutas.LOGIN) {
            composable(Rutas.LOGIN) {
                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginValido = {
                        val rutaInicial = if (loginViewModel.sesion.value?.rol == "DIRECTIVA") {
                            Rutas.RESERVAS
                        } else {
                            Rutas.MIS_RESERVAS
                        }
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
private fun PantallaProvisoria(titulo: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(titulo)
    }
}