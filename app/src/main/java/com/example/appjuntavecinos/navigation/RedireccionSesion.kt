package com.example.appjuntavecinos.navigation

// La sesión de una persona vive solo en memoria. Si el sistema mata la app en segundo plano, al volver Android restaura
// la pantalla en la que estaba, pero la sesión ya no existe: se vería una pantalla con datos viejos y sin nombre, y cada
// acción fallaría. En ese caso se lleva a la persona al inicio de sesión.
//
// modoPuerta es null mientras se lee si este aparato es una puerta: hasta saberlo no se decide nada. La puerta no tiene
// sesión de persona, y el login y la pantalla de sesión vencida no necesitan una.
fun debeVolverAlInicioDeSesion(haySesion: Boolean, ruta: String?, modoPuerta: Boolean?): Boolean =
    modoPuerta == false &&
        !haySesion &&
        ruta != null &&
        ruta != Rutas.LOGIN &&
        ruta != Rutas.SESION_VENCIDA
