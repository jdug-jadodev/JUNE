# Capa Premium — Cauce

> Documento de diseño de la capa paga y del motor de IA.
> Complementa `docs/ESCALAR-PROYECTO.md` (plan general) y `docs/MODO-NEGOCIO.md` (negocio, diferido).
> Última actualización: noviembre 2026.

---

## 1. Objetivo

La capa Premium convierte a Cauce de "registro + reporte determinista" a **educación financiera con IA**, sin romper la promesa de registrar en segundos ni caer en tono de banco.

La IA **no es el producto**: es una capa de explicación sobre un reporte que ya funciona sin ella. Si la IA falla o se agota el presupuesto, la app sigue siendo útil con el motor determinista.

---

## 2. Principios que se mantienen

1. **Registrar en segundos.** Monto + nota o voz. Nunca un formulario largo.
2. **Categorías invisibles.** Se deducen; el usuario no las elige al registrar.
3. **Sin culpa.** Se compara contra el pasado propio, no contra una meta impuesta.
4. **Nada esencial detrás del muro.** Registrar y ver lo básico es gratis para siempre.
5. **Un solo pago por hogar.** La suscripción cubre a toda la familia.
6. **Presupuesto de IA con corte automático.** Nunca una factura sorpresa.
7. **Honestidad.** La IA puede equivocarse y siempre se puede corregir.

---

## 3. Qué entra en cada capa

| Funcionalidad | Gratis | Premium (Plus Hogar) |
|---|---|---|
| Registrar gastos (texto/voz) | Ilimitado | Ilimitado |
| Análisis determinista (el actual) | Sí | Sí (respaldo) |
| Motor de organización por IA | No | **Sí** |
| Análisis narrativo con IA | 2/ciclo | Ilimitado* |
| Comparar contra ingresos (mensual/quincenal) | No | **Sí** |
| Historial y comparación entre ciclos | Básico | Completo |
| Miembros | 3 | 8 |
| Exportar CSV | Ciclo actual | Todo |

\* Uso razonable: tope mensual por hogar (ej. 30 análisis) para evitar abuso.

---

## 4. Motor de organización por IA

Hoy el clasificador es **determinista**: catálogo de ~150 conceptos + alias + correcciones por hogar. En Premium lo subimos a un **híbrido**.

### 4.1 Cómo funciona

- **IA es la capa principal** para clasificar. Entiende contexto y frases completas. Ejemplos:
  - `vps contado Jonathan` → **Negocio e inversión**
  - `mac pizza` → **Gusticos**
  - `cita médica neyde` → **Salud**
  - `mesada Eduardo` → **Ayuda a familiares**
- **Determinista queda como respaldo**: si la IA falla, se agota el presupuesto o hay timeout, se clasifica con el catálogo actual. Nadie se queda sin categorías.
- **Se clasifica una sola vez por término nuevo y se guarda en caché** para siempre (por hogar). No se vuelve a pagar por el mismo producto.
- Cada clasificación trae **nivel de confianza** y, opcionalmente, un "por qué" ("lo puse en Salud porque parece una cita médica").
- **El usuario siempre puede corregir** y esa corrección pesa más que la IA la próxima vez.

### 4.2 Aviso honesto (obligatorio)

Línea pequeña cerca de las categorías:

> "Estas categorías las asigna IA según tus hábitos. Puede equivocarse: revísalas y corrígelas cuando quieras."

---

## 5. Análisis narrativo con IA

Entrada: gastos del ciclo, totales por categoría, ritmo y ciclo anterior.

Salida:

- Un resumen en lenguaje natural ("Este ciclo el grueso fue vivienda y deudas; en gusticos gastaste X").
- **Una sola recomendación**, comparando **solo contra el historial propio**. Nunca "deberías" ni "estás gastando demasiado".
- Explicación del porqué de una clasificación dudosa.

Sin proyecciones, sin metas, sin culpa.

---

## 6. Comparación con ingresos (novedad)

Antes decidimos no pedir ingresos para no parecer un banco. En Premium lo reconciliamos así:

- Los ingresos son **opcionales y solo en Premium**. Se configuran una vez, no se preguntan al registrar.
- Dos modos: **mensual** o **quincenal**, con su día de corte.
- El análisis muestra:
  - "De tu ingreso quincenal, se fue **34%** en este ciclo."
  - "Te queda **X** disponible."
  - "Te alcanzó para **N** días."
