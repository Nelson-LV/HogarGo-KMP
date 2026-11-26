# HogarGo KMP

Microproyecto Kotlin Multiplatform (Android + iOS) de la electiva *Desarrollo de Aplicaciones Moviles* — Universidad del Cauca.
Version multiplataforma de HogarGo: organiza finanzas, mascotas y calendario del hogar.

## Arquitectura

**Compose Multiplatform**: UI, navegacion, ViewModels y datos viven en `commonMain`; cada plataforma solo aporta su punto de entrada y el driver SQLite.

```
composeApp/src/
  commonMain/   App, navegacion (Navigation Compose), ui/ (tema + pantallas), data/ (repositorio SQLDelight), sqldelight/ (esquema .sq)
  androidMain/  MainActivity, manifest, driver Android (AndroidSqliteDriver)
  iosMain/      MainViewController, driver iOS (NativeSqliteDriver)
iosApp/         Host SwiftUI minimo (project.yml para XcodeGen)
```

| Requisito de la guia | Donde |
|---|---|
| Android + iOS | `androidMain`, `iosMain`, `iosApp/` |
| >= 5 pantallas | Bienvenida, Crear/Unirse/Iniciar sesion/Recuperar codigo, Perfil, Inicio, Finanzas, Mascotas, Calendario, Acerca de |
| Persistencia | SQLDelight (`HogarGo.sq`, `HogarGoRepository`) |
| Navegacion compartida | `navigation/AppNavHost.kt` |
| Descripcion y creditos | `AboutScreen` en `ui/screens/Screens.kt` |

## Hogares (modulo de Isabella Romero)

Port del modulo de hogares de HogarGo iOS, todo en `commonMain/.../household/`:

- `data/`: esquema `Household.sq` (Household, Member, HouseholdNotice), `HouseholdRepository` con todas las reglas (crear, unirse con aprobacion, iniciar sesion por nombre o usuario de recuperacion, recuperar codigos, acciones de admin, salir con traspaso de admin), `SessionStore` (tabla `Setting`) y SHA-256 en Kotlin comun para el usuario de recuperacion.
- `HouseholdViewModel`: sesion del dispositivo, entrega de avisos y cierre de sesion si el admin te elimina.
- `notifications/`: avisos del hogar como notificaciones locales (`expect`/`actual`: NotificationManager en Android, UNUserNotificationCenter en iOS).
- `ui/`: Bienvenida, Crear hogar, Unirse, Iniciar sesion, Recuperar codigo, Perfil y sus editores. Textos ES/EN con `L10n.tr(es, en)`.

Todo dato del hogar lleva `householdId`. Si tu modulo guarda datos por hogar, borralos tambien en `HouseholdRepository.deleteEverything` (cuando sale la ultima persona).

Pruebas: `./gradlew :composeApp:testDebugUnitTest` (reglas comunes + repositorio con SQLite en memoria).

Si ya tenias instalada la version esqueleto, desinstalala antes: el esquema cambio y no hay migracion.

## Finanzas, Mascota y Calendario (modulo de Nelson)

Port a KMP de las tres pantallas de HogarGo Android/iOS, con las mismas reglas. Cada modulo vive en su paquete de `commonMain` (`finance/`, `pet/`, `calendar/`, `home/`) con esquema `.sq` propio, reglas puras, repositorio SQLDelight, ViewModel y pantalla Compose. Todo dato lleva `householdId`.

- **Finanzas**: gastos (coma o punto decimal), metas de ahorro con aportes y frasco, resumen y desglose del mes, lista de gastos recientes.
- **Mascota (Zori)**: decaimiento 0.05 cada 2 h, +0.15 por accion con cooldown de 15 min (alimentar y jugar independientes), nivel = 1 + acciones / 5, armario con monedas, el aviso de cooldown se actualiza solo. La imagen del zorro es un recurso compartido de Compose (`composeResources/`).
- **Calendario**: cuadricula del mes (semana desde domingo), puntos de factura/evento/tarea, detalle del dia, proximos eventos y facturas.
- **Inicio**: resumen con Zori, gasto del mes, proximo evento y facturas por pagar.
- **Borrado en cascada**: al salir la ultima persona se borran los datos de estos modulos (`HouseholdRepository.deleteEverything`).

Las monedas de Zori y las tareas del calendario salen de la tabla `Task` del modulo de Tareas: el calendario muestra las pendientes asignadas a ti (en su fecha limite) y las completadas (en el dia en que se completaron).

## Tareas, Racha Diaria y Consejo del Día (módulo de Jhonatan Palacios)

Implementación multiplataforma del módulo de gestión de tareas del hogar, motivación y buenos hábitos familiares (`tasks/` y `advice/` en `commonMain`):

- **Tareas del Hogar (`Tasks.sq`, `TaskRepository`)**:
  - Tabla `Task`: `id`, `householdId`, `title`, `category`, `assignedMemberId`, `assignedMemberName`, `assignedToOther`, `coinReward`, `dueDate`, `dueTime`, `isCompleted`, `completedAt`, `createdAt`.
  - Consultas reactivas en tiempo real: filtrado por todas, pendientes, hechas, contador y eliminación individual/por hogar.
- **Racha y Recompensas (`TaskViewModel`)**:
  - Cálculo de racha de tareas completadas del hogar y acumulación de monedas ganadas (🪙).
  - Integrado con la mascota Zori para el canje de recompensas y el progreso del equipo.
- **Recordatorio de Racha antes de las 8:00 PM**:
  - Programación de notificaciones locales recurrentes (`AlarmManager` en Android con `StreakReminderReceiver`, `UNCalendarNotificationTrigger` en iOS).
  - Alerta interactiva en `HomeScreen` y `TasksScreen` con conteo de tareas pendientes del día.
- **Consejo del Día (`advice/` con Ktor KMP)**:
  - Consumo HTTP asíncrono multiplataforma de `https://api.adviceslip.com/advice` (OkHttp en Android, Darwin en iOS).
  - Soporte de modo sin conexión / fallback automático con banco de consejos locales de convivencia y trabajo en equipo.
  - Pruebas unitarias con `MockEngine` (`DailyAdviceRepositoryTest`).

## Modo oscuro

La app sigue el modo claro u oscuro del sistema en ambas plataformas: `HogarGoTheme` elige el esquema de Material y la paleta `HogarGoColors` cambia con el. Usa esos colores (no valores fijos) para que las pantallas nuevas se vean bien en los dos modos.

## Ejecutar

**Android** (JDK 17 + Android SDK):

```
./gradlew :composeApp:installDebug
```

**iOS** (requiere macOS con Xcode):

```
brew install xcodegen
cd iosApp && xcodegen
open iosApp.xcodeproj
```

Luego ejecutar el esquema `iosApp` en un simulador.

## Flujo de trabajo

`main` contiene la base compartida. Cada integrante trabaja en su propia rama (`nombre-apellido`) y abre PR hacia `main`.

## Créditos

- Jhonatan Palacios Gómez — Módulo de Tareas, Racha Diaria y Consejo del Día
- Nelson Rodrigo López Vidales — Finanzas, Mascota y Calendario
- Dana Isabella Romero Núñez — Módulo de Hogares y Sesión
