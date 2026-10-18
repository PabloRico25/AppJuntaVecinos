package com.example.appjuntavecinos.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.appjuntavecinos.ui.screens.*
import com.example.appjuntavecinos.ui.theme.Gray
import com.example.appjuntavecinos.viewmodel.LoginViewModel
import com.example.appjuntavecinos.viewmodel.PuertaViewModel
import com.example.appjuntavecinos.ui.components.ActivarPuertaDialog
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    // Una sola instancia conserva la sesión al cambiar de pantalla o girar el teléfono.
    val loginViewModel: LoginViewModel = viewModel()
    val puertaViewModel: PuertaViewModel = viewModel()
    val configuracion by puertaViewModel.configuracion.collectAsState()
    val errorPuerta by puertaViewModel.error.collectAsState()
    var activarPuerta by remember { mutableStateOf(false) }
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    if (configuracion.cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (errorPuerta == null) CircularProgressIndicator() else Text(errorPuerta.orEmpty())
        }
        return
    }
    if (configuracion.modoPuerta) {
        PuertaScreen(puertaViewModel)
        return
    }
    val usuario by loginViewModel.sesion.collectAsState()
    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route ?: Rutas.LOGIN
    val directiva = usuario?.rol == "DIRECTIVA"
    val destinos = if (directiva) destinosDirectiva else destinosVecino
    var menuCuenta by remember { mutableStateOf(false) }
    val consultandoDesdeFormulario = ruta == Rutas.DISPONIBILIDAD &&
        navController.previousBackStackEntry?.destination?.route == Rutas.NUEVA_RESERVA
    val esRaiz = destinos.any { it.ruta == ruta } && !consultandoDesdeFormulario
    val titulo = when (ruta) {
        Rutas.NUEVA_RESERVA -> "Nueva reserva"
        Rutas.DETALLE_RESERVA -> "Detalle de reserva"
        Rutas.REGISTRAR_PAGO -> "Registrar pago"
        Rutas.TICKET_QR -> "Ticket QR"
        Rutas.DISPONIBILIDAD -> "Disponibilidad"
        else -> destinos.find { it.ruta == ruta }?.titulo ?: "Junta de Vecinos Zaror"
    }

    fun irAlLogin() {
        navController.navigate(Rutas.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Si Android termina el proceso, la sesión vuelve a estar cerrada.
    LaunchedEffect(usuario, ruta) {
        if (usuario == null && ruta != Rutas.LOGIN) irAlLogin()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(avisos) },
        topBar = {
            if (usuario != null) TopAppBar(
                title = { Text(titulo) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Gray),
                navigationIcon = {
                    if (!esRaiz) IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (directiva && ruta == Rutas.RESERVAS) IconButton(onClick = {
                        navController.navigate(Rutas.DISPONIBILIDAD)
                    }) { Icon(Icons.Default.DateRange, "Ver disponibilidad") }
                    Box {
                        IconButton(onClick = { menuCuenta = true }) { Icon(Icons.Default.Person, "Cuenta") }
                        DropdownMenu(expanded = menuCuenta, onDismissRequest = { menuCuenta = false }) {
                            DropdownMenuItem(text = { Text(usuario?.nombre.orEmpty()) }, onClick = {}, enabled = false)
                            DropdownMenuItem(text = { Text(if (directiva) usuario?.cargo.orEmpty() else "Vecino") },
                                onClick = {}, enabled = false)
                            DropdownMenuItem(text = { Text("Cerrar sesión") }, onClick = {
                                menuCuenta = false
                                loginViewModel.cerrarSesion()
                                irAlLogin()
                            })
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (usuario != null && esRaiz) NavigationBar(containerColor = Gray) {
                destinos.forEach { destino ->
                    NavigationBarItem(
                        selected = ruta == destino.ruta,
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        onClick = {
                            if (ruta != destino.ruta) navController.navigate(destino.ruta) {
                                popUpTo(if (directiva) Rutas.RESERVAS else Rutas.MIS_RESERVAS)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(destino.icono, null) },
                        label = { Text(destino.titulo) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Rutas.LOGIN, modifier = Modifier.padding(innerPadding)) {
            composable(Rutas.LOGIN) {
                LoginScreen(loginViewModel, onActivarPuerta = { puertaViewModel.limpiarError(); activarPuerta = true }, onLoginValido = {
                    navController.navigate(if (loginViewModel.sesion.value?.rol == "DIRECTIVA") Rutas.RESERVAS else Rutas.MIS_RESERVAS) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                })
            }
            composable(Rutas.MIS_RESERVAS) {
                usuario?.takeIf { it.rol == "VECINO" }?.let {
                    MisReservasScreen(it.id,
                        onNuevaReserva = { navController.navigate(Rutas.NUEVA_RESERVA) },
                        onDetalle = { id -> navController.navigate("detalle_reserva/$id") })
                }
            }
            composable(Rutas.NUEVA_RESERVA) {
                usuario?.takeIf { it.rol == "VECINO" }?.let {
                    NuevaReservaScreen(it.id,
                        onReservaCreada = { navController.popBackStack() },
                        onDisponibilidad = { navController.navigate(Rutas.DISPONIBILIDAD) })
                }
            }
            composable(Rutas.DETALLE_RESERVA, arguments = listOf(navArgument("reservaId") { type = NavType.IntType })) { entradaDetalle ->
                usuario?.takeIf { it.rol == "VECINO" }?.let {
                    DetalleReservaScreen(entradaDetalle.arguments?.getInt("reservaId") ?: 0, it.id,
                        onTicket = { id -> navController.navigate("ticket_qr/$id") })
                }
            }
            composable(Rutas.TICKET_QR, arguments = listOf(navArgument("reservaId") { type = NavType.IntType })) { entradaTicket ->
                usuario?.takeIf { it.rol == "VECINO" }?.let {
                    TicketQrScreen(entradaTicket.arguments?.getInt("reservaId") ?: 0, it.id, it.nombre)
                }
            }
            composable(Rutas.DISPONIBILIDAD) { if (usuario != null) DisponibilidadScreen(directiva) }
            composable(Rutas.FINANZAS) {
                if (usuario?.rol == "VECINO") ResumenFinancieroScreen(esDirectiva = false)
            }
            composable(Rutas.RESERVAS) {
                usuario?.takeIf { it.rol == "DIRECTIVA" }?.let {
                    ReservasDirectivaScreen(it, loginViewModel.puedeConfirmarReservas())
                }
            }
            composable(Rutas.RESUMEN) { if (directiva) ResumenFinancieroScreen(esDirectiva = true) }
            composable(Rutas.PAGOS) {
                if (directiva) PagosScreen(loginViewModel.puedeRegistrarPagos(),
                    onRegistrarPago = { navController.navigate(Rutas.REGISTRAR_PAGO) })
            }
            composable(Rutas.REGISTRAR_PAGO) {
                usuario?.takeIf { it.rol == "DIRECTIVA" }?.let {
                    RegistrarPagoScreen(it.id, loginViewModel.puedeRegistrarPagos(), onPagoRegistrado = {
                        navController.popBackStack()
                        alcance.launch { avisos.showSnackbar("Pago registrado") }
                    })
                }
            }
            composable(Rutas.ESCANEAR) {
                if (directiva) EscanearScreen()
            }
        }
    }
    if (activarPuerta) ActivarPuertaDialog(puertaViewModel) { activarPuerta = false }
}
