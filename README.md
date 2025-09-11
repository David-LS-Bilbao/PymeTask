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
