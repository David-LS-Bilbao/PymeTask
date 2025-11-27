# PymeTask

Aplicación Android (Kotlin) para gestión de tareas, contactos y notas.

Este README recoge:
- cómo compilar y ejecutar el proyecto en Windows (PowerShell)
- la estructura principal del repositorio
- permisos y consideraciones (notificaciones, SMS, contactos, alarmas)
- problemas detectados y soluciones inmediatas (incluyendo el arreglo en `AndroidManifest.xml`)
- recomendaciones y pasos para depurar errores recurrentes

---

Índice
- [Requisitos](#requisitos)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Compilar y ejecutar](#compilar-y-ejecutar)
- [Ejecutar en emulador / dispositivo](#ejecutar-en-emulador--dispositivo)
- [Ejecutar tests](#ejecutar-tests)
- [Permisos importantes](#permisos-importantes)
- [Cambios aplicados (rápido)](#cambios-aplicados-rápido)
- [Problemas detectados y soluciones recomendadas](#problemas-detectados-y-soluciones-recomendadas)
- [Depuración y logcat](#depuracion-y-logcat)
- [Contribuir](#contribuir)
- [Licencia](#licencia)

---

Requisitos
----------
- JDK 11+ (o la versión requerida por el Gradle Wrapper del proyecto)
- Android SDK (API level según `compileSdkVersion` del proyecto)
- Android Studio (recomendado) o usar `gradlew` desde PowerShell
- Conexión a internet para dependencias (la primera vez)

Estructura del repositorio (resumen)
-----------------------------------
- `app/` - módulo principal Android
  - `src/main/AndroidManifest.xml` - manifiesto de la aplicación
  - `src/main/java/...` - código Kotlin del app (UI, presentation, utils...)
  - `build/` - artefactos de compilación (generado)
- `build.gradle.kts`, `settings.gradle.kts`, `gradlew` - configuración y wrapper
- `docs/`, `test-results/` - documentación y resultados de prueba

Compilar y ejecutar
-------------------
Desde PowerShell en la raíz del proyecto:

```powershell
# Compilar APK de debug
.\gradlew assembleDebug

# Instalar en dispositivo conectado (debug)
.\gradlew installDebug

# Limpieza y rebuild
.\gradlew clean assembleDebug
```

Si usas Android Studio, abre la carpeta `PymeTask` y deja que IDE sincronice Gradle.

Ejecutar en emulador / dispositivo
---------------------------------
- Asegúrate de tener un emulador con Google Play o un dispositivo físico conectado.
- Para problemas con envío de SMS/Email en emulador: muchos emuladores no traen apps de SMS o clientes de email instalados por defecto. Prueba con:
  - un emulador con Google Play o una imagen con apps preinstaladas
  - un dispositivo físico (recomendado para pruebas de intents de SMS/MMS)

Ejecutar tests
--------------
Unit tests:
```powershell
.\gradlew test
```
Tests instrumentados (requieren emulador o dispositivo):
```powershell
.\gradlew connectedAndroidTest
```

Permisos importantes
--------------------
El manifiesto del proyecto declara permisos para:
- Notificaciones: `POST_NOTIFICATIONS`
- Lectura de contactos: `READ_CONTACTS`
- Acceso a Internet
- Alarmas exactas: `SCHEDULE_EXACT_ALARM`
- Vibración
- Localización (coarse / fine)

Lee y solicita permisos en runtime cuando sea necesario (Android 6+). Para notificaciones en Android 13+ es necesario pedir `POST_NOTIFICATIONS` explícitamente.

Cambios aplicados (rápido)
--------------------------
- Se corrigió la estructura de `app/src/main/AndroidManifest.xml` que contenía un cierre prematuro de la etiqueta `<application>` y un atributo sobrante, provocando el error:
  "El tipo de elemento \"application\" debe finalizar por la etiqueta final coincidente \"</application>\"."

  - Añadí la declaración de la activity lanzadora `.main.MainActivity` con su `intent-filter` MAIN/LAUNCHER dentro de `<application>`.
  - Verifiqué el manifiesto con las comprobaciones disponibles y no quedaron errores de sintaxis.

Problemas detectados y soluciones recomendadas
---------------------------------------------
A continuación se listan los problemas que me comentaste y recomendaciones prácticas para resolverlos o investigarlos más a fondo.

1) Valor 0.00 en movimientos al crear/editar
- Síntoma: al crear un movimiento, a veces aparece como `0.00` y hay que editarlo manualmente.
- Causa probable: parsing del input numérico que falla por el separador decimal (coma `,` vs punto `.`) o porque el campo vacío se interpreta como 0.
- Soluciones recomendadas:
  - En los `TextField` donde se captura el importe, normalizar la cadena antes de parsear: reemplazar `,` por `.` y luego `toDoubleOrNull()`.

    Ejemplo (Kotlin - Compose):
    ```kotlin
    val raw = importeText
    val normalized = raw.replace(',', '.')
    val value = normalized.toDoubleOrNull() ?: 0.0
    ```

  - Validar que el campo no esté vacío antes de crear el movimiento y mostrar un error si no es válido.
  - Como alternativa (UI), usar `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)` o `KeyboardType.Decimal` y aplicar un filtro de entrada que reemplace `,` por `.` al teclear.
  - Nota: no es fiable "deshabilitar la tecla coma" en todos los teclados; es mejor normalizar el input.

2) Tecla coma en el teclado y casting
- Recomendación: interceptar/normalizar la cadena en el listener del TextField y forzar el cast seguro, p.ej. `text.replace(',', '.')`.
- Para evitar la coma visualmente en algunos teclados: usar `KeyboardType.Number` (aunque puede variar según el IME).

3) Editar contactos — botón eliminar
- Requisito: añadir un botón para eliminar el contacto desde la pantalla de edición.
- Recomendaciones de implementación:
  - En `EditarContactoScreen` añadir un botón (p. ej. IconButton con trash) que llame a la función de ViewModel para borrar el contacto.
  - Confirmar acción con un diálogo `AlertDialog` antes de eliminar.
  - Después de eliminar, navegar hacia atrás o mostrar Toast.

4) Envío de nota por SMS / intent que rompe la app
- Síntoma: "no reconoce ninguna aplicación para enviar sms ni email en el emulador" y el intento rompe la app cuando seleccionas el programa.
- Causa probable: emulator sin apps, o el PendingIntent/Intent usado no es correcto (flags, uri, o formato del extra).
- Recomendaciones:
  - Probar en dispositivo físico o en emulador con Play Store y app de Mensajes instalada.
  - Validar el Intent de envío. Para SMS usar:
    ```kotlin
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:$phoneNumber")
        putExtra("sms_body", message)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // manejar ausencia de app
    }
    ```
  - Antes de startActivity, comprobar `resolveActivity()` para evitar el crash cuando no hay apps que gestionen el intent.

5) Crash al elegir app para enviar SMS (app rompe)
- Diagnóstico: revisar `logcat` para la excepción exacta (stacktrace). Suele ser ActivityNotFoundException si intent mal formado, o SecurityException si falta permiso.
- Pasos de depuración:
  - Reproducir con el emulador y revisar `logcat` filtrando por el package de la app o por "AndroidRuntime".
  - Añadir manejo defensivo con `resolveActivity()` como se indica arriba.

6) Notificaciones de alarmas que suenan pero no aparecen en el terminal/logcat
- Comportamiento: la alarma suena pero no se ve la notificación en el panel ni en logcat.
- Comprobaciones:
  - Revisar `NotificationHelper.kt` y confirmar que se crea/registran los canales (API >= 26).
  - Asegurarse de que `NotificationManager.notify(id, notification)` sea llamado y el id sea único/conocido.
  - Revisar `AlarmReceiver` / `DismissAlarmReceiver` para comprobar que su `onReceive()` crea la notificación.
  - Revisar permisos de notificación y `POST_NOTIFICATIONS` (Android 13+): si no se concedió, no aparecerán notificaciones.

7) Accionar notificación debería cancelar alarma o abrir tarea
- Mejora propuesta:
  - En la notificación, agregar acciones (botones) con PendingIntents: "Cancelar alarma" y "Abrir tarea".
  - Para abrir la app en la tarea específica, la `PendingIntent` puede ser un `getActivity()` apuntando a `MainActivity` con extras (id tarea) y `flags = PendingIntent.FLAG_UPDATE_CURRENT`.
  - Para cancelar la alarma, enviar `PendingIntent.getBroadcast()` a un `BroadcastReceiver` (p. ej. `DismissAlarmReceiver`) que cancele la alarma y actualice estado.

Depuración y logcat
-------------------
- Para ver logs en PowerShell:
```powershell
# Mostrar logs (con dispositivo/emulador conectado)
.\gradlew logcat
```
O desde Android Studio, usa Logcat tool window y filtra por el paquete `com.dls.pymetask` o por `AndroidRuntime`.

- Recomendación: cuando algo "rompe" al seleccionar una app para SMS/Email, reproduzca el crash y copie el stacktrace completo de Logcat para localizar la excepción exacta.

Buenas prácticas y checks adicionales
------------------------------------
- Añadir comprobaciones con `resolveActivity()` antes de `startActivity()` para intents implícitos.
- Manejar locales en parsing numérico: normalizar separador decimal.
- Crear un channel de notificaciones en la inicialización de la app (`Application.onCreate`) y comprobar que el usuario haya concedido permisos de notificación.
- Mantener los `BroadcastReceiver` registrados en el manifiesto o dinámicamente según necesidad y con `android:exported` correcto para Android 12+.

Sugerencias de próximas tareas (priorizadas)
-------------------------------------------
1. Fijar parsing de importes (coma -> punto) en `CrearMovimientoScreen.kt` y `EditarMovimientoScreen.kt` (alta prioridad).
2. Añadir `resolveActivity()` defensivo en los intents de SMS/Email y probar en device físico (alta prioridad).
3. Implementar botón "Eliminar" en `EditarContactoScreen` con diálogo de confirmación (media).
4. Añadir acciones en la notificación de alarma para "Cancelar" y "Abrir" (media).
5. Crear tests unitarios para el parsing de importes y tests instrumentados para flujo de notificación (opcional).

Contribuir
----------
Si quieres que haga los cambios de código (p. ej. aplicar reemplazo `,` -> `.` en los Screens, añadir el botón eliminar contacto, o mejorar la notificación con acciones), indícalo y los aplicaré directamente en el código, ejecutaré las pruebas y validaré la build.

Licencia
--------
Revisa el fichero `LICENSE` incluido en el repositorio.

Contacto
--------
Si quieres que haga alguno de los cambios listados (o todos), dime por cuál empezar y lo implemento y pruebo localmente en el workspace.

---

Notas finales
-------------
He incluido instrucciones concretas y fragmentos de código mínimos para las correcciones más urgentes. Dime si quieres que aplique ya alguna de las soluciones (por ejemplo: 1) editar `CrearMovimientoScreen.kt` y `EditarMovimientoScreen.kt` para normalizar el separador decimal, 2) añadir botón de eliminar contacto en `EditarContactoScreen`, o 3) añadir acciones en la notificación de alarma). Haré los cambios, ejecutaré compilación y pruebas unitarias en el workspace y te regresaré con los resultados.

