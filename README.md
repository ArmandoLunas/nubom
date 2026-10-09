# NUBOM · Ni Un Bocado Menos

Aplicación web para administrar el inventario de alimentos del hogar (refrigerador y alacena): productos con
seguimiento de caducidad, lista de compras, recetas compartidas con calificación, reportes y colaboración entre
los integrantes de una familia.

Proyecto final del Diplomado *Desarrollo de Sistemas con Tecnología Java* (DGTIC UNAM).

## Qué incluye

| Módulo | Funcionalidad |
|---|---|
| Usuarios y seguridad | Registro, inicio de sesión, perfil, cambio de contraseña, baja de cuenta |
| Hogar compartido | Crear hogar, agregar y retirar familiares, perfiles Propietario y Familiar |
| Inventario | CRUD de productos, vista gráfica por estilo, capacidad, autocompletado por código de barras |
| Caducidad y avisos | Semáforo de caducidad, avisos diarios en la aplicación y por correo |
| Lista de compras | CRUD, marcar como comprado, compartir o copiar la lista como texto |
| Recetas | CRUD con ingredientes principales y complementarios, compartir, moderación, calificación |
| Reportes | Por caducar, por categoría, ocupación y recetas mejor calificadas |
| API REST | Los mismos recursos bajo `/api/v1`, con JWT y refresh token |

## Tecnologías

Java 17 · Spring Boot 3.5 · Spring Web MVC · Thymeleaf · Spring Data JPA (Hibernate) · Spring Security ·
JJWT · MariaDB · Bootstrap 5 · JUnit 5 y Mockito.

## Requisitos

- JDK 17
- MariaDB 10.7 o posterior en `localhost:3306`
- Conexión a internet (Bootstrap por CDN y consulta a Open Food Facts)

## Ejecución

```bash
mvnw.cmd spring-boot:run
```

Después abre <http://localhost:8080>.

La base de datos `nubom` se crea sola. **En desarrollo el esquema se recrea en cada arranque** y se cargan los
datos de `src/main/resources/data.sql`, así que los cambios hechos en la aplicación se pierden al reiniciar.

### Usuarios de prueba

Todos comparten la contraseña de desarrollo `Nubom2026!`.

| Correo | Perfil |
|---|---|
| `armando@correo.com` | Propietario de "Casa Luna" |
| `correo1@correo.com` | Familiar de "Casa Luna" |
| `correo2@correo.com` | Sin hogar (para probar la creación de un hogar) |
| `ana@correo.com` | Propietaria de "Casa Rivera" |
| `admin@correo.com` | Administrador (modera recetas y mantiene catálogos) |

## Configuración

Todo está en `src/main/resources/application.properties`. La contraseña de la base de datos y la clave de
los JWT no vienen en el repositorio. Antes del primer arranque copia `application-local.properties.example`
como `application-local.properties` (en la raíz del proyecto) y llena tus valores, o defínelos como
variables de entorno:

```bash
cp application-local.properties.example application-local.properties
```

| Variable | Para qué sirve | Valor por omisión |
|---|---|---|
| `NUBOM_DB_USER` | Usuario de MariaDB | `root` |
| `NUBOM_DB_PASSWORD` | Contraseña de MariaDB | ninguno (obligatoria) |
| `NUBOM_JWT_SECRET` | Clave de firma de los JWT (32 bytes o más, en Base64) | ninguno (obligatoria) |
| `NUBOM_MAIL_ENABLED` | `true` para enviar correos reales | `false` |
| `NUBOM_MAIL_HOST` | Servidor SMTP | `smtp.gmail.com` |
| `NUBOM_MAIL_USER` / `NUBOM_MAIL_PASSWORD` | Cuenta SMTP | vacío |

Fuera de desarrollo hay que cambiar además
`spring.jpa.hibernate.ddl-auto` a `validate` y `spring.sql.init.mode` a `never`.

El envío de correo está desactivado por omisión: los avisos de caducidad se generan y se ven en la aplicación,
y en la bitácora queda registrado el correo que se habría enviado.

## Seguridad

Hay dos cadenas de filtros de Spring Security (`dgtic.core.security.SecurityConfig`):

- **Aplicación web** (todo excepto `/api/**`): formulario de inicio de sesión, sesión HTTP y protección CSRF.
- **API REST** (`/api/**`): sin estado, con JWT en `Authorization: Bearer`.

Las contraseñas se guardan con BCrypt. El rol global (`USUARIO` o `ADMIN`) se toma de `usuario.rol_sistema`;
el rol dentro del hogar (`PROPIETARIO` o `FAMILIAR`) se comprueba por recurso con `@PreAuthorize` y el bean
`hogarSeguridad`.

### Uso de la API

```bash
# 1. Iniciar sesión: devuelve accessToken (15 min) y refreshToken (7 días)
curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d "{\"correo\":\"armando@correo.com\",\"contrasena\":\"Nubom2026!\"}"

# 2. Usar el access token
curl http://localhost:8080/api/v1/productos -H "Authorization: Bearer <accessToken>"

# 3. Renovar (el refresh token es de un solo uso: cada renovación entrega uno nuevo)
curl -X POST http://localhost:8080/api/v1/auth/refresh \
     -H "Content-Type: application/json" -d "{\"refreshToken\":\"<refreshToken>\"}"
```

## Pruebas

```bash
mvnw.cmd test
```

Las pruebas de integración usan una base aparte (`nubom_test`, perfil `test`) para no tocar los datos de
desarrollo.

## Estructura

```
src/main/java/dgtic/core
├── security      Configuración de Spring Security, JWT y refresh token
├── controller    Controladores MVC (vistas Thymeleaf)
├── rest          API REST: controladores, DTOs, mappers, servicios y manejo de errores
├── service       Reglas de negocio
├── repository    Repositorios Spring Data JPA
├── model         Entidades JPA y DTOs de formularios
├── client        Cliente de Open Food Facts
├── scheduler     Tareas programadas (avisos de caducidad, limpieza de tokens)
└── config, validation, converter, interceptor
```

## Documentación

- `docs/Documento_Especificacion_NUBOM.pdf`: especificación técnica y arquitectura.
- `docs/documentacion_nubom_api.pdf` y `docs/guia_pruebas_api_nubom.pdf`: API del módulo 7. Son anteriores a
  la seguridad: hoy todas esas rutas requieren token.
- `esquema-base-datos.sql`: esquema de referencia en SQL.
