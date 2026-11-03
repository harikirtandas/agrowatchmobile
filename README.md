# AgroWatch Mobile

Cliente Android (Java) de la API [agrowatch-api](https://github.com/harikirtandas/agrowatch-api).

No replica el frontend web: es una opción mobile de **solo consulta** y lo más simple posible.

## Alcance

- Login contra la API existente.
- Dashboard simple de visualización.
- Toma de notas.

Toda la configuración (establecimientos, sensores, usuarios, etc.) sigue haciéndose desde el cliente web.

## Requisitos

- Android Studio
- minSdk 24

## Cómo abrirlo

1. Clonar el repo.
2. En Android Studio: **File → Open** y elegir esta carpeta.
3. Esperar el Gradle Sync y ejecutar el módulo `app`.
