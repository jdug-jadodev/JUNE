# Modo Negocio — especificación futura

> **Estado:** idea aprobada, **no implementada**.
> Este documento queda aparte del modo hogar. Se retoma cuando el hogar esté validado con usuarios reales.

---

## 1. Principio

No es un POS, no es un inventario, no es contabilidad.

Es una **libreta que entiende**: registrar en dos toques lo que entró y lo que salió, y ver qué quedó. Para vendedores ambulantes, tiendas de barrio, emprendimientos de una persona y negocios muy pequeños que hoy anotan en un cuaderno o en WhatsApp.

La promesa: **"lleva las cuentas de tu negocio sin comprar un sistema de inventarios"**.

---

## 2. Diferencia con el modo hogar

| | Hogar | Negocio |
|---|---|---|
| Registra | Solo salidas (gastos) | Entradas (ventas) y salidas (gastos) |
| Pide ingresos | **No** | **Sí**, porque es el punto del negocio |
| Análisis | Patrones, gustos pequeños, por qué | Margen, productos, días flojos |
| Metas | Ninguna | Ninguna |
| Inventario | No aplica | **No** (decisión de producto) |

El modo hogar no cambia. El modo negocio es un espacio distinto.

---

## 3. Flujo de registro

Dos botones grandes:

### Venta
- Monto: **obligatorio**.
- Qué se vendió: opcional (`empanada`, `gaseosa`, `recarga`).
- Cantidad: opcional (`3`).
- Si no escribe qué, igual cuenta para el total del día.

### Gasto
- Monto: **obligatorio**.
- Qué se pagó: opcional (`mercancía`, `arriendo`, `luz`).

### Voz (fase posterior)
- "Vendí 3 empanadas a 2 mil" → monto + cantidad + producto.
- "Pagué 40 mil de mercancía" → gasto.

### Lo que NO se pide
- Cliente, cédula, factura, IVA, retención.
- Stock, proveedor, costo unitario.
- Cierre de caja obligatorio.

---

## 4. Análisis del negocio

- **"Te quedó"** = ventas − gastos del período. No llamarlo "utilidad neta" (no somos contadores).
- **Top productos** por unidades y por ingreso, calculado de las ventas registradas. Sin inventario.
- **Gastos que se comen el margen**: cuánto pesa mercancía, arriendo, servicios.
- **Mejores y peores días** de la semana.
- **Comparación** contra el período anterior.
- **Una sola recomendación** accionable.

Ejemplos:
> "Vendiste $1.240.000 y gastaste $860.000: te quedaron $380.000."
> "La empanada es el 42% de tus ventas. La gaseosa subió 18% este mes."
> "Los martes vendes la mitad que los sábados."

---

## 5. Modelo de datos propuesto

```sql
-- Espacios: un usuario puede tener casa y negocio
ALTER TABLE households ADD COLUMN modo TEXT NOT NULL DEFAULT 'HOGAR';
-- 'HOGAR' | 'NEGOCIO'

ALTER TABLE households ADD COLUMN moneda TEXT NOT NULL DEFAULT 'COP';

-- Movimientos (reutiliza la tabla expenses para no duplicar lógica)
ALTER TABLE expenses ADD COLUMN direccion TEXT NOT NULL DEFAULT 'SALIDA';
-- 'SALIDA' (gasto) | 'ENTRADA' (venta)

ALTER TABLE expenses ADD COLUMN cantidad REAL;
ALTER TABLE expenses ADD COLUMN concepto_id TEXT;
ALTER TABLE expenses ADD COLUMN categoria TEXT;
```

Fiado (fase posterior):

```sql
CREATE TABLE fiados (
  id TEXT PRIMARY KEY,
  household_id TEXT NOT NULL,
  nombre TEXT NOT NULL,
  monto_cents INTEGER NOT NULL,
  fecha TEXT NOT NULL,
  pagado INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL
);
```

Notas:
- `expenses` se mantiene como tabla única de movimientos; `direccion` distingue venta de gasto.
- El catálogo de conceptos es el mismo del modo hogar (aprende alias).
- No se crea tabla de inventario.

---

## 6. Espacios y permisos

- Un usuario puede pertenecer a **varios hogares/negocios** (la tabla `members` ya lo permite).
- La app necesita un **selector de espacio**: "Casa" / "Negocio".
- Admin del negocio: todo.
- Miembro: registrar ventas y gastos.
- **Empleado** (fase posterior): solo registrar ventas, sin ver margen ni totales.
- El reporte de negocio **nunca** se mezcla con el de casa.

---

## 7. Pantallas

1. **Selector de espacio** al entrar.
2. **Inicio negocio**: "Hoy: vendiste $X, gastaste $Y, te quedó $Z" + botones Venta/Gasto + últimos movimientos.
3. **Análisis**: margen del período, top productos, gastos, días.
4. **Cierre de día**: resumen corto (opcional, no obligatorio).

---

## 8. Precio

- **Cauce Negocio:** $19.900 COP/mes o $149.000 COP/año.
- Sin anuncios.
- Incluye análisis con IA dentro del presupuesto mensual del Worker.

El negocio es el segmento con mayor disposición a pagar: si la app muestra que se pierden $300.000 al mes, la suscripción se paga sola.

---

## 9. Riesgos y decisiones pendientes

| Riesgo | Mitigación |
|---|---|
| **Sin internet** en venta ambulante | Cola local + sincronización. Es casi obligatorio para este segmento. |
| Expectativa de inventario o impuestos | Mensaje claro: "no es un sistema de inventario ni de facturación". |
| Soporte y capacitación | Onboarding de 3 pasos, textos grandes, lenguaje simple. |
| Datos de clientes (fiado) | Consentimiento y política de privacidad. |
| Mezclar casa y negocio | Espacios separados desde el modelo de datos. |
| Complejidad del registro | Máximo 2 campos; todo lo demás opcional. |

---

## 10. Fases

1. Modelo (`modo`, `direccion`, `cantidad`) + selector de espacios + ventas/gastos.
2. Análisis de margen, top productos y días.
3. Fiado.
4. Offline + sincronización.
5. Empleados y permisos finos.

---

## 11. Lo que este modo NO es

- No es facturación electrónica.
- No es inventario ni control de stock.
- No es nómina.
- No es conciliación bancaria.
- No es crédito ni préstamo.
- No reemplaza al contador.

---

## 12. Preguntas abiertas

- ¿El negocio necesita cierre de caja diario o solo el acumulado?
- ¿El "fiado" debe afectar el margen del día o solo registrarse aparte?
- ¿Se permite un negocio con varios puntos de venta?
- ¿El empleado puede ver montos o solo registrar?
- ¿Se cobra por negocio o por usuario?
