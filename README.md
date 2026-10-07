# AppJuntaVecinos

App Android para gestionar los arriendos de las dependencias comunitarias de la Junta de Vecinos Población Zaror (Los Jazmines 425, Cerrillos) y mostrar sus finanzas con transparencia. Es un caso ficticio y un MVP académico.

Evaluación Parcial 2 de DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC.

## Integrantes

- Pablo Alexander Rico Rodríguez
- William Martín Rodríguez Troncoso

Equipo: Grupo 

## Qué hace la app

Hay dos tipos de usuario, que entran por un login con correo y clave:

- **Vecino/a:** pide reservas de una dependencia, revisa la disponibilidad, ve el estado de sus solicitudes, muestra un ticket QR cuando la reserva está confirmada y consulta las finanzas de la junta en solo lectura.
- **Directiva:** ve las reservas, los pagos y el resumen financiero, y escanea los tickets en la entrada. Según el cargo se habilitan los botones: la Secretaría confirma o rechaza reservas, la Tesorería registra pagos y la Presidencia solo mira.

## Funcionalidades

Marcar cada una cuando esté terminada y probada.

- [ ] Login con correo y clave, con validación por campo
- [ ] Mis reservas, con el estado de cada una
- [ ] Disponibilidad de las dependencias por día
- [ ] Nueva reserva, con validaciones (fecha, horas, horario de la sede, horario ocupado)
- [ ] Detalle de reserva y cancelación de una solicitud pendiente
- [ ] Ticket QR de una reserva confirmada
- [ ] Reservas de la directiva: confirmar o rechazar (Secretaría)
- [ ] Escanear ticket con la cámara: acceso permitido, fuera de horario, otra dependencia o código no válido
- [ ] Pagos de la directiva y registro de pagos con comprobante de cámara o galería (Tesorería)
- [ ] Resumen financiero del mes
- [ ] Cerrar sesión

## Tecnologías

Kotlin, Jetpack Compose, Material Design 3, MVVM, Room, StateFlow y Navigation Compose.

## Estructura del proyecto

```
app/src/main/java/com/example/appjuntavecinos/
├─ MainActivity.kt
├─ navigation/     rutas y NavHost
├─ ui/
│   ├─ theme/      colores, tema y tipografía
│   ├─ components/ piezas reutilizables
│   └─ screens/    una pantalla por archivo
├─ viewmodel/      estado y validaciones de cada flujo
├─ model/          entidades de Room, estados de formulario y resultados de validación
└─ repository/     DAOs, base de datos y repositorios
```

## Cómo ejecutarla

1. Clonar el repositorio.
2. Abrir la carpeta con Android Studio (proyecto Compose).
3. Esperar a que termine la sincronización de Gradle.
4. Conectar un celular con la depuración USB activada, o crear un dispositivo en Device Manager.
5. Ejecutar el módulo `app`.

La base de datos local se llena sola la primera vez con datos de ejemplo.

## Usuarios de prueba

Todos los datos son ficticios. La clave de todos es `1234`.

| Correo | Rol |
|---|---|
| juan.perez@zaror.test | Vecino |
| ana.munoz@zaror.test | Vecina |
| pedro.soto@zaror.test | Vecino |
| maria.torres@zaror.test | Directiva, Secretaría |
| luis.rojas@zaror.test | Directiva, Tesorería |
| carmen.vidal@zaror.test | Directiva, Presidencia |

## Alcance

- Los datos son ficticios; no hay información real de la organización ni de sus vecinos.
- Registrar un pago es solo informativo: no procesa transacciones reales.
- Toda la información se guarda en el celular (Room). Cada celular tiene su propia base de datos.
