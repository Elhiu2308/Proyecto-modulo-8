# FotoApp — App Android de Fotos

Aplicación Android nativa (Kotlin) que registra usuarios en una base de datos
local, permite tomar fotos con la cámara del dispositivo, guardar cada foto en
la carpeta privada de la app junto con un comentario y la ubicación GPS, y
consultar después la lista de fotos con su comentario y coordenadas.

## Funcionalidad (mapeada al enunciado)

| # | Requisito | Dónde está implementado |
|---|-----------|-------------------------|
| 1 | Registro de usuario (nombre, apellidos, correo, contraseña) en BD local | `RegistroActivity.kt` + tabla `usuarios` (Room/SQLite) |
| 2 | Inicio de sesión comparando usuario y contraseña en la BD local | `LoginActivity.kt` + `UsuarioDao.iniciarSesion()` |
| 3 | Pantalla principal con las opciones **Tomar foto** y **Ver fotos** | `PrincipalActivity.kt` |
| 4 | Tomar foto → se muestra en la pantalla principal, se guarda en la carpeta de la app y en la BD se guarda GPS + comentario | `PrincipalActivity.kt`, `Utilidades.carpetaFotos()`, tabla `fotos` |
| 5 | Ver fotos → lista desde la BD; al seleccionar una se muestra el comentario y la ubicación GPS | `VerFotosActivity.kt` y `DetalleFotoActivity.kt` |

El boceto de las pantallas (estilo Balsamiq) está en [`docs/boceto.md`](docs/boceto.md).

## Pantallas

1. **Registro**: nombre(s), apellidos, correo y contraseña (con confirmación).
2. **Inicio de sesión**: valida correo + contraseña contra la base de datos local.
3. **Principal**: botones *Tomar foto* y *Ver fotos*, vista previa de la foto
   recién tomada, campo de comentario y botón *Guardar foto*.
4. **Ver fotos**: lista (RecyclerView) de las fotos del usuario, la más reciente primero.
5. **Detalle de la foto**: imagen, comentario, ubicación GPS y botón para abrir
   las coordenadas en una app de mapas.

## Cómo funciona el guardado de la foto

1. Se presiona **Tomar foto**: la app crea el archivo
   `files/fotos/foto_<marca de tiempo>.jpg` (carpeta privada de la app,
   `/data/data/com.ejemplo.fotosapp/files/fotos/`) y entrega su URI a la cámara
   del sistema mediante un `FileProvider`.
2. Al volver de la cámara, la foto se muestra en la pantalla principal.
3. El usuario escribe un **comentario** y presiona **Guardar foto**.
4. La app obtiene la última ubicación conocida (GPS/red) y guarda en la tabla
   `fotos`: usuario, ruta del archivo, comentario, latitud, longitud y fecha.

No se requieren permisos de almacenamiento porque las fotos viven en la carpeta
privada de la aplicación. Sólo se pide permiso de **ubicación** (y es opcional:
si se niega, la foto se guarda sin coordenadas).

## Base de datos local (Room / SQLite)

Nombre del archivo: `fotosapp.db`

```text
usuarios
  id            INTEGER PK autoincremental
  nombre        TEXT
  apellidos     TEXT
  correo        TEXT  (único)
  contrasena    TEXT  (hash SHA-256, nunca en texto plano)

fotos
  id            INTEGER PK autoincremental
  usuarioId     INTEGER  FK -> usuarios.id  (ON DELETE CASCADE)
  ruta          TEXT     (ruta del JPG en files/fotos/)
  comentario    TEXT
  latitud       REAL     (puede ser NULL)
  longitud      REAL     (puede ser NULL)
  fecha         INTEGER  (marca de tiempo en milisegundos)
```

## Estructura del proyecto

```text
FotoAppAndroid/
├── app/src/main/
│   ├── java/com/ejemplo/fotosapp/
│   │   ├── LoginActivity.kt          inicio de sesión
│   │   ├── RegistroActivity.kt       registro de usuario
│   │   ├── PrincipalActivity.kt      tomar foto / guardar (GPS + comentario)
│   │   ├── VerFotosActivity.kt       lista de fotos desde la BD
│   │   ├── DetalleFotoActivity.kt    comentario + ubicación GPS
│   │   ├── FotoAdapter.kt            adaptador del RecyclerView
│   │   ├── db/                       entidades, DAOs y base de datos Room
│   │   └── util/                     utilidades y manejo de sesión
│   ├── res/layout/                   pantallas XML
│   ├── res/xml/file_paths.xml        rutas del FileProvider
│   └── AndroidManifest.xml
├── docs/boceto.md                    boceto de las pantallas (Balsamiq)
├── gradlew / gradlew.bat             Gradle Wrapper 9.8.2
└── build.gradle / settings.gradle
```

## Requisitos para compilar

- Android Studio 2026.2 o superior (trae el JDK 25 y el soporte de Kotlin incluido en AGP 9).
- Android SDK **37** (`compileSdk 37`, `minSdk 24`).
- Gradle 9.8.2 (incluido en el *wrapper* del proyecto).
- Conexión a Internet la primera vez para descargar dependencias.

### Versiones usadas

| Componente | Versión |
|------------|---------|
| Android Gradle Plugin | 9.4.1 (Kotlin incluido / *built-in Kotlin*) |
| Gradle | 9.8.2 |
| KSP | 2.3.12 |
| Room | 2.8.5 |
| Material Components | 1.14.0 |
| Play Services Location | 21.4.0 |


## Cómo compilar y ejecutar

Con Android Studio:

1. `File ▸ Open` y selecciona la carpeta `FotoAppAndroid`.
2. Espera a que termine la sincronización de Gradle.
3. Conecta un dispositivo con cámara (o crea un emulador con cámara y GPS) y
   presiona **Run ▸ Run 'app'**.

Con la línea de comandos:

```sh
cd FotoAppAndroid
./gradlew assembleDebug          # genera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug           # instala en el dispositivo conectado
```

> En el emulador hay que asignar coordenadas en *Extended controls ▸ Location*
> para que la foto quede con ubicación GPS.

## Flujo de uso

1. Abrir la app → **Inicio de sesión** → *¿No tienes cuenta? Regístrate*.
2. Llenar los datos → **Registrar**.
3. Iniciar sesión con el correo y la contraseña registrados.
4. En la pantalla principal: **Tomar foto** → la cámara se abre → aceptar la foto.
5. Escribir el comentario → **Guardar foto** (concede el permiso de ubicación).
6. **Ver fotos** → tocar una foto → se muestra el comentario y la ubicación GPS.

## Publicar el repositorio en GitHub

El repositorio debe ser **público** para que el instructor lo pueda revisar:

```sh
cd FotoAppAndroid
git init
git add .
git commit -m "FotoApp: app Android de fotos con registro, login, cámara, GPS y SQLite"

# crea el repositorio (requiere tener GitHub CLI autenticado: gh auth login)
gh repo create FotoAppAndroid --public --source=. --remote=origin --push

# o, si prefieres hacerlo a mano:
# 1) crea el repositorio vacío en https://github.com/new (público, sin README)
git remote add origin https://github.com/<TU_USUARIO>/FotoAppAndroid.git
git branch -M main
git push -u origin main
```

## Autor

Proyecto escolar — App Android de fotos (registro local, cámara, GPS y comentarios).
