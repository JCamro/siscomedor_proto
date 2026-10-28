# Capturas para el informe del Laboratorio 05

Guardar cada PNG en esta misma carpeta con el nombre exacto. El informe cubre solo la tarea "Comprar ticket".
Credenciales demo: código `20240001`, contraseña `comedor123`.

| # | Figura | Archivo | Qué debe verse |
|---|---|---|---|
| 01 | 2 | `01_login_vacio.png` | Login recién abierto, botón "Ingresar" deshabilitado |
| 02 | 3 | `02_login_error_campos.png` | Código con 3 dígitos y contraseña de 2 caracteres: errores en rojo bajo cada campo |
| 03 | 4 | `03_login_credenciales_incorrectas.png` | Código 20240001 + contraseña "incorrecta1", tras pulsar Ingresar: aviso de error |
| 04 | 5 | `04_login_valido.png` | Credenciales correctas, contraseña visible (ojo), switch "Recordar mi código" activo |
| 05 | 6 | `05_inicio.png` | Inicio tras el login |
| 06 | 7 | `06_inicio_seleccion.png` | Fecha, servicio y variante elegidos; barra verde con el total |
| 07 | 8 | `07_tu_seleccion.png` | Panel "Tu selección" (tocar el resumen de la barra verde) con botón "Revisar y pagar" |
| 08 | 9 | `08_checkout_confirmacion.png` | Diálogo "Revisar y pagar" antes de pulsar "Confirmar pago" |
| 09 | 10 | `09_checkout_exito.png` | Resultado exitoso del pago |
| 10 | 11 | `10_mis_tickets.png` | Pestaña "Mis tickets" con el ticket comprado |
| 11 | 12 | `11_detalle_ticket.png` | Detalle del ticket abierto desde la lista (ruta tickets/{id}) |
| 12 | 13 | `12_sin_conexion.png` | Inicio en modo sin conexión (pago bloqueado) |
| 13 | 14 | `13_compra_duplicada.png` | Servicio ya comprado: "Ya tienes este ticket" |
| 14 | 15 | `14_tests_ok.png` | Android Studio mostrando los tests unitarios en verde (incluye LoginValidationTest) |
| 15 | 16 | `15_preview_login.png` | Panel @Preview de LoginPreviews.kt |

Prueba de navegación atrás: desde el detalle pulsar "Volver a Mis tickets" o Atrás → vuelve a Mis tickets; desde Mis tickets pulsar Atrás → vuelve a Inicio; desde Inicio pulsar Atrás → sale de la app (no vuelve al login).
