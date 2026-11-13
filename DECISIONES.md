# Decisiones de diseño y alcance

Este documento explica por qué la app y el backend de Junta Zaror están hechos como están. Sirve para la defensa del Parcial 5. Cada fila dice qué se decidió, qué otra opción había y por qué se eligió esta.

## Datos y conexión

| Decisión | Alternativa | Por qué |
|---|---|---|
| Room se queda como copia local (caché). El servidor tiene los datos oficiales y la app los pide al abrir las pantallas y los guarda en Room | Quitar Room y leer todo del servidor | La propuesta del docente guarda lo oficial en el servidor, pero la app sigue mostrando lo último que descargó si el servidor no responde. La pantalla avisa que son datos guardados |
| Las entidades de Room también son el modelo del JSON | Una clase aparte para la base y otra para la red | Evita duplicar clases en un proyecto de este tamaño. El costo: si el contrato cambia, cambia también la base local |
| Migración destructiva de Room (versión 4) | Escribir migraciones | La base es solo una copia: si cambia de versión se borra y se descarga de nuevo. No hay datos propios de la app que perder |
| El token de sesión vive solo en memoria. La excepción es el de la puerta, que se guarda en DataStore | Guardar también el de sesión en el teléfono | Al cerrar la app hay que entrar otra vez, pero no queda una credencial en disco. La puerta es un aparato fijo donde nadie inicia sesión, por eso ahí sí se guarda. Si el servidor responde 401 con una sesión abierta, la app avisa que venció y vuelve al login |
| La clave se envía al servidor tal cual y allá se guarda con SHA-256 | Calcular el hash en la app | Así el servidor es quien decide cómo guardar las claves. Por la red local de desarrollo viaja sin cifrar. En un uso real iría sobre HTTPS |
| El respaldo automático de Android está desactivado (`allowBackup="false"`) | Dejarlo activo con las reglas de plantilla | El modo puerta guarda en el teléfono un token de 7 días, y Room guarda una copia de reservas y pagos. Con el respaldo activo, esos datos viajarían a la cuenta de Google de quien use el teléfono |
| La dirección del servidor sale de `build.gradle.kts` (`-PurlServidor=...`). Por defecto es `10.0.2.2:9090`, la del emulador | Escribir la IP en el código | Un mismo código sirve para el emulador y para un teléfono real. La IP del equipo no queda en el repositorio. Android solo permite http hacia las direcciones listadas en `network_security_config.xml` |
| El teléfono usa `targetSdk` 36 y no 37 | Subir a 37 | En un emulador con Android 17 el sistema bloquea las conexiones a la red local sin un permiso especial. El ejemplo del docente también usa 36 |

## Estructura de la app

| Decisión | Alternativa | Por qué |
|---|---|---|
| `repository/` va dentro de `data/` | Al lado de `data/`, como en las guías 14 y 15 | Es lo que muestra el documento "Arquitectura de una APP". Cambiarlo toca más de 14 archivos |
| Los DAO y `AppDatabase` están en `data/repository/` | Una carpeta aparte | El docente no define dónde van. Quedan junto al repositorio que los usa |
| Existen `navigation/` y `utils/`, que el documento no lista | Meter todo en `ui/` | Navegación y lógica pura no son interfaz. `utils/` guarda las reglas que se prueban sin Android: fechas, bloques, QR, resumen y cargo |
| Cada flujo tiene una clase `UiState` en `data/model/` | Un `StateFlow` por cada dato, como en los ejemplos del docente | Un solo estado por pantalla deja en un lugar los errores de cada campo. No son datos del dominio, solo lo que la pantalla muestra |
| Las validaciones están en los ViewModel y en `utils/`, no en las pantallas | Validar dentro de la interfaz | Se pueden probar sin abrir una pantalla, y las pantallas solo muestran el resultado |
| Los ViewModel reciben el repositorio por constructor | Crearlo adentro | Permite reemplazarlo por uno falso en las pruebas. Hay un constructor secundario para que Compose los cree como siempre |

## Pruebas y entrega

| Decisión | Alternativa | Por qué |
|---|---|---|
| 136 pruebas unitarias en `src/test` con Kotest, MockK y coroutines-test | Pruebas con JUnit 4 | Kotest permite describir cada regla con una frase. Corren en el computador, sin emulador, y se ejecutan con JUnit Platform |
| No hay pruebas instrumentadas (`androidTest`) | Pruebas de pantallas con Compose | Se priorizó probar la lógica, donde están las reglas. Las pantallas se revisaron a mano en el emulador y en un teléfono, en modo claro y oscuro |
| El APK de `release` usa la optimización de R8 con un archivo `proguard-rules.pro` | Dejarla apagada, como en el ejemplo del docente | Reduce el tamaño. Con la optimización activa Gson y Retrofit necesitan reglas para no romperse, y se comprobó que el APK de `release` funciona |
| El keystore queda fuera del repositorio y solo lo maneja Alex en Android Studio | Guardarlo en el repositorio | Una llave de firma no se comparte. Si se pierde, no se puede actualizar la app |

## Diseño

