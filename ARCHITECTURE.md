# Andrómeda — Arquitectura del sistema

> Arquitectura hexagonal (puertos y adaptadores) del sistema completo: app Android + backend en Cloudflare.

## 1. Visión general

```
┌─────────────────────────────┐
│   App Android (Kotlin)      │
│   Jetpack Compose           │
│                             │
│   domain ─ application      │
│        └─ infrastructure    │
└──────────────┬──────────────┘
               │ HTTPS + JWT
               │ polling cada 7 s
               ▼
┌─────────────────────────────┐
│  Cloudflare Workers (Hono)  │
│  TypeScript                 │
│                             │
│   domain ─ application      │
│        └─ infrastructure    │
└──────────────┬──────────────┘
               │ SQL parametrizado
               ▼
        ┌──────────────┐
        │  D1 (SQLite) │
        └──────────────┘
```

### Decisiones

| Decisión | Elección |
|---|---|
| Cliente | Android nativo, Kotlin + Compose |
| Voz | `SpeechRecognizer` + TTS de Android, dentro de la app |
| Backend | Cloudflare Workers + Hono + TypeScript |
| Base de datos | Cloudflare D1 (SQLite) |
| Autenticación | JWT (access + refresh) + PBKDF2 |
| Tiempo real | Polling cada 7 s |
| Dinero | Enteros en centavos, mostrados en **COP** |
| Costo | **$0** en free tier |

> Se descartó el wake word "siempre escuchando" por batería y complejidad. No hay servicios en segundo plano ni overlays.

### Regla de dependencia

```
infrastructure  →  application  →  domain
```

`domain` no conoce Hono, D1, Compose ni Ktor.

## 2. Repositorios

```
registros-gastos-backend/          # Backend
├── migrations/
├── scripts/
├── src/
│   ├── domain/                    # entidades, reglas, puertos in/out
│   ├── application/usecases/      # casos de uso
│   ├── infrastructure/
│   │   ├── http/                  # Hono (entrada)
│   │   ├── d1/                    # repositorios (salida)
│   │   ├── auth/                  # JWT + PBKDF2
│   │   └── config/                # composition root
│   └── index.ts
└── test/

registro-gastos/                   # App Android
├── PLAN.md
├── ARCHITECTURE.md
├── README.md
├── INSTRUCCIONES.md
└── app/src/main/java/com/andromeda/gastos/
    ├── domain/
    ├── application/
    └── infrastructure/
```

## 3. Backend hexagonal

### Estructura

```
src/
├── domain/
│   ├── entities/                  # Gasto, Miembro, Hogar, Usuario
│   ├── enums/                     # Rol
│   ├── rules/                     # GastoReglas, PermisoReglas
│   ├── factories/                 # GastoFactory
│   └── ports/
│       ├── in/                    # RegistrarGastoPort, ConsultarGastosPort...
│       └── out/                   # GastoRepositorioPort, PasswordHashPort...
├── application/
│   └── usecases/                  # implementan los puertos de entrada
├── infrastructure/
│   ├── http/                      # Hono: rutas, auth, errores
│   ├── d1/                        # adaptadores + mappers
│   ├── auth/                      # JwtAdapter, Pbkdf2Adapter
│   └── config/                    # container (composition root)
└── exception/
```

### Puertos

**Entrada (lo que ofrece el sistema):**

- `AutenticacionPort`
- `GestionarHogarPort`
- `GestionarMiembrosPort`
- `RegistrarGastoPort`
- `ActualizarGastoPort`
- `EliminarGastoPort`
- `ConsultarGastosPort`
- `ExportarGastosPort`

**Salida (lo que necesita):**

- `UsuarioRepositorioPort`
- `HogarRepositorioPort`
- `MiembroRepositorioPort`
- `GastoRepositorioPort`
- `RefreshTokenRepositorioPort`
- `PasswordHashPort`
- `TokenPort`

