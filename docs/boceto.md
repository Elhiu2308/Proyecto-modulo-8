# Boceto de la app (wireframes estilo Balsamiq)

Estos bocetos son el plano de las pantallas antes de programarlas. Se pueden
recrear en [Balsamiq Wireframes](https://balsamiq.com/) arrastrando los mismos
controles que aparecen aquí (botones, cajas de texto, imágenes y etiquetas).

## Flujo general

```
Login  ──►  Registro
  │
  └──►  Principal ──► (Tomar foto)  ──►  Cámara del sistema
            │                                   │
            │  ◄─── guarda archivo + GPS + comentario (SQLite)
            │
            └──►  Ver fotos ──►  Detalle de la foto (comentario + GPS)
```

---

## Pantalla 1 — Registro de usuario

```
+---------------------------------------------------+
|                      [ LOGO ]                     |
|                                                   |
|  Crear cuenta                                     |
|  Registra tus datos para usar FotoApp             |
|                                                   |
|  +---------------------------------------------+  |
|  | Nombre(s)                               [ ] |  |
|  +---------------------------------------------+  |
|  | Apellidos                               [ ] |  |
|  +---------------------------------------------+  |
|  | Correo electrónico                      [ ] |  |
|  +---------------------------------------------+  |
|  | Contraseña                              [*] |  |
|  +---------------------------------------------+  |
|  | Confirmar contraseña                    [*] |  |
|  +---------------------------------------------+  |
|                                                   |
|  +---------------------------------------------+  |
|  |                 REGISTRAR                   |  |
|  +---------------------------------------------+  |
|                                                   |
|      ¿Ya tienes cuenta? Inicia sesión             |
+---------------------------------------------------+
```

## Pantalla 2 — Inicio de sesión

```
+---------------------------------------------------+
|                     [ LOGO ]                      |
|                                                   |
|                    FotoApp                        |
|            Inicia sesión para continuar           |
|                                                   |
|  +---------------------------------------------+  |
|  | Correo electrónico                      [ ] |  |
|  +---------------------------------------------+  |
|  | Contraseña                              [*] |  |
|  +---------------------------------------------+  |
|                                                   |
|  +---------------------------------------------+  |
|  |               INICIAR SESIÓN                |  |
|  +---------------------------------------------+  |
|                                                   |
|        ¿No tienes cuenta? Regístrate              |
+---------------------------------------------------+
```

## Pantalla 3 — Principal

```
+---------------------------------------------------+
|  Hola, Juan                                       |
|  Toma una foto y guarda su comentario y GPS       |
|                                                   |
|  +-------------------+  +-----------------------+ |
|  |    TOMAR FOTO     |  |      VER FOTOS        | |
|  +-------------------+  +-----------------------+ |
|                                                   |
|  +---------------------------------------------+  |
|  |                                             |  |
|  |         VISTA PREVIA DE LA FOTO             |  |
|  |                                             |  |
|  +---------------------------------------------+  |
|                                                   |
|  Foto lista para guardar                          |
|                                                   |
|  +---------------------------------------------+  |
|  | Comentario de la foto                       |  |
|  |                                             |  |
|  +---------------------------------------------+  |
|                                                   |
|  +---------------------------------------------+  |
|  |                 GUARDAR FOTO                |  |
|  +---------------------------------------------+  |
|                                                   |
|                  Cerrar sesión                    |
+---------------------------------------------------+
```

## Pantalla 4 — Ver fotos

```
+---------------------------------------------------+
|  Mis fotos                                        |
|                                                   |
|  +---------------------------------------------+  |
|  | [ IMG ] 19/10/2026 14:35                    |  |
|  |         Comentario de la foto...            |  |
|  |         Lat: 19.432608, Lon: -99.133209     |  |
|  +---------------------------------------------+  |
|  +---------------------------------------------+  |
|  | [ IMG ] 18/10/2026 09:12                    |  |
|  |         Otra foto con su comentario         |  |
|  |         Ubicación GPS no disponible         |  |
|  +---------------------------------------------+  |
|                                                   |
|  (si no hay fotos: "Todavía no has guardado fotos")
+---------------------------------------------------+
```

## Pantalla 5 — Detalle de la foto

```
+---------------------------------------------------+
|  +---------------------------------------------+  |
|  |                                             |  |
|  |              FOTO A TAMAÑO GRANDE           |  |
|  |                                             |  |
|  +---------------------------------------------+  |
|                                                   |
|  Tomada el 19/10/2026 14:35                       |
|                                                   |
|  Comentario                                       |
|  Comentario de la foto...                         |
|                                                   |
|  Ubicación GPS                                    |
|  Lat: 19.432608, Lon: -99.133209                  |
|                                                   |
|  +---------------------------------------------+  |
|  |               VER EN EL MAPA                |  |
|  +---------------------------------------------+  |
+---------------------------------------------------+
```

---

## Base de datos del boceto

```
USUARIOS                        FOTOS
+----------------------+        +----------------------+
| id (PK)              |        | id (PK)              |
| nombre               |        | usuario_id (FK) ----+
| apellidos            |        | ruta_archivo         |
| correo (único)       |        | comentario           |
| contrasena (SHA-256) |        | latitud              |
+----------------------+        | longitud             |
                                | fecha                |
                                +----------------------+
```
