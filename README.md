# AgroWatch Mobile

**El semáforo de tu campo en el bolsillo.** App Android de AgroWatchOut: el productor ingresa
con su cuenta, ve si sus lotes van a dejar plata y carga costos desde el lote. Usa la misma API
y los mismos datos que el frontend web.

|                    |                                                                                                |
| ------------------ | ---------------------------------------------------------------------------------------------- |
| **API**            | https://api-rest-agro.up.railway.app/api                                                       |
| **Frontend web**   | https://agrowatchout.up.railway.app                                                            |
| **Backend**        | [agrowatch-api](https://github.com/harikirtandas/agrowatch-api), desplegado en Railway (PaaS)  |
| **Materia**        | Aplicaciones Móviles y Cloud Computing (UPSO), Trabajo Práctico Integrador N.º 2               |
| **Docentes**       | Eduardo Mónaco y Heber Alonso                                                                  |
| **Grupo**          | 1                                                                                              |
| **Integrantes**    | Jorge Bianchi, Gabriel R. Iglesias, Marcos San Román, Camila Ailén Rodríguez y Facundo Curiman |

## Índice

1. [Presentación](#1-presentación)
2. [Cómo correrla](#2-cómo-correrla)
3. [Pantallas](#3-pantallas)
4. [Endpoints que usa la app](#4-endpoints-que-usa-la-app)
5. [Servicio cloud](#5-servicio-cloud)
6. [Decisiones de diseño](#6-decisiones-de-diseño)
7. [Limitaciones conocidas](#7-limitaciones-conocidas)

## 1. Presentación

**Tema:** economía agraria. AgroWatchOut responde, mientras el cultivo todavía está en el lote,
la pregunta que el productor se hace al cierre de la campaña: **¿lo que sembré me es rentable?**
La API calcula el margen bruto proyectado, el rinde de indiferencia y un semáforo de
rentabilidad con el precio del grano y el dólar del día.

La app es la versión mobile **simple** del frontend web. No lo replica: toda la configuración
(registro, establecimientos, lotes, campañas y siembras) sigue haciéndose en la web. En el
teléfono el productor hace lo que necesita en el campo:

- ver el semáforo de cada establecimiento y cuáles lotes están en riesgo;
- ver los números de un lote: margen, costos y rinde de indiferencia;
- **cargar un costo** apenas lo tiene (el gasoil, el flete, el fertilizante) y ver cómo cambia
  el semáforo.

Mantiene la identidad del frontend web: la misma paleta, el logo AW, el velocímetro del
semáforo con sus colores y los mismos textos («Bien», «Ajustado», «Alerta»).

**Usuario de prueba:** el mismo del frontend web, en la
[sección 2 del README de la API](https://github.com/harikirtandas/agrowatch-api#2-usuario-de-prueba).
También va en el envío del campus.

## 2. Cómo correrla

**Requisitos:** Android Studio, un emulador o teléfono con Android 7.0 (API 24) o superior y
conexión a Internet. Probada en el emulador **Medium Phone con Android API 37**.

1. Clonar el repositorio.
2. En Android Studio: **File → Open** y elegir la carpeta del proyecto.
3. Esperar el Gradle Sync y ejecutar el módulo `app`.

**La URL de la API está en un solo lugar:** la constante `URL_BASE` de
[`ApiCliente.java`](app/src/main/java/com/example/agrowatchmobile/api/ApiCliente.java). Apunta
al despliegue de Railway por HTTPS; no hace falta cambiarla para probar la app.

**APK:** `app/build/outputs/apk/debug/app-debug.apk`, generada con
**Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

**Tests:** la lógica del semáforo tiene tests de unidad, que se corren con
`./gradlew testDebugUnitTest`.

> **Pendiente (FCM):** cuando se sume el servicio cloud (sección 5) va a hacer falta el
> archivo `google-services.json` en la carpeta `app/`. Acá se va a explicar cómo conseguirlo.

## 3. Pantallas

| Activity | Qué muestra | Desde dónde se llega y qué recibe |
| --- | --- | --- |
| `LoginActivity` | Logo, email y contraseña. Ingresa con `POST /auth/login` y guarda el token. Un enlace abre el registro en la web | Es la pantalla de inicio. Si ya hay una sesión guardada pasa directo al panel. Hace `finish()`, así Atrás no vuelve a ella |
| `PanelActivity` | Selector de campo (solo si hay más de uno), el velocímetro con el semáforo del campo, tres indicadores (hectáreas sembradas, margen bruto, lotes en alerta) y la lista de lotes, del peor al mejor | Desde el ingreso. Sin extras: el campo elegido queda guardado en la sesión |
| `LoteActivity` | El semáforo del lote, el rinde de indiferencia, los números de la proyección y los costos cargados. Botón **Cargar costo** | Desde un lote del panel. Recibe el extra `siembra_id` (el id que se pide a la API) y `lote_nombre` |
| `NuevoCostoActivity` | Formulario del costo: categoría, descripción, cantidad, unidad, precio, moneda y fecha. Valida antes de enviar y hace el `POST` | Desde el lote, con la **Activity Result API**. Recibe `siembra_id` y `lote_nombre`; si la API guarda el costo devuelve `RESULT_OK` y el lote se vuelve a cargar |

<p>
  <img src="docs/capturas/01-ingreso.png" width="200" alt="Ingreso">
  <img src="docs/capturas/02-panel.png" width="200" alt="Panel con el semáforo del campo">
  <img src="docs/capturas/03-lote.png" width="200" alt="Detalle de un lote">
  <img src="docs/capturas/04-nuevo-costo.png" width="200" alt="Nuevo costo">
</p>

## 4. Endpoints que usa la app

Todos existían para el frontend web: **la app no agrega ni modifica endpoints**. Todos salvo el
ingreso llevan `Authorization: Bearer <token>`.

| Método | Ruta | Para qué la usa la app |
| --- | --- | --- |
| POST | `/auth/login` | Ingresar: devuelve `token`, `expira` y `usuario` |
| POST | `/auth/logout` | Salir: invalida el token en la API |
| GET | `/establecimientos` | Los campos del usuario, para el selector del panel |
| GET | `/campanias` | Elegir la campaña abierta más reciente, igual que la web |
| GET | `/lotes?establecimiento_id={id}` | Los lotes del campo elegido |
| GET | `/siembras?campania_id={id}` | Las siembras de la campaña, que se cruzan con los lotes |
| GET | `/siembras/{id}` | El detalle de una siembra: lote, cultivo, campaña y hectáreas |
| GET | `/siembras/{id}/margen` | El semáforo y los números de cada lote |
| GET | `/siembras/{id}/costos` | Los costos cargados en el lote |
| POST | `/siembras/{id}/costos` | **Alta de un costo desde la app** |

Las tres operaciones que pide la consigna: **listar** (lotes y costos), **crear** (costo) y
**detalle por id** (siembra). El costo cargado desde la app queda en la misma base y cambia
el margen del lote, que es lo que muestra el frontend web.

## 5. Servicio cloud

> **Pendiente.** Se eligió **Firebase Cloud Messaging (FCM)** para notificaciones push. Los
> pasos están en [PLAN.md](PLAN.md#servicio-cloud-fcm-r4).

Por qué FCM y no Firebase Authentication: la app ya tiene su ingreso, el de la API. Sumar
Authentication obligaría a ingresar dos veces y no protegería la API, que sigue verificando su
propio token. FCM no se superpone con nada, es gratis y no pide tarjeta.

Cuando esté terminado, acá van a ir qué datos salen de la app y hacia dónde (el token de la
instalación, hacia Firebase) y cómo se mandan los avisos.

## 6. Decisiones de diseño

**Cuatro pantallas, una por tarea.** El ingreso, el panel (todo el campo), el lote (uno solo) y
el alta del costo. Cada Activity tiene una sola responsabilidad y la navegación es la de la
cursada: Intents con extras hacia adelante y la Activity Result API para volver con el
resultado del alta. Lo que en la web es configuración no está en la app, para que siga siendo
simple.

**La API descrita en un solo lugar.** `AgroApi` es la interfaz de Retrofit con todos los
endpoints y `ApiCliente` arma el cliente una sola vez, con la URL base. Sobre el modelo de
AppAlumnos se agregaron dos cosas que pide esta API:
- un interceptor que suma `Authorization: Bearer` a cada pedido, como hace `api.js` en la web;
- una política de Gson que traduce el `snake_case` de la API (`ha_sembradas`) al `camelCase`
  de los modelos (`haSembradas`), sin anotar campo por campo.

**Manejo de errores centralizado.** `LlamadaApi` es el `Callback` de Retrofit con las decisiones
de siempre ya tomadas, para no repetirlas en cada pantalla:
- `onFailure` significa que no hubo respuesta: se muestra «Sin conexión con la API».
- `onResponse` con error muestra el `mensaje` de la API (400, 404, 422). En un 422 marca cada
  campo con lo que la API dice que está mal.
- Un 401 cierra la sesión y vuelve al ingreso.
- Si la Activity ya se cerró, por ejemplo porque se giró la pantalla, la respuesta se ignora.

`ErrorApi` lee el formato de error de esta API: `{"error": {"status", "mensaje", "errores"}}`.

**El semáforo del campo se calcula en la app.** La API calcula el margen de cada lote; el del
campo entero lo arma el frontend web en `dashboard.js`. Para que la app muestre los mismos
números, esa lógica se pasó a Java (`ResumenCampo` y `Semaforo`, con el mismo umbral del 20 %) y
tiene tests de unidad.

**Girar la pantalla.** Las pantallas que solo leen vuelven a pedir los datos en `onCreate`, que
es el criterio de la cursada. El alta del costo es la excepción: el `POST` vive en un
`ViewModel`, que sobrevive al giro. Así el pedido en curso no se pierde y el botón sigue
deshabilitado: no se puede mandar dos veces el mismo costo.

**Identidad visual.**
- La paleta de `colors.xml` usa los mismos nombres que las variables CSS de la web.
- El logo es `logo-aw.svg` pasado a Vector Drawable.
- El velocímetro es una View propia (`GaugeView`) que dibuja los arcos del `<svg>` de la web con
  los mismos ángulos.
- La tipografía es Roboto, que es la que la pila de fuentes de la web termina usando en Android.

**Librerías:**
- **Retrofit 2.11 con Gson**, la versión de la cursada;
- **Lifecycle** (`ViewModel` y `LiveData`), para el alta;
- **Material Components**, para el tema, las tarjetas y los campos de texto.

Las fechas usan `SimpleDateFormat` y no `java.time`, que necesita Android 8.0 y la app arranca
en Android 7.0.

## 7. Limitaciones conocidas

- **FCM todavía no está integrado** (sección 5).
- **No se pueden editar ni borrar costos desde la app**: se hace desde la web. La API ya tiene
  `PUT` y `DELETE /costos/{id}`, así que agregarlo es posible.
- **Al volver al panel se vuelven a pedir todos los datos**, incluido el margen de cada lote:
  un pedido por lote con siembra. Con muchos lotes la carga tarda más.
- **Sin modo oscuro:** la web tampoco lo tiene.
- **El ícono de la app es el del logo solo desde Android 8.0.** En 7.0 y 7.1 se ve el ícono por
  defecto de Android Studio.
- **Después de un error, el aviso de un campo sigue visible** aunque ya se haya corregido, hasta
  que se vuelve a tocar Guardar.
