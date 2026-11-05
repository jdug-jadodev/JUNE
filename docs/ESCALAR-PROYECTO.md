# Escalar Cauce — plan de producto, costos y capa paga

> Documento vivo. Última actualización: noviembre 2026.
> Complementa `docs/MODO-NEGOCIO.md` (especificación futura del segmento negocio).

---

## 1. Resumen ejecutivo

Cauce es una app de **educación financiera**, no un banco ni una cartera. No fija metas, no pide ingresos en el hogar y no regaña. Muestra patrones, explica el porqué y deja que la persona decida.

El negocio se sostiene con tres capas:

| Capa | Rol | Ingreso |
|---|---|---|
| **Gratis** | Misión: que cualquier hogar de Latinoamérica pueda entender sus gastos | Anuncios recompensados |
| **Plus Hogar** | Profundidad: IA, historial, comparaciones | Suscripción baja |
| **Negocio** | Sostenibilidad: ventas, gastos y margen para pequeños negocios | Suscripción media |

La IA no es el producto: es una capa de explicación sobre un reporte que ya funciona sin ella. Si la IA falla o se agota el presupuesto, la app sigue siendo útil.

---

## 2. Principios

1. **Registrar en segundos.** Monto + nota o voz. Nunca un formulario largo.
2. **Categorías invisibles.** Se deducen; el usuario no las elige al registrar.
3. **Sin culpa.** Se compara contra el pasado propio, no contra una meta impuesta.
4. **El total grande no vive en el inicio.** Inicio = acción; análisis = reflexión.
5. **Nada esencial detrás del muro.** Registrar y ver lo básico es gratis para siempre.
6. **Un solo pago por hogar.** La suscripción cubre a toda la familia.
7. **Presupuesto de IA con corte automático.** Nunca una factura sorpresa.

---

## 3. Planes

### Gratis (para siempre)
- Registro ilimitado de gastos (texto y voz).
- Ciclo actual: total y patrones básicos.
- **2 análisis con IA por ciclo.**
- 1 hogar, hasta 3 miembros.
- Exportar el ciclo actual.
- Modo discreto.
- Anuncios recompensados opcionales.

### Plus Hogar
- Análisis con IA ilimitado (uso razonable, con caché).
- Historial completo y comparación entre ciclos.
- Todos los gráficos y el simulador “¿y si…?”.
- Hasta 8 miembros.
- Exportar todos los ciclos.
- Sin anuncios.

### Negocio
- Todo lo de Plus.
- Modo negocio: ventas + gastos, margen, top productos.
- Fiado (fase posterior).
- Hasta 2 espacios (casa + negocio).
- Sin anuncios.

---

## 4. Costos y presupuesto de IA

### Costo real por análisis

Con DeepSeek V4.1 Flash y un payload de ~1.700 tokens de entrada + ~700 de salida:

| Escenario | Costo por análisis |
|---|---|
| Hora normal (off-peak) | ~2,3 COP |
| Hora pico | ~4,6 COP |

### Presupuesto

- Tope mensual: **$5 USD** (≈17.000 COP).
- Alcanza para **~3.000–7.000 análisis**.
- 100 usuarios × 2 análisis = 200 → **230–460 COP/mes**.

### Guardrails obligatorios

- Tabla `ai_usage` con tokens y costo por mes.
- **Corte automático al 80%** ($4). La app cae al reporte determinista y avisa cuándo se reanuda.
- Cuota por usuario: 2 análisis por ciclo en Gratis.
- Caché por huella de datos (`count` + `max(updated_at)`): no se paga dos veces lo mismo.
- Clasificación de productos nuevos **en lote**, una sola vez.
- `max_tokens` limitado, temperatura baja, timeout 20 s, un reintento.
- Prompt fijo al inicio para aprovechar el caché de contexto de DeepSeek.

---

## 5. Anuncios

Formato principal: **recompensado** (“mira 15 s y desbloquea un análisis más”).

- eCPM Colombia: ~$2–5 USD por mil impresiones → **7–17 COP por vista**.
- Un análisis cuesta 2–5 COP: el anuncio lo paga y sobra.
- Nunca en el flujo de registrar un gasto.
- Nunca para suscriptores.
- Banner discreto, máximo uno, solo en la pantalla de análisis para usuarios gratis.
- Consentimiento GDPR para España.
- Anuncios familiares; la app no está dirigida a niños.