- **Sin juicio**: nunca "te pasaste del presupuesto". Solo "de lo que entra, esto fue a gastos".

Esto respeta la misión (educar, no vigilar) y le da un ancla real a la comparación.

### 6.1 Perfil del hogar sin preguntar (implementado)

Pedir "cuántos niños, cuántos integrantes, cuánto cuesta vivir" es intrusivo. La app lo **deduce** de los datos que ya tiene:

| Señal | De dónde sale |
|---|---|
| Personas del hogar | integrantes registrados + mercado mensual estimado por persona |
| Niños / adolescentes | gastos de colegio, útiles, universidad, mesada, pañales, jardín |
| Mascotas | categoría de mascotas con gasto |
| Costo de vida | % del ingreso que se va en gastos fijos |
| Capacidad discrecional | % del ingreso disponible para gustos, ocio, restaurantes y ropa |

Los **montos sugeridos los calcula el backend**, no la IA: parte de un valor por persona (ej. ropa $90.000/mes por persona) y lo recorta si la carga fija es alta. La IA solo redacta el estado, las notas y la recomendación. Así nunca vuelve a sugerir "$40.000 de ropa" para una familia de seis.

La persona puede **ajustar el número de integrantes** desde la pantalla de análisis. Ese valor:

- se guarda **solo en el teléfono** (DataStore local, por hogar), nunca en D1;
- se envía únicamente en la petición del análisis para que la IA lo use en ese momento;
- se puede volver a la deducción automática cuando quiera.

Si no hay ajuste, el backend deduce el número con los integrantes registrados y el gasto en mercado.

---

## 7. Presupuesto de IA (economía de tokens)

Con DeepSeek V4.1 Flash (~$0.15/M entrada, $0.60/M salida off-peak), un análisis sale **~2–5 COP**.

### 7.1 Guardrails obligatorios

| Técnica | Efecto |
|---|---|
| Prompt de sistema fijo | aprovecha el **caché de contexto** de DeepSeek |
| Clasificación **en lote** (varios términos nuevos por llamada) | 1 pago por lote, no por producto |
| Caché por **huella de datos** (`count` + `max(updated_at)`) | no se paga dos veces lo mismo |
| `max_tokens` bajo, temperatura baja, timeout 20 s, 1 reintento | techo en salida |
| **Corte automático al 80%** ($4 de $5/mes) | nunca factura sorpresa |
| Cuota Gratis: 2 análisis/ciclo | control de abuso |

### 7.2 Tope

- Tope mensual: **$5 USD** (≈ 17.000 COP).
- Alcanza para **miles de análisis**.
- Al llegar al 80%, la app cae al reporte determinista y avisa cuándo se reanuda.

---

## 8. Otras funcionalidades propuestas (para priorizar)

1. **Ciclo quincenal** alineado a la quincena (ángulo diferencial en Colombia).
2. **"¿Y si…?"**: deslizar un gusto y ver cuánto se liberaría (experimento, no meta).
3. **Constancia**: racha de días registrando (premia el hábito, no el monto).
4. **Gastos fijos detectados**: la IA marca arriendo, suscripciones, colegio como "fijos" y avisa si suben.
5. **Aviso de olvido**: "llevás 3 días sin registrar" (recordatorio suave).
6. **Comparación mes a mes / ciclo a ciclo** más rica (histórico en Premium).
7. **Foto de ticket** opcional (útil también para el futuro modo negocio).
8. **Referidos**: invita a otro hogar y ambos ganan un mes.

---

## 9. Precios y lanzamiento

- Plus Hogar: **$5.900/mes** (o **$3.000** como opción ultra-accesible) · anual **$49.900**.
- Play Billing (15% en Colombia), **12 testers / 14 días**, códigos de fundador, becas.
- **No construir cobros hasta validar demanda con códigos.**

---

## 10. Orden de implementación recomendado

1. **Motor de IA híbrido** (clasificación en lote + caché + corte al 80%).
2. **Análisis narrativo** con ingresos opcionales.
3. **Cobro** (códigos → Play Billing) solo cuando haya demanda real.

---

## 11. Decisiones abiertas

1. Comparación de ingresos: ¿**mensual, quincenal o ambas**?
2. Precio: ¿**$5.900** o **$3.000**/mes?
3. ¿Prueba de 7 días, 2 análisis gratis, o ambas?
4. ¿Tope de IA: 30 análisis/mes por hogar?
5. ¿Motor híbrido desde el día 1, o IA solo para "sin clasificar" y luego expandir?
