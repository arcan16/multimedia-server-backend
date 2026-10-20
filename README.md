# MultimediaServer — Backend

API REST para un servidor de streaming de películas y series autoalojado. Gestiona usuarios con roles, un catálogo de películas y series con géneros y pósters, y los archivos de vídeo, que se reproducen directamente en el navegador por streaming HTTP (Range).

**Stack:** Spring Boot 4.1 · Java 17 · Maven · MySQL · Flyway · Spring Security + JWT (JJWT 0.13) · FFmpeg (`ffprobe`)

## Requisitos

- **Java 17** o superior. No hace falta instalar Maven, porque el proyecto incluye el wrapper `mvnw`.
- **MySQL 8**. La base de datos debe existir; las tablas las crea Flyway al arrancar.
- **FFmpeg**, en concreto `ffprobe`, para calcular la duración de los vídeos subidos. En Windows se instala con `winget install Gyan.FFmpeg`.

## Configuración

`application.properties` no contiene secretos: todo se configura con variables de entorno.

| Variable | Obligatoria | Por defecto | Descripción |
|---|---|---|---|
| `DB_URL_MULTIMEDIA` | Sí | — | URL JDBC, p. ej. `jdbc:mysql://localhost:3306/multimedia` |
| `DB_USERNAME_MULTIMEDIA` | Sí | — | Usuario de MySQL |
| `DB_PASSWORD_MULTIMEDIA` | Sí | — | Contraseña de MySQL |
| `JWT_SECRET_KEY` | Sí | — | Clave HMAC en Base64, de al menos 32 bytes decodificada |
| `JWT_EXPIRATION_KEY` | Sí | — | Duración del token en milisegundos (p. ej. `86400000` = 24 h) |
| `APP_ADMIN_PASSWORD` | No | `admin` | Contraseña del primer administrador. **Cámbiala** |
| `POSTERS_PATH_MULTIMEDIA` | No | `./storage/posters` | Carpeta de los pósters |
| `VIDEOS_PATH_MULTIMEDIA` | No | `./storage/videos` | Carpeta de los vídeos |
| `FFPROBE_PATH_MULTIMEDIA` | No | `ffprobe` | Ruta a `ffprobe` si no está en el PATH |
| `MAX_UPLOAD_SIZE_MULTIMEDIA` | No | `10GB` | Tamaño máximo de un vídeo subido |

Las rutas por defecto son relativas a la carpeta desde la que se arranca la aplicación. En producción, usa rutas absolutas.

Para generar una clave JWT válida:

```bash
openssl rand -base64 48
```

## Puesta en marcha

```bash
./mvnw spring-boot:run
```

En Windows (cmd o PowerShell), usa `mvnw.cmd spring-boot:run`.

Al arrancar por primera vez, Flyway crea las tablas y `DataInitializer` crea el usuario **`admin`** con la contraseña de `APP_ADMIN_PASSWORD`, porque sin un usuario inicial nadie podría iniciar sesión.

Otros comandos:

```bash
./mvnw package     # genera el jar en target/
./mvnw test        # ejecuta los tests (necesitan MySQL y las variables de entorno)
```

## Autenticación

```http
POST /login
Content-Type: application/json

{ "username": "admin", "password": "admin" }
```

```json
{ "Authentication": "<jwt>", "username": "admin", "message": "Autenticacion exitosa!!!" }
```

En el resto de peticiones, el token va en la cabecera:

```http
Authorization: Bearer <jwt>
```

Solo los usuarios con estado `ACTIVE` pueden iniciar sesión. El token guarda el nombre de usuario, así que si un usuario cambia su propio nombre tiene que volver a hacer login.

## Endpoints

Los listados son paginados (`?page=0&size=10&sort=campo,asc`, 10 por defecto). `PUT` recibe el `id` en el cuerpo, no en la URL.

### Usuarios — `/users`

| Método | Ruta | ADMIN | USER |
|---|---|---|---|
| `GET` | `/users` | Sí | No |
| `GET` | `/users/{id}` | Cualquier usuario | Solo su cuenta |
| `POST` | `/users` | Crea cuentas ADMIN o USER | No |
| `PUT` | `/users` | Su cuenta y las de USER, nunca la de otro ADMIN | Solo su cuenta, sin cambiar su rol |
| `DELETE` | `/users/{id}` | Su cuenta y las de USER, nunca la de otro ADMIN | No |

- Siempre tiene que quedar al menos un ADMIN activo: el último no puede borrarse ni degradarse a USER.
- Un ADMIN puede ascender a un USER a ADMIN.
- Los datos personales (`name`, `lastname`, `email`) se gestionan junto con el usuario.

```json
{ "username": "ana", "password": "secreta123", "rol": "USER",
  "name": "Ana", "lastname": "García", "email": "ana@example.com" }
```

### Géneros — `/genres`

| Método | Ruta | Quién |
|---|---|---|
| `GET` | `/genres`, `/genres/{id}` | Cualquier usuario autenticado |
| `POST` / `PUT` | `/genres` | ADMIN |
| `DELETE` | `/genres/{id}` | ADMIN. Falla si el género está asociado a alguna película o serie |