Los anuncios son un puente, no el modelo. La suscripción sigue siendo el ingreso principal.

---

## 6. Gráficos

Tres, máximo, con revelación progresiva. La psicología: **comparar contra el pasado propio**, no contra una meta.

1. **La carrera del ciclo.** Dos líneas acumuladas: ciclo actual vs. anterior. Mensaje: “vas 12% por debajo”.
2. **En qué se fue.** Barras horizontales del top 5, con monto y porcentaje. Sin grillas ni 3D.
3. **Tus gustos pequeños.** Ranking con frecuencia y total: *Coca-Cola 7 veces · $116.780*.

Extra:
4. **“¿Y si…?”** Deslizador de 0 a 100% sobre un producto; muestra cuánto se liberaría. No es una meta, es un experimento.
5. **Constancia.** Puntos de los días con registro. Premia el hábito, no el monto.

Implementación: **Canvas de Compose**, sin librerías. Dos colores (turquesa actual, gris pasado), dorado para el dato clave, etiquetas directas, números grandes, una idea por gráfico.

---

## 7. Capa paga

### 7.1 Qué se cobra y qué no

| Función | Gratis | Plus | Negocio |
|---|---|---|---|
| Registrar gastos | Ilimitado | Ilimitado | Ilimitado |
| Ver ciclo actual | Sí | Sí | Sí |
| Análisis IA | 2/ciclo | Ilimitado* | Ilimitado* |
| Historial y comparación | Básico | Completo | Completo |
| Gráficos avanzados | 1 | Todos | Todos |
| Miembros | 3 | 8 | 8 |
| Exportar | Ciclo actual | Todo | Todo |
| Modo negocio | No | No | Sí |
| Anuncios | Sí | No | No |

\* Uso razonable: tope mensual por hogar (ej. 30 análisis) para evitar abuso.

### 7.2 Precios

Contexto: salario mínimo 2026 en Colombia = **$1.750.905** + auxilio = **$2.000.000**.

| Plan | Mensual | Anual | Equivalencia |
|---|---|---|---|
| Plus Hogar | **$5.900** | **$49.900** | ~0,3% del mínimo; 2 pasajes de bus |
| Negocio | **$19.900** | **$149.000** | ~1% del mínimo |

Recomendación: arrancar en **$5.900/mes** para reducir fricción y subir después con evidencia. El anual da ~30% de descuento y mejora la retención.

### 7.3 Prueba

Dos capas, sin tarjeta al inicio:

1. **Prueba de producto:** 2 análisis IA gratis por ciclo, para siempre.
2. **Prueba de suscripción:** 7 días gratis **solo cuando la persona ya chocó con el límite** y quiere más.

El paywall aparece en el momento de valor, nunca al abrir la app.

### 7.4 Facturación por hogar

- La suscripción pertenece al **hogar**, no a la persona.
- Paga cualquier administrador; todos los miembros reciben Plus.
- Un solo pago cubre a la familia.
- Si el pagador sale del hogar, el plan sigue hasta el vencimiento y luego el hogar vuelve a Gratis.

### 7.5 Flujo con Google Play Billing

- Suscripciones con base plans y oferta de prueba.
- Comisión: **15%** en Colombia (suscripciones).
- Play Console: pago único de $25.
- Cuenta personal nueva: **12 testers durante 14 días continuos** antes de producción.
- La app **no** puede enviar a pagar por fuera (política de Play para bienes digitales).

### 7.6 Verificación en el servidor

Nunca confiar en el cliente.

```
App → POST /billing/verify { productId, purchaseToken }
Worker → Google Play Developer API (service account)
Worker → upsert subscriptions + households.plan
App → GET /billing/status
```

Tablas:

