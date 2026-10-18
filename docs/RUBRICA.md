# Revisión frente a la rúbrica de Parcial 2

Fecha: 17 de octubre de 2026. Referencia: Ev_Parcial2_Encargo_DSY1105.pdf,
instrucciones de páginas 2–3 y rúbrica de páginas 4–6.
Esta matriz registra evidencia; la calificación y el nivel final de logro corresponden al docente.

| Criterio | Peso | Implementación y evidencia | Estado |
|---|---:|---|---|
| IE 2.1.1: interfaz y navegación | 15% | AppNavigation, Rutas, Destinos; 11 pantallas del caso, barra por rol, Volver, cierre de sesión; formularios con scroll/ancho limitado y resumen adaptable | Implementado; recorridos probados en emulador |
| IE 2.1.2: formularios, errores e íconos | 15% | Login, Nueva reserva y Registrar pago completos; errores por campo, isError, supportingText e ícono Warning; configuración de puerta con errores de sala/clave | Implementado; casos válidos e inválidos probados |
| IE 2.2.1: validación y estado desacoplados | 10% | UiState, StateFlow, ViewModels, ReglasReserva/ReglasPago y comprobaciones transaccionales del repositorio | Implementado; pruebas de reglas, permisos, cruces y duplicados |
| IE 2.2.2: animaciones funcionales | 10% | AnimatedVisibility en errores, comprobante y resultado del lector; transiciones de Navigation Compose; indicadores de carga y controles deshabilitados mientras guarda | Implementado; se observa al enviar vacío y activar comprobante |
| IE 2.3.1: modularidad y persistencia | 15% | Capas de pantallas, ViewModels, repositorio/DAO; Room 3, migración conservadora, archivos privados y DataStore para puerta | Implementado; migración, persistencia y flujos probados |
| IE 2.3.2: GitHub y planificación compartida | 20% | Repositorio existente con rama version-william; README y guía de pruebas preparados | Parcial: revisar contribuciones reales, tareas compartidas y publicación final con el equipo |
| IE 2.4.1: dos recursos nativos | 15% | Cámara con permiso y FileProvider; galería con selección de imagen; vista previa, copia privada y lectura posterior del comprobante | Cámara y galería probadas en emulador; repetir en el teléfono de exposición |

No se asigna un porcentaje global a partir de esta tabla. Contar archivos, pantallas
o pruebas no demuestra por sí solo el nivel máximo de cada indicador.

## Cobertura de formularios

| Formulario | Validación | Respuesta visible |
|---|---|---|
| Login | Correo requerido/formato, clave requerida, credenciales almacenadas | Error de campo e ícono; error general ante credenciales incorrectas |
| Nueva reserva | Sala, fecha no pasada, inicio/fin 09:00–22:00, orden, reglamento y cruce | Error junto al dato; resultado de guardado; estado Pendiente |
| Registrar pago | Tipo, reserva confirmada sin pago o categoría, entero positivo, fecha no futura, comprobante si activo y confirmación informativa | Campos que cambian según tipo; errores e íconos; carga y aviso de pago registrado |
| Escanear | Sala, código, firma, fecha y ventana de ingreso | Mensaje de problema o tarjeta con resultado e ícono, nombre y ventana |
| Activar/salir de puerta | Sala existente, clave válida, coincidencia de hash al salir | Errores en sala/clave y aviso de almacenamiento si falla |

Descripción y comprobante son opcionales según el caso. El comprobante pasa a ser
obligatorio solamente cuando se activa su Switch. No se impone un campo que el caso no pide.

## Reglas de negocio comprobadas

- Reserva propia pendiente: puede cancelarse. Una confirmada no se cancela por esa acción.
- Solo Secretaría confirma/rechaza; ni Presidencia ni Tesorería tienen esa facultad.
- Solo Tesorería registra pagos. Los permisos se revisan antes de escribir, además de controlar botones.
- Confirmadas bloquean; pendientes no bloquean. Horarios contiguos son válidos.
- Dos pagos simultáneos no producen un segundo arriendo para la misma reserva.
- Ticket disponible únicamente tras confirmación y pago de la reserva propia.
- QR alterado, sala incorrecta o fuera de ventana se rechaza.
- Ventana inclusiva: inicio −10 minutos a inicio +15 minutos.
- El modo puerta registra un solo uso por id de reserva en ese aparato y exige clave para salir.
- No se realizan cobros ni se controla una cerradura física.

## Evidencia para mostrar al docente

1. Login vacío e incorrecto, y entrada válida con dos cargos distintos.
2. Solicitud vacía, solicitud válida, confirmación, choque y cancelación.
3. Pago inválido y válido; imágenes capturadas por cámara y seleccionadas desde galería.
4. Reinicio del proceso y comprobación de datos persistidos.
5. Cambio del resumen tras registrar ingreso/gasto.
6. Ticket y lectura, con resultados válido, sala distinta y fuera de horario.
7. Puerta: activación, lectura, repetición y clave de salida; explicar que es ampliación local.
8. Código: seguir un evento desde pantalla hasta ViewModel, repositorio y DAO.
9. Historial real de GitHub y tablero real, con responsables y trabajo de ambos integrantes.

Ver resultados y límites concretos en [PRUEBAS.md](PRUEBAS.md) y pasos en el [README](../README.md).

## Entrega que debe completar el equipo

- Confirmar número de equipo y enlace del tablero de planificación.
- Revisar y publicar los cambios finales en el repositorio público acordado, accesible al docente.
- Mostrar commits técnicos, participación real de ambos y tareas compartidas; no crear actividad retrospectiva ficticia.
- Subir el código al enlace grupal de AVA mediante un integrante.
- Cada integrante activa su enlace individual con nombre, apellido, equipo y aplicación/contexto.
- Confirmar con el docente la fecha y hora límite. La pauta prohíbe cambios en el repositorio después de la entrega y antes de la evaluación.
- Repetir los recursos nativos en el teléfono/tablet que se ocupará y preparar la defensa sin depender de esta guía.

La revisión local del código no demuestra que ya se haya cumplido GitHub/Trello,
ni que se haya realizado la entrega en AVA.