| Decisión | Alternativa | Por qué |
|---|---|---|
| Colores, tema, íconos y marca salen del kit de diseño. Se usa Nunito Sans y Poppins solo para el nombre del logo | Tipografías del sistema | Es el diseño que se entregó. Con Nunito Sans las letras del logo quedaban muy delgadas, por eso Poppins en ese único lugar. Las dos tienen licencia abierta, en `assets/licencias/` |
| Las ilustraciones son vectores y dibujos de Compose, no imágenes | Archivos PNG o animaciones Lottie | Pesan casi nada, se ven nítidas en cualquier pantalla y toman los colores del modo claro u oscuro |
| El borde de los campos de texto es el punto medio entre el del kit y un contraste fuerte (`#9E8670`, unos 3,3 a 1 sobre el fondo) | El borde pálido del kit (`#E8D7C2`) | El borde del kit casi no se ve sobre el crema y no llega al mínimo de 3 a 1 que pide la accesibilidad para componentes de formulario. Los bordes decorativos de las tarjetas sí usan el del kit |
| La foto de cada sala llena la tarjeta de la reserva (vecino) y es el encabezado de la ficha en el detalle y en las reservas de la directiva, con el texto encima sobre un degradado oscuro | El ícono de una casita, igual para todas las salas | Con la foto se reconoce la sala a primera vista. La foto se busca por el nombre de la sala; si una sala no tiene foto, la tarjeta queda con fondo liso. Las fotos van reducidas a 1000 px (unos 50 a 75 KB cada una) |
| La cabecera tiene un solo ícono de cuenta: el nombre, el cargo y el avatar con la inicial forman un solo botón que abre el menú para cerrar sesión | Un avatar con el nombre a la izquierda y otro ícono de persona a la derecha | Dos íconos de persona en la misma barra confundían: no quedaba claro cuál abría el menú |
| El estado de una reserva se dice con ícono y texto, además del color | Solo el color de la tarjeta | Una persona que no distingue colores igual lo entiende |
| El chip de estado cambia de color con un fundido de 320 ms cuando la reserva cambia de estado | Cambiarlo de golpe | Es el movimiento que pide el kit para ese chip |
| Los gastos por categoría llevan el ícono del kit (agua, electricidad, gas) sobre una ficha de color, y sus barras crecen en secuencia al abrir la pantalla | Barras fijas sin ícono | Es lo que muestra el diseño de Finanzas. La animación se salta con "Reducir animaciones". No lleva la línea de tendencia del kit, porque la pantalla muestra un mes y esa línea necesita varios meses de datos |
| Los estados vacíos llevan la ilustración del kit (reservas, pagos, gastos, escanear y cámara sin permiso), dibujadas como vectores con una versión para el modo oscuro | Solo un texto | El diseño las pide, y sin ellas una lista vacía parecía una pantalla rota. Son vectores, no imágenes: pesan casi nada y toman los colores del modo claro u oscuro |
| El botón principal se achica a 0,97 mientras se presiona (120 ms) y un campo que queda con error se sacude una vez (6 dp, 320 ms) | Sin movimiento | Es lo que pide el kit de diseño para esos dos casos. Los dos se saltan con "Reducir animaciones" |
| Cancelada usa la tinta roja de Rechazada, con ✕ y fondo neutro | El guión sobre fondo gris | Un guión no dice nada. Rechazada y Cancelada son un "no", y el fondo es lo que las distingue entre sí |
| Las animaciones respetan "Reducir animaciones" del teléfono | Animar siempre | Es una opción de accesibilidad. Si está activada, las animaciones se saltan |
| El teclado empuja el contenido con `imePadding()` y el botón del teclado envía el formulario | Dejar que Android redimensione la ventana | Con la app dibujando de borde a borde, Android ya no redimensiona la ventana y el teclado tapaba lo que se escribía |

## Backend

| Decisión | Alternativa | Por qué |
|---|---|---|
| Tres servicios más un gateway, cada uno con su base de datos | Un servicio único | El caso pide microservicios. Se mantienen los nombres del ejemplo del docente y la equivalencia con la propuesta está en el README del backend |
| El gateway valida el token y `reserva-service` y `pago-service` lo validan otra vez | Validar solo en el gateway | Esos dos servicios no quedan abiertos si alguien llega a ellos sin pasar por el gateway. `auth-service` solo firma tokens y no los valida. Los tres escuchan solo en `127.0.0.1` |
| `pago-service` pregunta a `reserva-service` si la reserva está confirmada | Que la app lo compruebe | Las reglas de dinero no pueden depender de lo que diga la app |
| Los controladores devuelven las entidades, y los DTO se usan en las peticiones | DTO también para las respuestas | Menos clases. Para un vecino, `GET /pagos` entrega los campos privados vacíos. Crear los DTO de respuesta es una mejora posible |
| Los errores usan `ResponseStatusException` con un manejador central | Excepciones propias, como en el ejemplo del docente | Equivalente: el cuerpo del error es el mismo en los tres servicios |
| La puerta se activa con un inicio de sesión de la directiva, con un token de 7 días | Una clave fija en el aparato | Solo la directiva puede activar una puerta y se puede identificar quién lo hizo. El token sirve únicamente para registrar accesos |
| El QR se genera y se firma en la app. El servidor guarda qué QR ya se usó | Que el servidor firme el QR | Es lo que alcanzó el tiempo. Su límite: la clave de firma está dentro de la app |

## Lo que no se hizo

| Pendiente | Qué haría falta |
|---|---|
| Notificaciones cuando cambia el estado de una reserva | Avisos locales al refrescar, o un servicio de mensajes. Hoy la app avisa solo dentro de la pantalla |
| Operaciones sin conexión que se envían después | Una cola de operaciones pendientes con reintentos |
| Subir el comprobante de pago al servidor | Un endpoint `multipart`. Hoy solo viaja la referencia |
| Tablas `historial_reserva`, `categoria_movimiento`, `comprobante`, `informe_financiero` y `rol` | La propuesta dice que los atributos pueden ajustarse. `historial_reserva` sería la primera |
| Docker | Hay `Dockerfile` para dos servicios, pero no se ejecutan. El backend se enciende con scripts de PowerShell |
| QR firmado por el servidor | Dos rutas nuevas y reescribir en Java la validación de la puerta |
| Escanear: mensajes de "ya usado", "otra sala" y "sin conexión" con las pantallas del diseñador | Una decisión de producto sobre qué debe mostrar cada caso |
