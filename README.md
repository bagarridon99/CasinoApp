# CasinoApp

Aplicación móvil desarrollada en **Kotlin** con **Jetpack Compose** que simula un entorno de casino digital, integrando autenticación local, manejo de datos persistentes con **Room**, navegación dinámica, gestión de estado y notificaciones nativas (bonos, rachas y recompensas).

---

## Características principales

### Interfaz moderna con Material 3
- Diseño adaptado a **Material Design 3**.
- Paleta de colores dinámica.
- Composables reutilizables para pantallas y formularios.

### Autenticación local (Room)
- Registro y login persistente usando **Room Database**.
- Validaciones de formulario en tiempo real.
- Gestión de sesión con `CasinoViewModel`.

### Almacenamiento local
- Base de datos `casino_db` creada con **Room**.
- DAO y entidades personalizadas para usuarios y balance.
- Persistencia de datos entre sesiones.

### Notificaciones nativas
- Implementación de **NotificationManager** con canales separados:
  - Bonos y Recompensas  
  - General
- Notificaciones locales automáticas:
  - Bono diario disponible (+20%).
  - Racha activa (partidas restantes).
  - Bono por ciudad (por ejemplo, “Disponible en Maipú”).

### Navegación
- Estructura basada en `NavHost` y `composable` routes.
- Pantallas principales:
  - `LoginScreen`
  - `SignUpScreen`
  - `HomeScreen`

### Recursos nativos utilizados
- Notificaciones del sistema (Android Notifications API).
- Almacenamiento local mediante Room DB.

---

## Tecnologías y dependencias

| Módulo | Librería | Uso |
|--------|-----------|-----|
| UI | **Jetpack Compose + Material3** | Interfaz visual y componentes |
| Navegación | **Navigation Compose** | Flujo entre pantallas |
| Persistencia | **Room Database** | Registro y login local |
| Estado | **ViewModel + State Hoisting** | Gestión de estado |
| Notificaciones | **NotificationManager + PendingIntent** | Bonos y alertas locales |

---

## Estructura del proyecto

app/
├─ data/
│ ├─ dao/ → Interfaces DAO de Room
│ ├─ entity/ → Entidades (UserEntity)
│ └─ AppDatabase.kt → Configuración de Room
│
├─ notification/
│ ├─ NotifyHelper.kt → Canales y envío de notificaciones
│ ├─ ReminderScheduler.kt → Planificación futura (si aplica)
│ └─ AskNotificationsPermissionOnce.kt → Permiso dinámico
│
├─ view/
│ ├─ LoginScreen.kt
│ ├─ SignUpScreen.kt
│ └─ HomeScreen.kt
│
├─ viewmodel/
│ └─ CasinoViewModel.kt
│
├─ MainActivity.kt → Navegación principal
└─ ...


---

## Ejecución

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/bagarridon99/CasinoApp.git
