# AppJuntaVecinos

Aplicación Android para gestionar reservas de la Junta de Vecinos Población Zaror,
Los Jazmines 425, Cerrillos. Caso ficticio para DSY1105, Evaluación Parcial 2.

## Integrantes

- Pablo Alexander Rico Rodríguez
- William Martín Rodríguez Troncoso
- Número de equipo: por confirmar

## Versión de William: etapa de reservas

Esta entrega de desarrollo cubre el inicio de sesión, las reservas y la consulta financiera.
Es un avance funcional; quedan pendientes los comprobantes y los tickets QR.

| Funcionalidad | Estado |
|---|---|
| Login, validaciones por campo y cierre de sesión | Implementado |
| Mis reservas y estado vacío | Implementado |
| Disponibilidad por dependencia y fecha | Implementado |
| Nueva reserva con validación y persistencia | Implementado |
| Detalle y cancelación de solicitudes pendientes propias | Implementado |
| Confirmación/rechazo por Secretaría | Implementado |
| Resumen financiero por mes, ingresos, gastos y saldo | Implementado con registros de ejemplo |
| Registro de pagos y comprobantes de cámara/galería | Pendiente |
| Emisión del ticket QR y lectura con cámara | Pendiente |

Las pestañas Pagos y Escanear muestran claramente que están pendientes.
No simulan registros, cobros ni lecturas de cámara.

## Cómo ejecutarla

1. Abrir este proyecto en Android Studio y pulsar **Sync Now**.
2. Usar el JDK configurado por Android Studio/Gradle (el proyecto pide Java 25 para el daemon).
3. Tener instalado Android SDK 37; el dispositivo debe tener Android 7.0/API 24 o superior.
4. Seleccionar el módulo `app` y un emulador o teléfono.
5. Pulsar **Run**.

Desde una terminal del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

El último comando requiere un emulador o teléfono conectado. Las pruebas de
repositorio usan bases independientes de los datos de la app.

## Comprobaciones del 16 de octubre de 2026

- `assembleDebug`: compilación correcta.
- `testDebugUnitTest`: 9 pruebas aprobadas (8 de reglas, fechas y cálculos; 1 de plantilla).
- `connectedDebugAndroidTest`: 12 pruebas aprobadas en Medium Phone API 37.0
  (6 de Room, 5 de interfaz/navegación y 1 de plantilla).
- Se comprobó crear, consultar y cancelar una solicitud desde la pantalla,
  errores del Login/formulario, restricciones por cargo, cruces de horario,
  persistencia al cerrar/reabrir la base y conservación de sesión/formulario al recrear la actividad.

Estas pruebas cubren esta etapa. No sustituyen la prueba final en un teléfono,
otros tamaños de pantalla ni los recursos nativos todavía pendientes.

## Usuarios ficticios

La clave de todos es `1234`. Las cuentas se documentan aquí, no en el Login.

| Correo | Rol / cargo |
|---|---|
| juan.perez@zaror.test | Vecino |
| ana.munoz@zaror.test | Vecina; comienza sin reservas |
| pedro.soto@zaror.test | Vecino |
| maria.torres@zaror.test | Directiva, Secretaría |
| luis.rojas@zaror.test | Directiva, Tesorería |
| carmen.vidal@zaror.test | Directiva, Presidencia |

Las claves se guardan como hash SHA-256 para esta demostración académica.

## Recorrido para revisar el avance

1. Entrar con Ana: comprobar el estado vacío y abrir Nueva reserva.
2. Enviar vacío: comprobar los errores de dependencia, fecha, horas y reglamento.
3. Consultar Disponibilidad y volver al formulario.
4. Elegir un horario libre y enviar la solicitud: aparece como Pendiente.
5. Abrir su detalle y cancelar, o cerrar sesión y entrar con María para gestionarla.
6. Confirmar una pendiente: el horario queda ocupado en Disponibilidad.
7. La solicitud de ejemplo de Pedro que coincide con una confirmada debe ser rechazada al intentar confirmarla.
8. Entrar con Carmen o Luis: pueden consultar, pero no confirmar/rechazar.
9. Consultar Finanzas/Resumen y cambiar de mes.
10. Cerrar el proceso y volver a abrir: se solicita Login; las reservas y pagos siguen guardados.

Solo las reservas confirmadas bloquean horario. Dos pendientes pueden coincidir,
por lo que el cruce se revisa tanto al solicitar como al confirmar. Una reserva
que termina a las 12:00 permite otra que empieza a las 12:00.

## Estructura

```text
app/src/main/java/com/example/appjuntavecinos/
├── MainActivity.kt
├── navigation/       AppNavigation, Rutas y Destinos
├── data/
│   ├── model/        entidades, UiState y errores de formulario
│   └── repository/   DAOs, AppDatabase y JuntaRepository
├── viewmodel/        estado, validaciones, permisos y operaciones
├── ui/
│   ├── screens/      una pantalla por archivo
│   ├── components/   campos, mensajes y estados reutilizables
│   └── theme/        paleta, tema y tipografía
└── utils/            fechas y hash de claves
```

La explicación de cada parte y los pasos pendientes están en [Guía del proyecto](docs/GUIA_DEL_PROYECTO.md).

## Datos y límites actuales

- Room guarda cuatro tablas en `junta.db`, esquema 2.
- La carga inicial es transaccional y no vuelve a insertar datos si ya existe.
- Contiene dos espacios, seis usuarios, ocho reservas y cinco pagos ficticios.
- Las fechas se calculan en relación con la primera carga. Los movimientos de
  ejemplo están entre dos y cinco días antes; al cambiar de mes pueden aparecer en el período anterior.
- Cada dispositivo tiene su propia base; no hay sincronización ni servidor.
- La sesión en el ViewModel sobrevive a giros; al reiniciar el proceso se vuelve al Login.
- Los vecinos ven disponibilidad sin nombres ni comprobantes.
- Los pagos son registros informativos: no se realizan transacciones bancarias.
- Falta integrar y probar los dos recursos nativos (cámara y galería), el QR y los formularios de pagos.
- Falta completar la entrega académica y las evidencias reales de GitHub/Trello.
