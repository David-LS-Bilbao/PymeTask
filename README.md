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


## 🧪 Informes de tests (GitHub Pages)

[![Pages](https://img.shields.io/badge/Docs-GitHub%20Pages-blue)](https://david-ls-bilbao.github.io/PymeTask/)

Todos los reportes HTML generados (Agenda, NotaForm, DetalleContacto, Weather, etc.) están publicados en el índice de GitHub Pages:

➡️ **Ver informes:** https://david-ls-bilbao.github.io/PymeTask/

> Los archivos se sirven desde `docs/test-results/` y el índice se genera en `docs/index.html`.  
> Puedes añadir/renombrar informes y el índice seguirá funcionando sin tocar el README.
