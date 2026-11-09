# Marca Cauce

Guía de identidad visual de **Cauce · Finanzas de familia**. Estos son los colores, tipografías y formas oficiales de la marca. Cualquier pantalla nueva, pieza gráfica o material debe salir de aquí.

La fuente de verdad en código es `app/src/main/java/com/andromeda/gastos/infrastructure/ui/theme/Theme.kt` (tema) y `infrastructure/ui/components/Superficies.kt` (degradado y superficies).

---

## 1. Colores principales

| Nombre | Hex | Uso |
|---|---|---|
| **Teal Cauce** | `#0F766E` | Color primario: botones principales, montos, enlaces, íconos activos (modo claro). |
| **Teal Claro** | `#2ED3C0` | Primario en modo oscuro y brillo del logo. |
| **Teal Logo** | `#12A594` | Trazo de la "C" del logo. |
| **Azul Marino** | `#0B1F3A` | Fondo del ícono de la app, final del degradado de marca, texto sobre dorado. |
| **Dorado** | `#E9B949` | Acento: la moneda del logo, anillos y barras de progreso, el consejo clave de la IA. |
| **Dorado Claro** | `#F6D67C` | Aro interno de la moneda del logo. |
| **Crema** | `#FBF9F5` | Fondo general en modo claro. |

### Degradado de marca

```
linear-gradient(135deg, #0F766E → #0B1F3A)
```

Se usa en las tarjetas protagonistas (total del ciclo, resumen de Análisis, tarjeta "Tu ciclo" de la IA) y en el botón flotante **IA**. El texto encima va en blanco (`#FFFFFF`), y los textos secundarios en blanco al 75 % de opacidad. Los acentos encima del degradado van en Dorado.

En Compose: `DegradadoCauce`.

---

## 2. Paleta del tema — modo claro

| Rol (Material 3) | Hex | Para qué |
|---|---|---|
| primary | `#0F766E` | Acciones principales |
| onPrimary | `#FFFFFF` | Texto sobre primary |
| primaryContainer | `#D7F0EC` | Fondos de íconos, avatares, botón de voz |
| onPrimaryContainer | `#063C38` | Texto/íconos sobre primaryContainer |
| secondary | `#E9B949` | Acento dorado |
| onSecondary | `#0B1F3A` | Texto sobre dorado |
| secondaryContainer | `#E4F2EF` | Chips seleccionados, botones tonales |
| onSecondaryContainer | `#063C38` | Texto sobre secondaryContainer |
| tertiary | `#0B1F3A` | Azul marino |
| tertiaryContainer | `#F8EDCD` | Fondos dorados suaves |
| background / surface | `#FBF9F5` | Fondo de pantallas y barras superiores |
| onBackground / onSurface | `#16202A` | Texto principal |
| surfaceVariant | `#EDF1EF` | Pistas de barras de progreso |
| onSurfaceVariant | `#5A6764` | Texto secundario, etiquetas |
| surfaceContainerLow | `#FFFFFF` | Tarjetas |
| surfaceContainer | `#F4F3EF` | Filas dentro de tarjetas |
| outline | `#8A9693` | Bordes de campos |
| outlineVariant | `#DDE3E0` | Borde fino de tarjetas y divisores |

## 3. Paleta del tema — modo oscuro

| Rol (Material 3) | Hex |
|---|---|
| primary | `#2ED3C0` |
| onPrimary | `#00201C` |
| primaryContainer | `#0B4F49` |
| onPrimaryContainer | `#C9F5EF` |
| secondary | `#E9B949` |
| onSecondary | `#0B1F3A` |
| secondaryContainer | `#173A37` |
| onSecondaryContainer | `#C9F5EF` |
| tertiary | `#B9C8E0` |
| tertiaryContainer | `#4A3B12` |
| background / surface | `#0E1116` |
| onBackground / onSurface | `#E4E8E7` |
| surfaceVariant | `#242A31` |
| onSurfaceVariant | `#AEB9B7` |
| surfaceContainerLow (tarjetas) | `#171B22` |
| surfaceContainer | `#1A1F26` |
| outline | `#7E8A88` |
| outlineVariant | `#323A43` |

---

## 4. Tipografía

| Uso | Fuente | Peso |
|---|---|---|
| Logotipo "cauce" | **Manrope** | Bold (700), interletrado −1.5 sp en grande / −0.5 sp en barra |
| Títulos, encabezados, montos grandes | **Manrope** | Bold (700) |
| Subtítulos y títulos pequeños (`titleMedium`, `titleSmall`) | **Manrope** | SemiBold (600) |
| Texto de lectura, etiquetas, botones | Roboto (sistema) | Regular / Medium |
| Lema "FINANZAS DE FAMILIA" | Roboto, mayúsculas | Medium, interletrado 1.6 sp, en Teal |

Etiquetas de grupo (ej. "HOY", "NOVIEMBRE 2026") van en mayúsculas, `labelMedium`, color `onSurfaceVariant`.

Archivo de la fuente: `app/src/main/res/font/manrope.ttf` (variable) y `branding/fonts/`.

---

## 5. Formas y espaciado

| Elemento | Radio |
|---|---|
| Campos pequeños | 8 dp |
| Íconos en recuadro, avatares cuadrados | 12 dp |
| Filas internas | 16 dp |
| Tarjetas (`TarjetaSuave`) | 20 dp |
| Tarjetas protagonistas con degradado, diálogos, buscador | 28 dp |
| Botones | Píldora (totalmente redondeados) |

- Margen lateral de pantalla: **16 dp**.
- Separación entre tarjetas: **8–14 dp**.
- Relleno interno de tarjetas: **16–18 dp**; tarjetas protagonistas 20–22 dp.
- Botones de acción principal: alto **48–50 dp**, ancho completo.

### Tarjetas

Las tarjetas son claras con **borde fino de 1 dp** en `outlineVariant`, sin sombra. Nada de grises ni lilas por defecto de Material.

---

## 6. Logo

- **Símbolo:** una "C" (el cauce de un río) en Teal Logo `#12A594`, con un brillo concéntrico en Teal Claro al 60 % sobre el borde superior izquierdo, y una **moneda dorada** (`#E9B949` con aro `#F6D67C`) como remate al final del trazo inferior, separada de la C.
- **Ícono de la app:** el símbolo sobre fondo Azul Marino `#0B1F3A`.
- **Logotipo:** símbolo + palabra "cauce" en minúsculas, Manrope Bold.
- **Archivos:** `branding/cauce-icon.svg`, `app/src/main/res/drawable/ic_cauce_symbol.xml`, `ic_launcher_foreground.xml`.
- Geometría (lienzo 1024): centro (512, 512), radio 210, grosor de trazo 104, la C va de −40° a 84°, moneda de radio 66 centrada en el ángulo 44°.

---

## 7. Reglas de uso

1. **Teal** para lo que se puede tocar y para los montos. **Dorado** solo como acento (progreso, logros, consejo clave), nunca para botones principales.
2. El **degradado de marca** se reserva para una sola tarjeta protagonista por pantalla, más el botón flotante IA.
3. Errores en el rojo de Material (`error`); no usar rojo para nada más.
4. Fondo crema en claro y casi negro `#0E1116` en oscuro; nunca blanco puro de fondo de pantalla.
5. Íconos en estilo **Outlined** de Material Symbols.
