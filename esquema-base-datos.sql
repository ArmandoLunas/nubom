-- ============================================================
-- ESQUEMA DE BASE DE DATOS - NUBOM (Ni Un Bocado Menos)
-- ============================================================
-- Este archivo es solo para ENTREGA/DOCUMENTACION. La aplicacion
-- genera este mismo esquema automaticamente al arrancar gracias a
-- spring.jpa.hibernate.ddl-auto=create (ver application.properties),
-- y lo siembra con datos de prueba via data.sql.
--
-- Motor: MariaDB
-- Base de datos: nubom
-- Tablas: 13 (8 de la entrega del modulo 7 + 5 de la version final)
-- ============================================================

CREATE DATABASE IF NOT EXISTS nubom;
USE nubom;

-- ------------------------------------------------------------
-- Catalogos
-- ------------------------------------------------------------

-- Roles dentro de un hogar
CREATE TABLE rol (
    id_rol      INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL,   -- PROPIETARIO / FAMILIAR
    descripcion VARCHAR(255)
);

-- Tipos de inventario
CREATE TABLE inventario_tipo (
    id_tipo     INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL,   -- Refrigerador / Alacena
    descripcion VARCHAR(255),
    icono       VARCHAR(50)              -- clase de bootstrap-icons
);

-- ------------------------------------------------------------
-- Usuarios y hogares
-- ------------------------------------------------------------

-- Usuarios del sistema.
-- contrasena guarda el hash BCrypt (60 caracteres), nunca el texto.
-- rol_sistema es el rol global: se traduce a ROLE_USUARIO / ROLE_ADMIN en Spring Security.
CREATE TABLE usuario (
    id_usuario     INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    correo         VARCHAR(150) NOT NULL UNIQUE,
    contrasena     VARCHAR(255) NOT NULL,
    rol_sistema    ENUM('ADMIN', 'USUARIO') NOT NULL DEFAULT 'USUARIO',
    fecha_registro DATETIME,
    activo         BOOLEAN DEFAULT TRUE     -- baja logica
);

CREATE TABLE hogar (
    id_hogar        INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    fecha_creacion  DATETIME
);

-- Relacion N:M usuario <-> hogar con atributos propios (rol dentro de ESE hogar,
-- fecha de union, activo). Se modela como entidad de union con tres @ManyToOne.
CREATE TABLE usuario_hogar (
    id_usuario_hogar INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario       INT NOT NULL,
    id_hogar         INT NOT NULL,
    id_rol           INT NOT NULL,
    fecha_union      DATETIME,
    activo           BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_usuario_hogar_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_usuario_hogar_hogar   FOREIGN KEY (id_hogar)   REFERENCES hogar (id_hogar),
    CONSTRAINT fk_usuario_hogar_rol     FOREIGN KEY (id_rol)     REFERENCES rol (id_rol)
);

-- ------------------------------------------------------------
-- Inventario
-- ------------------------------------------------------------

-- Inventarios (refrigerador / alacena) de cada hogar.
-- Cada hogar recibe 2 al crearse (la API permite crear adicionales).
CREATE TABLE inventario (
    id_inventario     INT AUTO_INCREMENT PRIMARY KEY,
    id_hogar          INT NOT NULL,
    id_tipo           INT NOT NULL,
    nombre            VARCHAR(100) NOT NULL,
    capacidad_maxima  INT NOT NULL,              -- NUMERO DE OBJETOS: 20, 40 u 80
    estilo            VARCHAR(20) DEFAULT 'clasico', -- clasico / moderno / retro (como se dibuja)
    fecha_creacion    DATETIME,
    CONSTRAINT fk_inventario_hogar FOREIGN KEY (id_hogar) REFERENCES hogar (id_hogar),
    CONSTRAINT fk_inventario_tipo  FOREIGN KEY (id_tipo)  REFERENCES inventario_tipo (id_tipo)
);

-- Productos guardados dentro de un inventario.
CREATE TABLE producto (
    id_producto      INT AUTO_INCREMENT PRIMARY KEY,
    id_inventario    INT NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    cantidad         INT NOT NULL,
    unidad           VARCHAR(20) NOT NULL,      -- pza, kg, g, L, ml, paquete
    categoria        VARCHAR(50) NOT NULL,      -- Lácteos, Verduras, Frutas, etc.
    fecha_caducidad  DATE,                      -- opcional
    codigo_barras    VARCHAR(13),               -- opcional (8 a 13 digitos)
    fecha_ingreso    DATETIME,
    activo           BOOLEAN DEFAULT TRUE,      -- baja logica
    CONSTRAINT fk_producto_inventario FOREIGN KEY (id_inventario) REFERENCES inventario (id_inventario)
);