```sql
ALTER TABLE households ADD COLUMN plan TEXT NOT NULL DEFAULT 'FREE';
ALTER TABLE households ADD COLUMN plan_expires_at TEXT;
ALTER TABLE households ADD COLUMN plan_source TEXT; -- PLAY | FOUNDER | MANUAL | BECA

CREATE TABLE subscriptions (
  id TEXT PRIMARY KEY,
  household_id TEXT NOT NULL,
  payer_user_id TEXT NOT NULL,
  platform TEXT NOT NULL,          -- PLAY
  product_id TEXT NOT NULL,
  purchase_token TEXT NOT NULL,
  status TEXT NOT NULL,            -- ACTIVE | GRACE | ON_HOLD | CANCELED | EXPIRED
  current_period_end TEXT,
  auto_renew INTEGER NOT NULL DEFAULT 0,
  last_verified_at TEXT NOT NULL,
  created_at TEXT NOT NULL
);

CREATE TABLE billing_events (
  id TEXT PRIMARY KEY,
  household_id TEXT,
  tipo TEXT NOT NULL,
  payload TEXT,
  created_at TEXT NOT NULL
);
```

- **RTDN** (Real-time Developer Notifications) vía Pub/Sub para renovaciones, cancelaciones y reembolsos.
- Cron diario que re-verifica las suscripciones por vencer.
- Periodo de gracia: 3 días de acceso tras un fallo de cobro.
- Reembolso o cancelación: se revoca el acceso al terminar el periodo pagado.

### 7.7 Gating

Las reglas viven en el **backend**, no en la app:

- `puedeUsarIa(hogarId)`
- `puedeVerHistorico(hogarId)`
- `puedeExportarTodo(hogarId)`
- `maxMiembros(hogarId)`
- `puedeModoNegocio(hogarId)`

La app solo muestra u oculta botones; el endpoint siempre valida.

### 7.8 Códigos, fundadores y becas

- Tabla `activation_codes` (código, plan, días, usos, expira).
- **Fundador:** pago único de $79.900 para los primeros 100, Plus de por vida con tope de IA (10 análisis/mes).
- **Beca:** códigos gratuitos para personas que no pueden pagar, entregados manualmente.
- Los códigos también sirven para el test cerrado.

### 7.9 Reembolsos, gracia y cancelación

- Cancelar debe ser fácil y visible.
- Mantener acceso hasta el final del periodo pagado.
- No borrar datos al bajar a Gratis; solo se bloquean funciones.
- Si vuelve, recupera su historial completo.

### 7.10 Impuestos y comisión

- Colombia: 15% de comisión en suscripciones.
- Google gestiona IVA/recaudo en la mayoría de mercados; el precio debe definirse con impuestos incluidos.
- La nueva estructura 10% + 5% aplica primero a EE. UU., Reino Unido y EEE; Colombia llegaría después (2027).

---

## 8. Métricas

| Métrica | Meta inicial |
|---|---|
| Activación (5 registros en 24 h) | > 60% |
| Hábito (3 días con registro en la primera semana) | > 40% |
| Retención semana 4 | > 25% |
| Conversión Gratis → Plus | 2–5% |
| Churn mensual Plus | < 8% |
| Costo de IA por usuario activo | < $0,02 USD/mes |

Para 20 suscriptores de hogar al 3% de conversión se necesitan ~670 usuarios activos. Al 5%, ~400.

---

## 9. Cómo llegar a 20 suscriptores

1. **Test cerrado:** 12 personas (requisito de Play) entre familia, amigos y conocidos.
2. **Contenido:** videos cortos tipo “en qué se me fue la plata”, con datos reales y sin burla.
3. **Comunidades:** grupos de madres, finanzas personales, emprendedores, vendedores.
4. **Referidos:** invita a otro hogar y ambos reciben un mes de Plus.
5. **Negocio:** venta directa por WhatsApp a vendedores y tiendas; un mes gratis y acompañamiento.
6. **Alianzas:** cooperativas, bancos comunales y empresas que quieran ofrecer educación financiera a sus empleados (B2B2C, fase posterior).

La vía más corta a 20 pagos no es el hogar: es **negocio + venta directa**. El hogar da volumen y misión; el negocio da ingresos.

---

## 10. Roadmap

| Fase | Qué | Estado |
|---|---|---|
| 0 | Catálogo que aprende + clasificador + 3 gráficos + presupuesto IA | Siguiente |
| 1 | Análisis con IA, cuotas y caché | Pendiente |
| 2 | Anuncio recompensado | Pendiente |
| 3 | Capa paga: códigos → Play Billing | Pendiente |
| 4 | Modo negocio | Documentado |
| 5 | Fiado, offline, B2B2C | Futuro |

---

## 11. Riesgos