### Películas y series — `/movies-series`

| Método | Ruta | Quién |
|---|---|---|
| `GET` | `/movies-series?name=` | Cualquier usuario autenticado (`name` filtra por texto) |
| `GET` | `/movies-series/{id}` | Cualquier usuario autenticado |
| `GET` | `/movies-series/{id}/poster` | Cualquier usuario autenticado. Devuelve la imagen |
| `POST` / `PUT` | `/movies-series` | ADMIN. Usa `multipart/form-data` |
| `DELETE` | `/movies-series/{id}` | ADMIN. Borra también sus archivos multimedia |

Campos del formulario: `name`, `synopsis`, `releaseYear`, `classification`, `type` (`MOVIE` o `SERIE`), `genreIds` (puede repetirse) y `poster`, una imagen JPG, PNG o WEBP de hasta 5MB, opcional.

```bash
curl -X POST http://localhost:8080/movies-series \
  -H "Authorization: Bearer $TOKEN" \
  -F name="El viaje" -F synopsis="..." -F releaseYear=2024 -F type=MOVIE \
  -F genreIds=1 -F genreIds=3 -F poster=@poster.jpg
```

### Archivos multimedia — `/multimedia`

| Método | Ruta | Quién |
|---|---|---|
| `GET` | `/multimedia?movieSeriesId=` | Cualquier usuario autenticado. Ordenado por temporada y episodio |
| `GET` | `/multimedia/{id}` | Cualquier usuario autenticado |
| `GET` | `/multimedia/{id}/video` | Cualquier usuario autenticado. Streaming |
| `POST` / `PUT` | `/multimedia` | ADMIN. Usa `multipart/form-data` |
| `DELETE` | `/multimedia/{id}` | ADMIN |

Campos del formulario: `movieSeriesId`, `title`, `season`, `episodeNumber` y `video` (MP4 o WEBM; obligatorio en `POST` y opcional en `PUT`). La duración la calcula el servidor con `ffprobe`.

- Una **película** tiene un único vídeo, sin temporada ni episodio. Para cambiarlo se usa `PUT`.
- Un **episodio** de serie necesita `season` y `episodeNumber`, y esa combinación no se puede repetir en la serie.

```bash
curl -X POST http://localhost:8080/multimedia \
  -H "Authorization: Bearer $TOKEN" \
  -F movieSeriesId=2 -F season=1 -F episodeNumber=1 -F title="Piloto" -F video=@s01e01.mp4
```

**Reproducción:** el endpoint de vídeo admite peticiones `Range` (responde 206), así que el navegador puede empezar a reproducir sin descargar todo el archivo y saltar a cualquier punto. Como `<video>` no puede enviar cabeceras, este endpoint, y solo este, acepta el token en la URL:

```html
<video src="http://localhost:8080/multimedia/7/video?token=<jwt>" controls></video>
```

## Errores

Todos los errores devuelven JSON:

```json
{ "message": "El registro indicado no existe", "code": 404, "status": 404 }
```

| Código | Cuándo |
|---|---|
| 400 | Datos inválidos, valor duplicado (usuario, email, género, episodio) o regla de negocio incumplida |
| 401 | Falta el token, es inválido o el login es incorrecto |
| 403 | El rol no permite la acción |
| 404 | El registro no existe o está borrado |

Los errores de validación de campos devuelven una lista:

```json
[ { "field": "username", "message": "El nombre de usuario es obligatorio" } ]
```

## Base de datos

El esquema lo definen **solo** las migraciones de Flyway en `src/main/resources/db/migration/`. Para cambiarlo, crea una nueva migración `V<n>__Descripcion.sql`; nunca edites una ya aplicada.

- **Borrado lógico:** los registros no se eliminan, se marcan con `deleted_at` (los usuarios, además, con `status = DELETED`). Un nombre de usuario, un email o un episodio borrados se pueden volver a usar. La excepción son los géneros, que se borran físicamente.
- **Auditoría:** cada alta, cambio y baja guarda en `AUDIT_LOG` quién la hizo y el estado anterior y posterior en JSON.
- **Archivos:** en la base de datos solo se guarda el nombre del archivo, que se genera con un UUID. Los archivos viven en las carpetas configuradas y se conservan al borrar el registro.

## Estructura

```
src/main/java/MultimediaServer/
├── controllers/     Endpoints REST
├── services/        Lógica de negocio
├── repositories/    Acceso a datos (Spring Data JPA)
├── dto/             DTOs de entrada y salida por entidad
├── validations/     Reglas de negocio, una clase por regla
├── models/          Entidades JPA y enums
├── config/          Datos iniciales (primer admin)
└── infra/
    ├── errors/      Formato de errores y manejador global
    ├── security/    JWT, filtros, CORS y reglas de acceso
    └── services/    Auditoría, almacenamiento de archivos, ffprobe
```
