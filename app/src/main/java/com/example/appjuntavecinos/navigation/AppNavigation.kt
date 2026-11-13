package com.example.appjuntavecinos.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appjuntavecinos.data.remote.EventosSesion
import com.example.appjuntavecinos.ui.components.CabeceraSesion
import com.example.appjuntavecinos.ui.components.ContenidoCentrado
import com.example.appjuntavecinos.ui.components.animacionesActivas
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
import com.example.appjuntavecinos.ui.screens.SesionVencidaScreen
import com.example.appjuntavecinos.ui.screens.SolicitudEnviadaScreen
import com.example.appjuntavecinos.ui.screens.TicketQrScreen
import com.example.appjuntavecinos.utils.nombreDelCargo
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

    // Si el servidor rechaza la sesión en cualquier pantalla, se cierra y se lleva a la pantalla de sesión vencida.
    // La puerta no cuenta: no tiene una sesión de persona.
    LaunchedEffect(Unit) {
        EventosSesion.vencida.collect {
            if (loginViewModel.sesion.value != null && puertaViewModel.modoPuerta.value != true) {
                loginViewModel.cerrarSesion()
                navController.navigate(Rutas.SESION_VENCIDA) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }
    }

    // Pantallas que se abren encima de otra: llevan flecha para volver.
    val rutasInternas = listOf(
        Rutas.NUEVA_RESERVA,
        Rutas.DETALLE_RESERVA,
        Rutas.TICKET_QR,
        Rutas.REGISTRAR_PAGO
    )
    // La Disponibilidad solo es interna para la directiva; para el vecino es una pestaña.
    val esPantallaInterna = rutaActual in rutasInternas ||
            (rutaActual == Rutas.DISPONIBILIDAD && sesion?.rol == "DIRECTIVA")

    // Pestaña de la barra que queda marcada cuando se está en una pantalla interna.
    val rutaDePestana = when (rutaActual) {
        Rutas.NUEVA_RESERVA, Rutas.DETALLE_RESERVA, Rutas.TICKET_QR, Rutas.SOLICITUD_ENVIADA -> Rutas.MIS_RESERVAS
        Rutas.REGISTRAR_PAGO -> Rutas.PAGOS
        Rutas.DISPONIBILIDAD -> if (sesion?.rol == "DIRECTIVA") Rutas.RESERVAS else Rutas.DISPONIBILIDAD
        else -> rutaActual
    }

    // Movimiento entre pantallas (ver Transiciones.kt). Con "Reducir animaciones" no hay desplazamiento.
    val animar = animacionesActivas()
    val desplazamiento = with(LocalDensity.current) { 24.dp.roundToPx() }

    val contenido: @Composable () -> Unit = {
        NavHost(
            navController = navController,
            startDestination = Rutas.LOGIN,
            enterTransition = {
                entradaAlAvanzar(tipoAlAvanzar(initialState.destination.route, targetState.destination.route), animar, desplazamiento)
            },
            exitTransition = {
                salidaAlAvanzar(tipoAlAvanzar(initialState.destination.route, targetState.destination.route), animar, desplazamiento)
            },
            popEnterTransition = {
                entradaAlVolver(tipoAlVolver(initialState.destination.route, targetState.destination.route), animar, desplazamiento)
            },
            popExitTransition = {
                salidaAlVolver(tipoAlVolver(initialState.destination.route, targetState.destination.route), animar, desplazamiento)
            }
        ) {
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
                        navController.navigate(Rutas.SOLICITUD_ENVIADA) {
                            popUpTo(Rutas.NUEVA_RESERVA) { inclusive = true }
                        }
                    }
                )
            }
            composable(Rutas.SOLICITUD_ENVIADA) {
                SolicitudEnviadaScreen(
                    onVerMisReservas = {
                        navController.navigate(Rutas.MIS_RESERVAS) {
                            popUpTo(Rutas.MIS_RESERVAS)
                        }
                    }
                )
            }
            composable(Rutas.SESION_VENCIDA) {
                SesionVencidaScreen(
                    onIniciarSesion = {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
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
                ResumenScreen(titulo = "Resumen")
            }
            composable(Rutas.FINANZAS) {
                ResumenScreen(titulo = "Finanzas")
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
    } else if (rutaActual == Rutas.LOGIN || rutaActual == Rutas.SESION_VENCIDA ||
        rutaActual == Rutas.SOLICITUD_ENVIADA || rutaActual == null
    ) {
        ContenidoCentrado(anchoMaximo) { contenido() }
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                destinos.forEach { destino ->
                    item(
                        icon = { Icon(painterResource(destino.icono), contentDescription = destino.etiqueta) },
                        label = { Text(destino.etiqueta) },
                        selected = rutaDePestana == destino.ruta,
                        onClick = {
                            navController.navigate(destino.ruta) {
                                launchSingleTop = true
                                popUpTo(destino.ruta)
                            }
                        }
                    )
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {},
                    // Flecha para volver, solo en las pantallas internas.
                    navigationIcon = {
                        if (esPantallaInterna) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                            }
                        }
                    },
                    actions = {
                        if (sesion?.rol == "DIRECTIVA" && rutaActual == Rutas.RESERVAS) {
                            IconButton(onClick = { navController.navigate(Rutas.DISPONIBILIDAD) }) {
                                Icon(Icons.Filled.DateRange, contentDescription = "Ver disponibilidad")
                            }
                        }
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            CabeceraSesion(
                                nombre = sesion?.nombre ?: "",
                                rol = nombreDelCargo(sesion?.cargo),
                                onClick = { menuAbierto = true }
                            )
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