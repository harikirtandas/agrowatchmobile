# AgroWatch Mobile

Cliente Android (Java) de la API [agrowatch-api](https://github.com/harikirtandas/agrowatch-api).

No replica el frontend web: es una versión mobile simple, con la misma paleta, logo y semáforo.

## Alcance

- Ingreso con el usuario de la API.
- Panel con el semáforo del campo y la lista de lotes.
- Detalle de cada lote y carga de costos.
- Notificaciones push con FCM (pendiente).

Toda la configuración (establecimientos, sensores, usuarios, etc.) sigue haciéndose desde el cliente web.

## Requisitos

- Android Studio
- minSdk 24

## Cómo abrirlo

1. Clonar el repo.
2. En Android Studio: **File → Open** y elegir esta carpeta.
3. Esperar el Gradle Sync y ejecutar el módulo `app`.

La URL de la API está en un solo lugar: `ApiCliente.URL_BASE`
(`app/src/main/java/com/example/agrowatchmobile/api/ApiCliente.java`).

El plan completo, con las pantallas, los endpoints y el cronograma, está en [PLAN.md](PLAN.md).