| Riesgo | Mitigación |
|---|---|
| Baja conversión en hogares | Precio bajo, anual, fundadores, negocio como ingreso principal |
| Churn tras el primer mes | Hábito semanal, valor antes del cobro, no bloquear lo esencial |
| Costo de IA | Corte al 80%, caché, cuotas, clasificación en lote |
| Anuncios que degradan la experiencia | Solo recompensados, nunca en el registro |
| Política de Play | Usar Play Billing; no enlaces de pago externos |
| Expansión a otros países | Moneda y locale por hogar; catálogo con alias por país |
| Dependencia de DeepSeek | Reporte determinista siempre disponible; proveedor intercambiable |
| Soporte y contraseñas | Pregunta, llave y reset por admin ya implementados |

---

## 12. Decisiones abiertas

1. ¿Cobrar por hogar (recomendado) o por persona?
2. ¿$5.900 o $9.900 al mes?
3. ¿Prueba de 7 días, 2 análisis gratis, o ambas?
4. ¿Lanzar pase de fundador de por vida? ¿A qué precio?
5. ¿Banner discreto en análisis o solo recompensados?
6. ¿El plan Negocio se vende desde el mismo APK o como app separada?

---

## 13. Notas de revisión (nov 2026)

> Ideas nuevas registradas para analizar. **No reemplazan** lo anterior todavía.

### 13.1 Precio hogar + IA
- En consideración: **$3.000 COP/mes** (opción más accesible, por debajo de los $14.900–22.900 de Wallet/Monefy premium).
- Viable en costos: un análisis IA cuesta 2–5 COP; 30 análisis/mes ≈ 60–150 COP.
- Neto con 15% de Play: ~$2.550/mes por usuario.
- Pregunta de anclaje: precio muy bajo puede leerse como “bajo valor”. Es una elección de posicionamiento (el más accesible), coherente con la misión.

### 13.2 Negocio como sistema aparte
- Decisión en curso: el modo negocio sería un **sistema aparte** (Worker, D1 y app propios).
- Aclaración técnica: el beneficio de la capa gratis de Cloudflare es **por cuenta**, no por Worker. Un Worker/D1 extra bajo la **misma** cuenta comparte la cuota (100k peticiones/día, D1 5M lecturas/día). Para duplicar la capa gratis se necesita una **cuenta de Cloudflare separada** (otro correo).
- Trade-off: dos cuentas = dos facturaciones, dos logins, dos apps. A cambio, aislamiento total de datos y de cuotas, e iteración independiente.
- Inventario, empleados y fiado se evalúan **después**, separados del hogar.

### 13.3 Empleado deshonesto
- Ningún software obliga a registrar lo que se quiere ocultar: el dato es auto-reportado. Cauce no es un sistema antifraude.
- Mitigaciones de diseño:
  1. **Fricción cero** (voz + 1 toque): registrar en 5 s para que “olvidar” no sea excusa.
  2. **Cuadre de caja**: el dueño cuenta el efectivo al cierre; la app compara “esperado = fondo + ventas − gastos” con lo real y muestra la diferencia. Es lo único que detecta una venta no registrada.
  3. **Auditoría y roles**: el empleado agrega pero no edita ni borra; cada registro lleva quién y cuándo; borrar exige contraseña de admin (patrón ya existente).
  4. **Foto de ticket** opcional para gastos (reduce gastos inventados).
- El cuadre de caja es la respuesta real a la preocupación y un argumento para que negocio sea un producto aparte y cuidado.

### 13.4 Competencia (hallazgos)
- **Hogar/personal:** Mis Gastos (Freeman, 4,9★, 327k reseñas), Wallet (BudgetBakers), Monefy, Spendee, MonAi (voz + IA).
- **Parejas/familia:** Honeydue (inglés).
- **Presupuestos/suscripciones:** YNAB (~$15 USD/mes), Rocket Money, Fintonic (ES).
- **Negocio LatAm:** **Treinta** (~8M usuarios), **Control de Mi Negocio** (Lehreer: ventas, fiado, cuadre, sin facturación electrónica), Alegra.
- Conclusión: registrar gastos está saturado; el negocio LatAm está **disputado** (Treinta). El ángulo más defendible es **hogar + coaching amable + voz en español + ciclo de quincena**, que nadie resuelve bien. No hay que ser el único; hay que ser el que una mamá o un vendedor colombiano sí usa.
