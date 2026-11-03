# Plan — AgroWatch Mobile (TP Integrador N.º 2)

App Android en Java que consume la misma API del TP N.º 1
([agrowatch-api](https://github.com/harikirtandas/agrowatch-api)), desplegada en
`https://api-rest-agro.up.railway.app/api/`. Es una versión mobile **simple**: el
productor ingresa, ve el semáforo de su campo y carga costos desde el lote. Toda la
configuración (establecimientos, lotes, campañas, siembras, usuarios) sigue en el
frontend web.

Entrega: **martes 24/11/2026, 23:59**, por el campus.

## Alcance

| Hace la app | Queda en la web |
|---|---|
| Ingreso con el usuario de la API | Registro de cuenta |
| Panel: semáforo del campo, resumen y lista de lotes | Crear o editar establecimientos y lotes |
| Detalle de un lote: semáforo, números de la proyección y costos | Campañas y siembras |
| **Cargar un costo** a la siembra de un lote | Sincronizar cotizaciones |
| Recibir notificaciones push (FCM) | |

## Pantallas

| Activity | Qué muestra | Cómo se llega / qué recibe |
|---|---|---|
| `LoginActivity` | Logo, email, contraseña. `POST /auth/login` | Pantalla de entrada. Si ya hay sesión pasa directo al panel. Hace `finish()`: no queda en la pila |
| `PanelActivity` | Selector de campo, velocímetro del campo, 3 indicadores (ha sembradas, margen bruto, lotes en alerta) y la lista de lotes con su color | Desde el login |
| `LoteActivity` | Velocímetro del lote, rinde de indiferencia, ingreso, costo, margen y la lista de costos | Extra `siembra_id`: abre un elemento por su id |
| `NuevoCostoActivity` | Formulario del costo, validado antes de enviar. `POST /siembras/{id}/costos` | Abierta con la **Activity Result API**; devuelve `RESULT_OK` y el lote se recarga |

## Endpoints que usa la app

No se agregan endpoints nuevos ni se modifica ninguno.

| Método | Ruta | Para qué |
|---|---|---|
| POST | `/auth/login` | Ingresar: devuelve `token`, `expira`, `usuario` |
| POST | `/auth/logout` | Salir: invalida el token |
| GET | `/establecimientos` | Campos del usuario (selector del panel) |
| GET | `/campanias` | Se usa la abierta más reciente, igual que la web |
| GET | `/lotes?establecimiento_id=X` | Lotes del campo elegido |
| GET | `/siembras?campania_id=Y` | Siembras de la campaña, cruzadas con los lotes |
| GET | `/siembras/{id}` | Detalle de la siembra (título del lote) |
| GET | `/siembras/{id}/margen` | Semáforo y números de cada lote |
| GET | `/siembras/{id}/costos` | Costos cargados en el lote |
| POST | `/siembras/{id}/costos` | **Alta de un costo** desde la app |

R2 queda cubierto así: listar (GET de lotes y costos), crear (POST de costo) y detalle por id
(`GET /siembras/{id}`).

## Coherencia con el frontend web

- **Paleta** en `colors.xml` con los mismos nombres que las variables CSS de la web:
  verde oscuro `#1c3325` (barra superior), verde `#4f8f63` (botones), amarillo `#e8b84b`
  (acento del logo), fondo `#f2f1ec`, bordes `#e2e2dc`, textos `#1a1a1a` / `#6b7268`.
- **Semáforo**: rojo `#d1583f`, amarillo `#e0a83c`, verde `#4f9d63`, los mismos arcos de
  `semaforo.html`, y los mismos textos: «Bien», «Ajustado», «Alerta».
- **Logo** `logo-aw.svg` pasado a Vector Drawable: barra superior, login e ícono de la app.
- **Tipografía**: la web usa `-apple-system, "Segoe UI", Roboto…`, que en Android es Roboto,
  la fuente del sistema. Se copian los pesos (700 en títulos, 600 en etiquetas).
- **Velocímetro**: `GaugeView`, una View propia que dibuja con `Canvas` los tres arcos y la
  aguja con los mismos cortes que `anguloAguja()` de la web (±25,7°).
- **El semáforo del campo no lo calcula la API**: la web lo arma en `dashboard.js`
  (`resumenAgregado()`, `semaforoDeMargen()`, umbral verde 20 %). La app usa la misma lógica,
  pasada a Java en `ResumenCampo` y `Semaforo`, para mostrar los mismos números que la web.

## Arquitectura

- Activities + layouts XML, el modelo de AppAlumnos de la cursada.
- `api/`: `AgroApi` (interfaz de Retrofit, la API descripta en un solo lugar), `ApiCliente`
  (la **URL base en un solo lugar**, interceptor que agrega `Authorization: Bearer`) y
  `ErrorApi` (lee el `{"error": {"status", "mensaje", "errores"}}` de la API).
- `modelo/`: un POJO por recurso (`Usuario`, `Establecimiento`, `Campania`, `Lote`, `Siembra`,
  `Margen`, `Costo`) y los cuerpos de los pedidos (`LoginPedido`, `LoginRespuesta`, `CostoPedido`).
- `sesion/Sesion`: token y campo elegido en `SharedPreferences`.
- `panel/`, `lote/`, `costo/`, `login/`: una Activity por pantalla con sus adaptadores.
- `semaforo/`: `Semaforo` (estado, colores, ángulo, frase), `ResumenCampo` (el agregado del
  campo) y `GaugeView`.
- Retrofit 2.11 + Gson, como en la Semana 14. Los pedidos van con `enqueue()`, fuera del hilo
  principal.
- El alta del costo vive en un `ViewModel`: si se gira la pantalla mientras se guarda, el
  pedido no se pierde ni se manda dos veces.

## Requisitos de la consigna

| # | Cómo se cumple |
|---|---|
| R1 | 4 Activities, Intents con extras (`siembra_id`, nombre del lote), `NuevoCostoActivity` con Activity Result API |
| R2 | GET de lotes, siembras y costos; POST de costo; GET por id de la siembra |
| R3 | Validación del formulario antes del POST. `onFailure` → «Sin conexión con la API»; `onResponse` con error → el `mensaje` de la API (400/404/422) y, en un 422, los `errores` por campo. 401 → vuelve al login |
| R4 | Notificaciones push con **FCM** |
| R5 | Textos en `strings.xml`, `EdgeToEdge` con insets, lista de lotes de la API, «Cargando…», botón Guardar deshabilitado mientras espera |
| R6 | Las listas se vuelven a pedir en `onCreate`; el formulario conserva lo escrito al girar; el alta vive en un `ViewModel`; la pantalla de ingreso no queda en la pila |
| R7 | URL base solo en `ApiCliente`. Sin contraseñas en el código: el usuario de prueba va en el envío del campus |
| R8 | La API y la web ya están en Railway. Sin endpoints nuevos |
| R9 | README con las 7 secciones, APK de depuración y video de 3 a 5 minutos |
| R10 | Commits de **todos** los integrantes: cada uno toma etapas del cronograma |

## Servicio cloud: FCM (R4)

Se eligió FCM porque no se superpone con el login de la API (Firebase Authentication obligaría
a un segundo ingreso, y no protege la API), es gratis y no pide tarjeta.

1. Crear el proyecto en la consola de Firebase y registrar la app con el `applicationId`
   `com.example.agrowatchmobile`.
2. Bajar `google-services.json` a `app/` y sumar el plugin `com.google.gms.google-services`
   y la BoM de Firebase con `firebase-messaging`.
3. `AvisosService extends FirebaseMessagingService`: `onNewToken` y `onMessageReceived`.
4. Pedir el permiso `POST_NOTIFICATIONS` (Android 13+) al entrar al panel.
5. Suscribir la app al tema `avisos`, para mandar desde la consola sin copiar tokens.
6. Para el video: mandar una notificación de prueba desde la consola y mostrar que llega.
7. Opcional: que el PHP avise por FCM al sincronizar cotizaciones (API HTTP v1, con la cuenta
   de servicio en una variable de entorno de Railway, nunca en el repo).

Consultar a los docentes si `google-services.json` va en el repositorio.

## Cronograma

| Cuándo | Etapa |
|---|---|
| Semana 13 | Tema, colores, logo, textos; las 4 Activities con Intents; `GaugeView` |
| Semana 14 (10/11) | `ApiCliente`, `AgroApi`, modelos y `ErrorApi`; login; panel y lista de lotes |
| Semana 14 | `LoteActivity` y `NuevoCostoActivity` con el POST y Activity Result |
| Semana 15 (16–17/11) | FCM |
| 18 al 22/11 | Errores forzados (modo avión, 422, id inexistente), rotación, README, APK y video |
| 24/11 | Entrega |

## Antes de entregar

- [ ] La APK instala en un emulador limpio (Android 14+) y apunta a Railway.
- [ ] La API y la web abren desde un celular con datos móviles.
- [ ] Un costo cargado en la app aparece en el semáforo de la web.
- [ ] Modo avión, datos inválidos, id inexistente y rotación: la app no se cierra.
- [ ] La notificación de FCM llega.
- [ ] No hay contraseñas ni claves en el código ni en el historial.
- [ ] README con las 7 secciones; usuario de prueba en el envío del campus.
- [ ] Todos los integrantes tienen commits.