-- Lista de compras de cada hogar.
CREATE TABLE lista_compra (
    id_item         INT AUTO_INCREMENT PRIMARY KEY,
    id_hogar        INT NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    categoria       VARCHAR(50) NOT NULL,
    cantidad        INT NOT NULL,
    unidad          VARCHAR(20) NOT NULL,
    comprado        BOOLEAN DEFAULT FALSE,
    fecha_creacion  DATETIME,
    CONSTRAINT fk_lista_compra_hogar FOREIGN KEY (id_hogar) REFERENCES hogar (id_hogar)
);

-- ------------------------------------------------------------
-- Recetas
-- ------------------------------------------------------------

-- estado: PRIVADA -> (compartir) -> PENDIENTE -> (moderacion) -> APROBADA o RECHAZADA.
-- Solo las APROBADA son visibles para otros usuarios.
-- fecha_creacion / fecha_modificacion las llena Spring Data JPA Auditing.
CREATE TABLE receta (
    id_receta           INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario          INT NOT NULL,              -- autor
    nombre              VARCHAR(120) NOT NULL,
    descripcion         VARCHAR(500),
    pasos               TEXT,                      -- un paso por linea
    tiempo_minutos      INT,
    estado              ENUM('APROBADA', 'PENDIENTE', 'PRIVADA', 'RECHAZADA') NOT NULL,
    fecha_creacion      DATETIME,
    fecha_modificacion  DATETIME,
    CONSTRAINT fk_receta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

CREATE TABLE receta_ingrediente (
    id_ingrediente  INT AUTO_INCREMENT PRIMARY KEY,
    id_receta       INT NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    cantidad        DOUBLE,
    unidad          VARCHAR(20),
    principal       BOOLEAN NOT NULL,              -- TRUE = principal, FALSE = complementario
    CONSTRAINT fk_ingrediente_receta FOREIGN KEY (id_receta) REFERENCES receta (id_receta)
);

-- Una calificacion por usuario y receta.
CREATE TABLE receta_calificacion (
    id_calificacion  INT AUTO_INCREMENT PRIMARY KEY,
    id_receta        INT NOT NULL,
    id_usuario       INT NOT NULL,
    puntuacion       INT NOT NULL,                 -- 1 a 5
    fecha            DATETIME,
    CONSTRAINT uk_calificacion_receta_usuario UNIQUE (id_receta, id_usuario),
    CONSTRAINT fk_calificacion_receta  FOREIGN KEY (id_receta)  REFERENCES receta (id_receta),
    CONSTRAINT fk_calificacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

-- ------------------------------------------------------------
-- Avisos y seguridad
-- ------------------------------------------------------------

-- Avisos de caducidad. Los genera la tarea programada: como maximo uno por
-- producto, usuario y dia. Si el producto se elimina fisicamente (al borrar
-- su inventario o su hogar), sus avisos se van con el.
CREATE TABLE notificacion (
    id_notificacion  INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario       INT NOT NULL,
    id_producto      INT,
    mensaje          VARCHAR(255) NOT NULL,
    leida            BOOLEAN NOT NULL,
    fecha_creacion   DATETIME,
    CONSTRAINT fk_notificacion_usuario  FOREIGN KEY (id_usuario)  REFERENCES usuario (id_usuario),
    CONSTRAINT fk_notificacion_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto) ON DELETE CASCADE
);

-- Refresh tokens de la API. Se guarda el hash SHA-256 del token, no el token.
-- Cada renovacion marca el usado como revocado y crea uno nuevo (rotacion).
CREATE TABLE refresh_token (
    id_token        INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario      INT NOT NULL,
    token_hash      VARCHAR(64) NOT NULL UNIQUE,
    expira_en       DATETIME NOT NULL,
    revocado        BOOLEAN NOT NULL,
    fecha_creacion  DATETIME,
    CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

-- ============================================================
-- Reglas de negocio (implementadas en los servicios), documentadas
-- aqui como referencia:
--
--   Al crear un hogar se generan automaticamente 2 inventarios:
--     Refrigerador -> capacidad_maxima inicial = 20 objetos
--     Alacena      -> capacidad_maxima inicial = 40 objetos
--   El propietario puede cambiar despues esa capacidad entre 20, 40 u 80
--   objetos (no por debajo de los productos que ya tiene guardados).
--
--   No se puede agregar un producto si el inventario ya alcanzo su
--   capacidad_maxima (cantidad de filas activas en "producto").
--
--   Un producto esta "por vencer" cuando faltan 3 dias o menos para su
--   fecha_caducidad.
--
--   Una receta necesita al menos un ingrediente principal. Nadie puede
--   calificar su propia receta.
-- ============================================================
