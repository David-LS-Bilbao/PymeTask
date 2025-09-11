# PymeTask

Gestor integral para pequeñas y medianas empresas, desarrollado en **Kotlin** con **Jetpack Compose**.  
Incluye módulos para **autenticación, contactos, notas, agenda/tareas, movimientos económicos, archivos, estadísticas, ajustes y clima**.

---

## ✨ Funcionalidades principales
- **Autenticación**: registro/login con email y Google (Firebase Auth).
- **Contactos**: CRUD con foto (Storage), llamadas, SMS, WhatsApp, email.
- **Notas**: editor con colores, deshacer/rehacer, compartir.
- **Tareas (Agenda)**: alarmas, marcado como completadas, filtros por día/semana/mes.
- **Movimientos**: ingresos/gastos, importación CSV, totales y saldo.
- **Archivos**: gestión en carpetas, subir/bajar, abrir, compartir, eliminar.
- **Estadísticas**: gráficos de ingresos/gastos, comparativas mensuales.
- **Ajustes**: idioma, tema, moneda, fecha, recordatorios, escalado de texto.
- **Clima**: bloque en Dashboard con clima actual y previsión semanal.

---

## 🏛️ Arquitectura
- **Clean Architecture** + **MVVM**
- **Hilt** para inyección de dependencias
- **Kotlin Coroutines + Flow**
- **Jetpack Compose Navigation**

Estructura de carpetas (resumen):contentReference[oaicite:0]{index=0}:

## 🧱 Requisitos

- Android Studio **Narwhal 2025.1.1** (K2)
- JDK **21**
- Android SDK: `compileSdk = 35`, `minSdk = 24`  <!-- ajusta si difiere -->
- Gradle wrapper incluido en el repo

## 🔐 Configuración (Firebase)

1. Crea un proyecto en Firebase y una app Android con tu **applicationId**.
2. Activa **Authentication** (Email/Password y Google), **Firestore** y **Storage**.
3. Descarga `google-services.json` y colócalo en `app/`.
4. (Google Sign-In) Añade **SHA-1** de tu firma debug/release en Firebase.
5. Lanza la app: login/registro deberían funcionar.

## 🔒 Permisos y privacidad

La app usa únicamente los permisos necesarios:
- `INTERNET` y `ACCESS_NETWORK_STATE` (conectividad y Firebase).
- `ACCESS_COARSE_LOCATION` / `ACCESS_FINE_LOCATION` (bloque de clima).
- `POST_NOTIFICATIONS` (Android 13+, para recordatorios/alarma).
- **Opcional** `READ_CONTACTS` (solo si importas contactos del dispositivo).

Para llamadas, SMS, WhatsApp y email se usan **Intents** del sistema (sin enviar nada automáticamente).



## 🧪 Informes de tests (GitHub Pages)

[![Pages](https://img.shields.io/badge/Docs-GitHub%20Pages-blue)](https://david-ls-bilbao.github.io/PymeTask/)

Todos los reportes HTML generados (Agenda, NotaForm, DetalleContacto, Weather, etc.) están publicados en el índice de GitHub Pages:

➡️ **Ver informes:** https://david-ls-bilbao.github.io/PymeTask/

> Los archivos se sirven desde `docs/test-results/` y el índice se genera en `docs/index.html`.  
> Puedes añadir/renombrar informes y el índice seguirá funcionando sin tocar el README.
>
> ## 🧪 Tests

- Unit tests: `./gradlew testDebugUnitTest`
- Instrumented: `./gradlew connectedDebugAndroidTest`

**Informes HTML** publicados en GitHub Pages:
➡️ https://david-ls-bilbao.github.io/PymeTask/


## 📦 Builds

- Debug APK: `./gradlew assembleDebug`
- Release APK (firmada): `./gradlew assembleRelease`  
  El APK queda en `app/build/outputs/apk/release/`.


  ## 🔖 Licencia
MIT. Ver archivo [LICENSE](./LICENSE).

## 👤 Autor
David López Sotelo — contacto: (lopezsotelo77@gmail.com)