### Contrato de la API

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/register` | Crear usuario |
| `POST` | `/auth/login` | Obtener tokens |
| `POST` | `/auth/refresh` | Renovar access token |
| `POST` | `/households` | Crear hogar (creador = admin) |
| `GET` | `/households` | Listar hogares del usuario |
| `POST` | `/households/join` | Unirse con código |
| `GET` | `/households/:hid` | Datos del hogar |
| `POST` | `/households/:hid/admin-password` | Configurar contraseña admin |
| `GET` | `/households/:hid/members` | Listar miembros |
| `DELETE` | `/households/:hid/members/:mid` | Eliminar miembro (admin) |
| `GET` | `/households/:hid/expenses` | Listar gastos (polling) |
| `POST` | `/households/:hid/expenses` | Registrar gasto |
| `PATCH` | `/households/:hid/expenses/:eid` | Actualizar (admin + `X-Admin-Password`) |
| `DELETE` | `/households/:hid/expenses/:eid` | Eliminar (admin + `X-Admin-Password`) |
| `GET` | `/households/:hid/export.csv` | Exportar CSV (`monto_cop`) |

Todas las rutas salvo `/auth/*` exigen `Authorization: Bearer <token>`.

### Esquema D1

```sql
users (id, email, password_hash, nombre, created_at)
households (id, nombre, owner_id, invite_code, admin_password_hash, created_at)
members (id, household_id, user_id, role, joined_at)
expenses (id, household_id, producto, monto_cents, fecha, created_by, created_at, updated_at)
refresh_tokens (id, user_id, token_hash, expires_at, created_at)
```

Índices: `expenses(household_id, updated_at)`, `members(household_id)`, `refresh_tokens(user_id)`.

### Seguridad

- Contraseñas de usuario y admin con PBKDF2 (Web Crypto).
- Refresh tokens guardados hasheados.
- Editar/eliminar exige rol `ADMIN` + contraseña verificada en el servidor.
- SQL parametrizado (`prepare().bind()`).
- Secretos en `wrangler secret` / `.dev.vars`.

## 4. App Android

### Estructura

```
app/src/main/java/com/andromeda/gastos/
├── domain/
│   ├── entity/                    # Gasto, Miembro, Hogar, Usuario, Sesion
│   ├── enums/                     # Rol
│   ├── rules/                     # GastoReglas, PermisoReglas
│   ├── factories/
│   ├── util/                      # MontoParser, Formato (COP)
│   └── port/
│       ├── portin/
│       └── portout/
├── application/usecase/
├── infrastructure/
│   ├── ui/                        # Compose + ViewModels (entrada)
│   ├── remote/                    # Ktor + API + polling (salida)
│   ├── voz/                       # SpeechRecognizer + TTS
│   ├── local/                     # sesión en DataStore
│   └── export/                    # CSV + FileProvider
├── AppContainer.kt                # composition root
└── MainActivity.kt
```

### Voz

- Motor: `SpeechRecognizer` de Android para transcribir; TTS de Android para hablar.
- Se activa con el botón **"Registrar por voz"**:
  - en la pantalla de inicio, visible apenas se abre la app;
  - en la pantalla de registro, arriba del botón **"Registrar"**.
- Diálogo guiado en `RegistrarGastoPorVozUseCase`:
  1. "¿Qué compraste?" → texto.
  2. "¿Cuánto costó en pesos?" → `MontoParser`.
  3. Confirma y registra.
- No hay servicios en segundo plano, overlays ni modelo Vosk.

### Tiempo real

```kotlin
class GastoApiAdapter(private val api: ApiClient) : GastoRepositorioPort {
    override fun observar(hogarId: String): Flow<List<Gasto>> = flow {
        while (true) {
            emit(listar(hogarId))
            delay(7_000)
        }
    }.flowOn(Dispatchers.IO)
}
```

El `Flow` es lo que ve el dominio; el polling queda oculto en el adaptador. Migrar a WebSocket solo cambia este archivo.

### Editar / eliminar

- La UI pide la contraseña de admin.
- El caso de uso la envía al backend.
- El backend la verifica contra `admin_password_hash`.
- La app no guarda la contraseña de admin.

## 5. Pruebas

| Nivel | Backend | App |
|---|---|---|
| Dominio | Vitest | JUnit |
| Casos de uso | Vitest + fakes | fakes |
| Adaptadores | D1 local (`wrangler dev`) | — |
| E2E | `scripts/smoke.mjs` | Prueba manual en dispositivo |

## 6. Desarrollo y despliegue

### Backend

```bash
cd ../registros-gastos-backend
pnpm install
pnpm db:migrate:local
pnpm dev                 # http://127.0.0.1:8787
pnpm deploy              # wrangler deploy
```

Para dos celulares en LAN:

```bash
pnpm exec wrangler dev --ip 0.0.0.0 --port 8787
```

### App

```bash
cd ../registro-gastos
./gradlew :app:assembleDebug
adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk
```

Para dispositivo físico con backend local:

```bash
adb -s <serial> reverse tcp:8787 tcp:8787
```

La URL base está en `BuildConfig.API_BASE_URL` (`http://127.0.0.1:8787`).

## 7. Costo

| Servicio | Free tier | Uso familiar |
|---|---|---|
| Workers | 100.000 req/día | Muy por debajo |
| D1 | 5M lecturas/día, 100k escrituras/día, 5 GB | Muy por debajo |
| **Total** | **$0** | **$0** |

## 8. Roadmap

- [x] Backend hexagonal con auth, hogares, miembros, gastos y CSV.
- [x] App con tabla, voz en la app, manual, edición/eliminación con contraseña y miembros.
- [ ] Despliegue en Cloudflare.
- [ ] Publicación en Play Store.
- [ ] Auto-renovación de token.
- [ ] Optimización del polling en segundo plano.
- [ ] Análisis de gastos con IA (futuro).
