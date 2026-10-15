# AppJuntaVecinos

App Android para gestionar los arriendos de las dependencias comunitarias de la Junta de Vecinos Población Zaror (Los Jazmines 425, Cerrillos) y registrar sus pagos. Es un caso ficticio y un MVP académico.

Evaluación Parcial 2 de DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC.

## Integrantes

- Pablo Alexander Rico Rodríguez
- William Martín Rodríguez Troncoso

## Qué hace la app

Hay dos tipos de usuario, que entran por un login con correo y clave:

- **Vecino/a:** pide reservas de una dependencia (Salón Principal o Sala Multiuso), ve el estado de sus solicitudes y cancela las que siguen pendientes.
- **Directiva:** ve las reservas y los pagos. Según el cargo se habilitan los botones: la Secretaría confirma o rechaza reservas, la Tesorería registra pagos y la Presidencia solo mira.

## Funcionalidades

- [x] Login con correo y clave, con validación por campo y la clave guardada como hash SHA-256
- [x] Cerrar sesión desde el menú de la cuenta
- [x] Mis reservas, con el estado de cada una (pendiente, confirmada, cancelada o rechazada) y su color
- [x] Nueva reserva, con validaciones (fecha, horas, horario de la sede, horario ocupado) y aviso al crearla
- [x] Cancelar una reserva pendiente, con confirmación
- [x] Reservas de la directiva: la Secretaría confirma o rechaza, y se vuelve a revisar el choque al confirmar
- [x] Pagos de la directiva, con la lista de ingresos y gastos
- [x] Registro de pagos con comprobante de cámara o galería (solo Tesorería)
- [ ] Disponibilidad de las dependencias por día
- [ ] Resumen financiero
- [ ] Ticket QR y escaneo del ticket en la entrada

Lo que aparece sin marcar no está en esta versión. Ver el apartado Limitaciones.

## Recursos nativos

- **Cámara:** toma la foto del comprobante de un pago. Pide el permiso `CAMERA` cuando se necesita y, si el usuario lo niega, la app no se cae: avisa y deja usar la galería.
- **Galería:** permite elegir una imagen ya guardada como comprobante. No necesita permiso.

El comprobante elegido se copia a la carpeta privada de la app, para que siga disponible aunque la foto original desaparezca.

## Reglas de negocio

- La fecha de una reserva no puede ser pasada.
- La hora de fin tiene que ser mayor que la de inicio, y las dos dentro del horario de la sede (de 09:00 a 22:00).
- No se puede reservar un horario que ya esté ocupado por una reserva confirmada de la misma dependencia.
- Solo se puede cancelar una reserva pendiente.
- Solo la Secretaría confirma o rechaza reservas, y solo las que están pendientes. Al confirmar se vuelve a revisar que el horario no choque con otra reserva ya confirmada.
- Solo la Tesorería registra pagos.
- Un pago de arriendo exige una reserva confirmada, y cada reserva tiene como máximo un pago de arriendo.
- El monto de un pago tiene que ser mayor que 0.
- Un pago es solo informativo: no procesa transacciones reales.

## Tecnologías

Kotlin, Jetpack Compose, Material Design 3, MVVM con StateFlow, Room (con KSP), Navigation Compose y Coil para mostrar la imagen del comprobante.

## Estructura del proyecto

```
app/src/main/java/com/example/appjuntavecinos/
├─ MainActivity.kt
├─ navigation/     rutas, destinos de cada menú y NavHost
├─ ui/
│   ├─ theme/      colores, tema y tipografía
│   ├─ components/ piezas reutilizables (menú desplegable)
│   └─ screens/    una pantalla por archivo
├─ viewmodel/      estado y validaciones de cada flujo
├─ utils/          hash de la clave, fechas y manejo del comprobante
└─ data/
    ├─ model/      entidades de Room y estados de formulario con sus errores
    └─ repository/ DAOs, base de datos y repositorio
```

## Cómo ejecutarla

1. Clonar el repositorio.
2. Abrir la carpeta con Android Studio.
3. Esperar a que termine la sincronización de Gradle.
4. Conectar un celular con la depuración USB activada, o crear un dispositivo en Device Manager.
5. Ejecutar el módulo `app`.

La app funciona desde Android 7.0 (API 24). La base de datos local se llena sola la primera vez con datos de ejemplo: 2 dependencias, 6 usuarios, 8 reservas y 5 pagos.

Si el celular ya tenía instalada una versión anterior de la app, hay que borrar sus datos (o desinstalarla) antes de abrir esta, porque la base de datos cambió de versión.

## Usuarios de prueba

Todos los datos son ficticios. La clave de todos es `1234`.

| Correo | Rol | Qué puede hacer |
|---|---|---|
| juan.perez@zaror.test | Vecino | Pedir, ver y cancelar sus reservas |
| ana.munoz@zaror.test | Vecina | Igual que Juan; no tiene reservas al empezar |
| pedro.soto@zaror.test | Vecino | Igual que Juan |
| maria.torres@zaror.test | Directiva, Secretaría | Ver reservas y pagos, y confirmar o rechazar reservas |
| luis.rojas@zaror.test | Directiva, Tesorería | Ver reservas y pagos, y registrar pagos |
| carmen.vidal@zaror.test | Directiva, Presidencia | Ver reservas y pagos |

## Limitaciones

- No incluye disponibilidad por día, resumen financiero ni el ticket QR con su escaneo.
- Los datos son ficticios; no hay información real de la organización ni de sus vecinos.
- Toda la información se guarda en el celular (Room). Cada celular tiene su propia base de datos y no se comparten entre aparatos.