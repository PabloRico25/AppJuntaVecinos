package com.example.appjuntavecinos.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appjuntavecinos.ui.components.ContenidoCentrado
import com.example.appjuntavecinos.ui.screens.DetalleReservaScreen
import com.example.appjuntavecinos.ui.screens.DisponibilidadScreen
import com.example.appjuntavecinos.ui.screens.EscanearScreen
import com.example.appjuntavecinos.ui.screens.LoginScreen
import com.example.appjuntavecinos.ui.screens.MisReservasScreen
import com.example.appjuntavecinos.ui.screens.NuevaReservaScreen
import com.example.appjuntavecinos.ui.screens.PagosScreen
import com.example.appjuntavecinos.ui.screens.PuertaScreen
import com.example.appjuntavecinos.ui.screens.RegistrarPagoScreen
import com.example.appjuntavecinos.ui.screens.ReservasDirectivaScreen
import com.example.appjuntavecinos.ui.screens.ResumenScreen
import com.example.appjuntavecinos.ui.screens.TicketQrScreen
import com.example.appjuntavecinos.viewmodel.LoginViewModel
import com.example.appjuntavecinos.viewmodel.PuertaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val loginViewModel: LoginViewModel = viewModel()
    val puertaViewModel: PuertaViewModel = viewModel()
    val modoPuerta by puertaViewModel.modoPuerta.collectAsState()
    val sesion by loginViewModel.sesion.collectAsState()
    val destinos = if (sesion?.rol == "DIRECTIVA") destinosDirectiva else destinosVecino
    var menuAbierto by remember { mutableStateOf(false) }
    val avisos = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val anchoMaximo = if (rutaActual == Rutas.RESUMEN || rutaActual == Rutas.FINANZAS) Dp.Unspecified else 640.dp

    val contenido: @Composable () -> Unit = {
        NavHost(navController = navController, startDestination = Rutas.LOGIN) {
            composable(Rutas.LOGIN) {
                LoginScreen(
                    viewModel = loginViewModel,
                    puertaViewModel = puertaViewModel,
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
            composable(Rutas.MIS_RESERVAS) {
                MisReservasScreen(
                    vecinoId = sesion?.id ?: 0,
                    onNuevaReserva = { navController.navigate(Rutas.NUEVA_RESERVA) },
                    onDetalle = { id -> navController.navigate("detalle_reserva/$id") }
                )
            }
            // No visto en clase (lo integramos con permiso del docente): navArgument pasa el número de la reserva a la pantalla.
            composable(
                route = Rutas.DETALLE_RESERVA,
                arguments = listOf(navArgument("reservaId") { type = NavType.IntType })
            ) { entrada ->
                DetalleReservaScreen(
                    reservaId = entrada.arguments?.getInt("reservaId") ?: 0,
                    vecinoId = sesion?.id ?: 0,
                    onTicket = { id -> navController.navigate("ticket_qr/$id") }
                )
            }
            composable(
                route = Rutas.TICKET_QR,
                arguments = listOf(navArgument("reservaId") { type = NavType.IntType })
            ) { entrada ->
                TicketQrScreen(
                    reservaId = entrada.arguments?.getInt("reservaId") ?: 0,
                    vecinoId = sesion?.id ?: 0,
                    nombreVecino = sesion?.nombre ?: ""
                )
            }
            composable(Rutas.NUEVA_RESERVA) {
                NuevaReservaScreen(
                    vecinoId = sesion?.id ?: 0,
                    onReservaCreada = {
                        navController.popBackStack()
                        scope.launch { avisos.showSnackbar("Reserva solicitada. Queda pendiente de confirmación") }
                    }
                )
            }
            composable(Rutas.RESERVAS) {
                ReservasDirectivaScreen(
                    gestionadaPorId = sesion?.id ?: 0,
                    puedeConfirmar = loginViewModel.puedeConfirmarReservas(),
                    onAviso = { mensaje ->
                        scope.launch { avisos.showSnackbar(mensaje) }
                    }
                )
            }
            composable(Rutas.PAGOS) {
                PagosScreen(
                    puedeRegistrar = loginViewModel.puedeRegistrarPagos(),
                    onRegistrarPago = { navController.navigate(Rutas.REGISTRAR_PAGO) }
                )
            }
            composable(Rutas.ESCANEAR) {
                EscanearScreen()
            }
            composable(Rutas.RESUMEN) {
                ResumenScreen()
            }
            composable(Rutas.FINANZAS) {
                ResumenScreen()
            }
            composable(Rutas.DISPONIBILIDAD) {
                DisponibilidadScreen(
                    mostrarNombres = sesion?.rol == "DIRECTIVA"
                )
            }
            composable(Rutas.REGISTRAR_PAGO) {
                RegistrarPagoScreen(
                    registradoPorId = sesion?.id ?: 0,
                    puedeRegistrar = loginViewModel.puedeRegistrarPagos(),
                    onPagoRegistrado = {
                        navController.popBackStack()
                        scope.launch { avisos.showSnackbar("Pago registrado") }
                    }
                )
            }
        }
    }

    if (modoPuerta == null) {
        Box(modifier = Modifier.fillMaxSize())
    } else if (modoPuerta == true) {
        PuertaScreen(viewModel = puertaViewModel)
    } else if (rutaActual == Rutas.LOGIN || rutaActual == null) {
        ContenidoCentrado(anchoMaximo) { contenido() }
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
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(sesion?.nombre ?: "") },
                    actions = {
                        if (sesion?.rol == "DIRECTIVA" && rutaActual == Rutas.RESERVAS) {
                            IconButton(onClick = { navController.navigate(Rutas.DISPONIBILIDAD) }) {
                                Icon(Icons.Filled.DateRange, contentDescription = "Ver disponibilidad")
                            }
                        }
                        IconButton(onClick = { menuAbierto = true }) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "Menú de la cuenta")
                        }
                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    menuAbierto = false
                                    loginViewModel.cerrarSesion()
                                    navController.navigate(Rutas.LOGIN) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                )
                Box(modifier = Modifier.weight(1f)) {
                    ContenidoCentrado(anchoMaximo) { contenido() }
                    SnackbarHost(
                        hostState = avisos,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}