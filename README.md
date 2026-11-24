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
| >= 5 pantallas | Bienvenida, Inicio, Finanzas, Mascotas, Calendario, Acerca de |
| Persistencia | SQLDelight (`HogarGo.sq`, `HogarGoRepository`) |
| Navegacion compartida | `navigation/AppNavHost.kt` |
| Descripcion y creditos | `AboutScreen` en `ui/screens/Screens.kt` |

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
Las pantallas Finanzas, Mascotas y Calendario son esqueletos con un contador persistido para validar SQLDelight; se reemplazan en cada rama.

## Creditos

- Nelson Lopez Vidales
- (agregar integrantes)
