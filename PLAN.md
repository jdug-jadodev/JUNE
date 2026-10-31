# Andrómeda — Plan de sistema (MVP familiar)

> App para registrar y compartir los gastos de la familia, con voz dentro de la app, tabla en tiempo real y backend en Cloudflare.

## 1. Objetivo

- Registrar compras **por voz** con el botón **"Registrar por voz"** (visible en el inicio y en la pantalla de registro).
- Ver la tabla de gastos (fecha, producto, valor) en tiempo real.
- Registro manual disponible siempre.
- Invitaciones para miembros de la familia.
- **Editar/eliminar solo el administrador**, con contraseña.
- Exportar CSV.
- Montos en **pesos colombianos (COP)**.
- Costo de infraestructura: **$0**.

## 2. Stack tecnológico

| Capa | Tecnología | Costo |
|---|---|---|
| App | Kotlin + Jetpack Compose | $0 |
| Voz | `SpeechRecognizer` + TTS de Android | $0 |
| Diálogo | `RegistrarGastoPorVozUseCase` | $0 |
| Backend | Cloudflare Workers + Hono (TypeScript) | $0 |
| Base de datos | Cloudflare D1 (SQLite) | $0 |
| Autenticación | JWT (access + refresh) + PBKDF2 | $0 |
| Tiempo real | Polling cada 7 s | $0 |
| Exportación | CSV generado por el backend | $0 |

> **Decisión:** se descartó el modo "siempre escuchando" (wake word) por consumo de batería y complejidad. La voz se activa manualmente dentro de la app.

## 3. Flujo de voz

```
Inicio / Registrar
  └─ "Registrar por voz"
       ├─ "¿Qué compraste?"        → SpeechRecognizer
       ├─ "¿Cuánto costó en pesos?" → SpeechRecognizer + MontoParser
       ├─ "¿Registro X por $Y COP?" → confirma
       └─ POST /households/:hid/expenses (con el token guardado)
```

- Si no entiende, vuelve a preguntar (hasta 2 intentos).
- La fecha se asigna automáticamente (hoy).

## 4. Modelo de datos (Cloudflare D1)

- `users` (id, email, password_hash, nombre, created_at)
- `households` (id, nombre, owner_id, invite_code, admin_password_hash, created_at)
- `members` (id, household_id, user_id, role, joined_at)
- `expenses` (id, household_id, producto, monto_cents, fecha, created_by, created_at, updated_at)
- `refresh_tokens` (id, user_id, token_hash, expires_at, created_at)

Los montos se guardan en **centavos** (`monto_cents`) y se muestran en **COP**.

## 5. Pantallas

1. **Inicio**
   - Tabla de gastos: *Fecha · Producto · Valor*.
   - Botón **"Registrar por voz"** visible apenas se abre la app.
   - FAB **"+"** para registro manual.
   - Editar/eliminar solo si eres admin (pide contraseña).

2. **Registrar**
   - **"Registrar por voz"** arriba.
   - **"Registrar"** manual abajo.
   - Campos: producto, monto en pesos, fecha.

3. **Miembros**
   - Código de invitación, lista de miembros y eliminación (admin).

4. **Ajustes**
   - Configurar contraseña de administrador.
   - Exportar CSV.
   - Cerrar sesión.

## 6. Seguridad

- Login con email + contraseña; JWT (access + refresh).
- Contraseñas con PBKDF2.
- Editar/eliminar exige rol `ADMIN` **y** contraseña verificada en el backend.
- La app no guarda la contraseña de admin; solo el token de sesión.
- SQL siempre parametrizado (`prepare().bind()`).

## 7. Costo

| Servicio | Free tier | Uso familiar |
|---|---|---|
| Workers | 100.000 req/día | Muy por debajo |
| D1 | 5M lecturas/día, 100k escrituras/día, 5 GB | Muy por debajo |
| **Total** | **$0** | **$0** |

## 8. Prueba con dos celulares en local

1. Exponer el backend en la LAN:
   ```bash
   pnpm exec wrangler dev --ip 0.0.0.0 --port 8787
   ```
2. Obtener la IP LAN del PC (`ipconfig`).
3. Configurar la app con `http://<IP-LAN>:8787`.
4. Instalar el APK en el **A56** (`<serial_del_A56>`) y en el segundo celular (misma Wi-Fi).
5. El A56 crea el hogar, comparte el código y el segundo se une.
6. Ambos registran gastos y verifican la sincronización (polling 7 s).

## 9. Estado de implementación

### Hecho y probado en dispositivo real

- **Backend**: Cloudflare Workers + Hono + D1, arquitectura hexagonal.
  - Auth JWT + refresh, PBKDF2.
  - Hogares, invitaciones, miembros, gastos (CRUD).
  - Editar/eliminar con admin + contraseña.
  - Export CSV (`monto_cop`).
  - 9 pruebas y smoke test E2E.
- **App Android**: Kotlin + Compose, arquitectura hexagonal.
  - Login, registro y sesión persistente.
  - Crear hogar o unirse con código.
  - Tabla de gastos con polling cada 7 s.
  - Botón de voz en el inicio y en registrar.
  - Registro manual.
  - Editar/eliminar solo admin con contraseña.
  - Miembros, copiar invitación y eliminar miembros.
  - Exportar CSV y compartir.
  - 13 pruebas de dominio.

### Pendiente (requiere tu intervención)

- **Cloudflare**: crear la base D1 remota, configurar `JWT_SECRET` y `wrangler deploy`.
- **Play Store**: cuenta de desarrollador para publicar.
- **Optimizaciones**: auto-renovación de token y pausar el polling en segundo plano.

### Credenciales de prueba (D1 local)

- Admin: `<email_admin_local>` / `<contraseña_local>` · contraseña admin: `<contraseña_admin_local>`
- Miembro: `<email_miembro_local>` / `<contraseña_local>`
- Hogar: `<nombre_hogar>` · código `<código_invitación>`
- Gasto de prueba: `<producto> · <monto> COP · <miembro>`

> Las credenciales reales se guardan localmente y **no se versionan**.
