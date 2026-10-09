-- ============================================================
-- DATOS INICIALES DE DESARROLLO - NUBOM
-- Se cargan en cada arranque (el esquema se recrea con ddl-auto=create).
-- ============================================================

-- Catalogo de roles dentro de un hogar
insert into rol (nombre, descripcion) values ('PROPIETARIO', 'Administra por completo el hogar, sus inventarios y puede invitar o eliminar miembros');
insert into rol (nombre, descripcion) values ('FAMILIAR', 'Puede consultar y actualizar el inventario del hogar, sin permisos de administracion');

-- Catalogo de tipos de inventario (2 tipos, compartidos por todos los hogares)
insert into inventario_tipo (nombre, descripcion, icono) values ('Refrigerador', 'Almacenamiento refrigerado para perecederos', 'bi-snow2');
insert into inventario_tipo (nombre, descripcion, icono) values ('Alacena', 'Almacenamiento a temperatura ambiente para abarrotes', 'bi-basket2');

-- ============================================================
-- Usuarios de prueba. La columna contrasena guarda el hash BCrypt, nunca el
-- texto. Los cinco comparten la misma contrasena de desarrollo: Nubom2026!
--   id 1  armando@correo.com  USUARIO  propietario de "Casa Luna"
--   id 2  correo1@correo.com  USUARIO  familiar de "Casa Luna"
--   id 3  correo2@correo.com  USUARIO  sin hogar (para probar "crear hogar")
--   id 4  admin@correo.com    ADMIN    modera recetas y administra catalogos
--   id 5  ana@correo.com      USUARIO  propietaria de "Casa Rivera"
-- ============================================================
insert into usuario (nombre, correo, contrasena, rol_sistema, fecha_registro, activo) values
('Armando Luna', 'armando@correo.com', '$2a$10$Llf20YjqyUsMiaD7NAgQRuzFCLRUBjHq8rLYidgj.9RpKUQCzfaJK', 'USUARIO', now(), true);
insert into usuario (nombre, correo, contrasena, rol_sistema, fecha_registro, activo) values
('Usuario Uno', 'correo1@correo.com', '$2a$10$Llf20YjqyUsMiaD7NAgQRuzFCLRUBjHq8rLYidgj.9RpKUQCzfaJK', 'USUARIO', now(), true);
insert into usuario (nombre, correo, contrasena, rol_sistema, fecha_registro, activo) values
('Usuario Dos', 'correo2@correo.com', '$2a$10$Llf20YjqyUsMiaD7NAgQRuzFCLRUBjHq8rLYidgj.9RpKUQCzfaJK', 'USUARIO', now(), true);
insert into usuario (nombre, correo, contrasena, rol_sistema, fecha_registro, activo) values
('Administrador NUBOM', 'admin@correo.com', '$2a$10$Llf20YjqyUsMiaD7NAgQRuzFCLRUBjHq8rLYidgj.9RpKUQCzfaJK', 'ADMIN', now(), true);
insert into usuario (nombre, correo, contrasena, rol_sistema, fecha_registro, activo) values
('Ana Torres', 'ana@correo.com', '$2a$10$Llf20YjqyUsMiaD7NAgQRuzFCLRUBjHq8rLYidgj.9RpKUQCzfaJK', 'USUARIO', now(), true);

-- ============================================================
-- Hogares (3 en total). "Departamento Sur" no tiene integrantes: sirve para
-- comprobar que nadie ajeno puede consultarlo por la API (403).
-- ============================================================
insert into hogar (nombre, fecha_creacion) values ('Casa Luna', now());
insert into hogar (nombre, fecha_creacion) values ('Casa Rivera', now());
insert into hogar (nombre, fecha_creacion) values ('Departamento Sur', now());

-- Membresias
insert into usuario_hogar (id_usuario, id_hogar, id_rol, fecha_union, activo) values (1, 1, 1, now(), true);
insert into usuario_hogar (id_usuario, id_hogar, id_rol, fecha_union, activo) values (2, 1, 2, now(), true);
insert into usuario_hogar (id_usuario, id_hogar, id_rol, fecha_union, activo) values (5, 2, 1, now(), true);

-- ============================================================
-- Inventarios: cada hogar tiene sus 2 inventarios (Refrigerador y Alacena).
--   id_inventario 1, 2 -> Casa Luna        3, 4 -> Casa Rivera        5, 6 -> Departamento Sur
-- ============================================================
insert into inventario (id_hogar, id_tipo, nombre, capacidad_maxima, estilo, fecha_creacion) values (1, 1, 'Refrigerador', 20, 'clasico', now());
insert into inventario (id_hogar, id_tipo, nombre, capacidad_maxima, estilo, fecha_creacion) values (1, 2, 'Alacena', 40, 'clasico', now());
insert into inventario (id_hogar, id_tipo, nombre, capacidad_maxima, estilo, fecha_creacion) values (2, 1, 'Refrigerador', 20, 'moderno', now());
insert into inventario (id_hogar, id_tipo, nombre, capacidad_maxima, estilo, fecha_creacion) values (2, 2, 'Alacena', 40, 'retro', now());
insert into inventario (id_hogar, id_tipo, nombre, capacidad_maxima, estilo, fecha_creacion) values (3, 1, 'Refrigerador', 20, 'clasico', now());
insert into inventario (id_hogar, id_tipo, nombre, capacidad_maxima, estilo, fecha_creacion) values (3, 2, 'Alacena', 40, 'clasico', now());

-- ============================================================
-- Productos. Las fechas de caducidad son relativas a hoy para que siempre
-- haya productos vencidos, por vencer y vigentes.
-- ============================================================

-- Casa Luna > Refrigerador (id_inventario = 1)
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(1, 'Espinaca', 1, 'paquete', 'Verduras', date_sub(curdate(), interval 1 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(1, 'Yogurt natural', 2, 'pza', 'Lácteos', date_add(curdate(), interval 1 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(1, 'Leche entera', 2, 'L', 'Lácteos', date_add(curdate(), interval 2 day), '7501055300075', now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(1, 'Pechuga de pollo', 1, 'kg', 'Carnes', date_add(curdate(), interval 3 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(1, 'Queso panela', 1, 'pza', 'Lácteos', date_add(curdate(), interval 6 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(1, 'Jitomate', 1, 'kg', 'Verduras', date_add(curdate(), interval 5 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(1, 'Huevo', 12, 'pza', 'Abarrotes', date_add(curdate(), interval 14 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(1, 'Manzana', 6, 'pza', 'Frutas', date_add(curdate(), interval 10 day), null, now(), true);

-- Casa Luna > Alacena (id_inventario = 2)
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(2, 'Pan de caja', 1, 'paquete', 'Panadería', date_add(curdate(), interval 3 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(2, 'Arroz', 2, 'kg', 'Granos', date_add(curdate(), interval 240 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(2, 'Frijol negro', 1, 'kg', 'Granos', date_add(curdate(), interval 180 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(2, 'Atún en lata', 4, 'pza', 'Abarrotes', date_add(curdate(), interval 400 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(2, 'Avena', 1, 'paquete', 'Granos', date_add(curdate(), interval 90 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(2, 'Aceite vegetal', 1, 'L', 'Abarrotes', null, null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(2, 'Jugo de naranja', 2, 'L', 'Bebidas', date_add(curdate(), interval 30 day), null, now(), true);

-- Casa Rivera > Refrigerador (id_inventario = 3)
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(3, 'Zanahoria', 1, 'kg', 'Verduras', date_add(curdate(), interval 2 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(3, 'Crema', 1, 'pza', 'Lácteos', date_add(curdate(), interval 8 day), null, now(), true);

-- Casa Rivera > Alacena (id_inventario = 4)
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(4, 'Pasta', 2, 'paquete', 'Granos', date_add(curdate(), interval 300 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(4, 'Lentejas', 1, 'kg', 'Granos', date_add(curdate(), interval 200 day), null, now(), true);

-- Departamento Sur > Refrigerador (id_inventario = 5)
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(5, 'Huevo', 12, 'pza', 'Abarrotes', date_add(curdate(), interval 12 day), null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(5, 'Jugo de naranja', 1, 'L', 'Bebidas', date_add(curdate(), interval 20 day), null, now(), true);

-- Departamento Sur > Alacena (id_inventario = 6)
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(6, 'Café molido', 1, 'paquete', 'Abarrotes', null, null, now(), true);
insert into producto (id_inventario, nombre, cantidad, unidad, categoria, fecha_caducidad, codigo_barras, fecha_ingreso, activo) values
(6, 'Azúcar', 1, 'kg', 'Abarrotes', null, null, now(), true);

-- ============================================================
-- Lista de compras: cada hogar tiene la suya.
-- ============================================================

-- Casa Luna (id_hogar = 1)
insert into lista_compra (id_hogar, nombre, categoria, cantidad, unidad, comprado, fecha_creacion) values
(1, 'Tortillas', 'Panadería', 1, 'kg', false, now());
insert into lista_compra (id_hogar, nombre, categoria, cantidad, unidad, comprado, fecha_creacion) values
(1, 'Huevo', 'Abarrotes', 1, 'kg', false, now());
insert into lista_compra (id_hogar, nombre, categoria, cantidad, unidad, comprado, fecha_creacion) values
(1, 'Jitomate', 'Verduras', 2, 'kg', true, now());
insert into lista_compra (id_hogar, nombre, categoria, cantidad, unidad, comprado, fecha_creacion) values
(1, 'Detergente', 'Limpieza', 1, 'pza', false, now());
insert into lista_compra (id_hogar, nombre, categoria, cantidad, unidad, comprado, fecha_creacion) values
(1, 'Aguacate', 'Verduras', 3, 'pza', false, now());

-- Casa Rivera (id_hogar = 2)
insert into lista_compra (id_hogar, nombre, categoria, cantidad, unidad, comprado, fecha_creacion) values
(2, 'Papel higiénico', 'Limpieza', 1, 'paquete', false, now());
insert into lista_compra (id_hogar, nombre, categoria, cantidad, unidad, comprado, fecha_creacion) values
(2, 'Plátano', 'Frutas', 1, 'kg', false, now());
insert into lista_compra (id_hogar, nombre, categoria, cantidad, unidad, comprado, fecha_creacion) values
(2, 'Pan de caja', 'Panadería', 1, 'pza', true, now());

-- Departamento Sur (id_hogar = 3)
insert into lista_compra (id_hogar, nombre, categoria, cantidad, unidad, comprado, fecha_creacion) values
(3, 'Leche deslactosada', 'Lácteos', 2, 'L', false, now());
insert into lista_compra (id_hogar, nombre, categoria, cantidad, unidad, comprado, fecha_creacion) values
(3, 'Cebolla', 'Verduras', 1, 'kg', false, now());

-- ============================================================
-- Recetas. Los pasos van uno por linea.
--   1 Ensalada de espinaca y queso   Ana       APROBADA
--   2 Tostadas de pan con yogurt     Armando   APROBADA
--   3 Sopa de verduras               Ana       APROBADA
--   4 Pollo al horno con zanahoria   Usuario 1 PENDIENTE (espera moderacion)
--   5 Agua de limón con chía         Armando   PRIVADA
--   6 Arroz rojo                     Armando   APROBADA
-- ============================================================
insert into receta (id_usuario, nombre, descripcion, pasos, tiempo_minutos, estado, fecha_creacion, fecha_modificacion) values
(5, 'Ensalada de espinaca y queso', 'Fresca, rápida y usa lo que ya tienes en el refrigerador.',
'Lava y seca la espinaca.
Corta el queso panela en cubos y el jitomate en gajos.
Mezcla todo en un tazón.
Adereza con aceite, limón y sal al gusto.', 15, 'APROBADA', now(), now());
insert into receta (id_usuario, nombre, descripcion, pasos, tiempo_minutos, estado, fecha_creacion, fecha_modificacion) values
(1, 'Tostadas de pan con yogurt', 'Desayuno ligero para no desperdiciar el pan que está por vencer.',
'Tuesta las rebanadas de pan.
Unta una capa de yogurt natural.
Agrega rodajas de manzana y un poco de avena.
Sirve de inmediato.', 10, 'APROBADA', now(), now());
insert into receta (id_usuario, nombre, descripcion, pasos, tiempo_minutos, estado, fecha_creacion, fecha_modificacion) values
(5, 'Sopa de verduras', 'Ideal para aprovechar verduras próximas a caducar.',
'Pica la zanahoria, el jitomate y la cebolla.
Sofríe la cebolla hasta que esté transparente.
Agrega el resto de las verduras y cubre con agua.
Cocina 25 minutos y sazona con sal.', 35, 'APROBADA', now(), now());
insert into receta (id_usuario, nombre, descripcion, pasos, tiempo_minutos, estado, fecha_creacion, fecha_modificacion) values
(2, 'Pollo al horno con zanahoria', 'Receta completa para la comida del domingo.',
'Precalienta el horno a 200 grados.
Sazona el pollo con sal, pimienta y ajo.
Acomoda el pollo y la zanahoria en una charola.
Hornea 40 minutos o hasta que el pollo esté dorado.', 50, 'PENDIENTE', now(), now());
insert into receta (id_usuario, nombre, descripcion, pasos, tiempo_minutos, estado, fecha_creacion, fecha_modificacion) values
(1, 'Agua de limón con chía', 'Refrescante y lista en cinco minutos.',
'Exprime los limones en una jarra con agua.
Agrega la chía y el azúcar.
Mezcla y deja reposar 5 minutos antes de servir.', 5, 'PRIVADA', now(), now());
insert into receta (id_usuario, nombre, descripcion, pasos, tiempo_minutos, estado, fecha_creacion, fecha_modificacion) values
(1, 'Arroz rojo', 'El acompañamiento de siempre, con jitomate natural.',
'Licúa el jitomate con ajo y cebolla.
Sofríe el arroz hasta que cambie de color.
Agrega el jitomate licuado y el agua.
Tapa y cocina a fuego bajo 20 minutos.', 30, 'APROBADA', now(), now());

-- Ingredientes (principal = true es indispensable para la receta)
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (1, 'Espinaca', 1, 'paquete', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (1, 'Queso panela', 200, 'g', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (1, 'Jitomate', 2, 'pza', false);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (1, 'Limón', 1, 'pza', false);

insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (2, 'Pan de caja', 4, 'pza', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (2, 'Yogurt natural', 1, 'pza', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (2, 'Manzana', 1, 'pza', false);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (2, 'Avena', 2, 'cda', false);

insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (3, 'Zanahoria', 3, 'pza', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (3, 'Jitomate', 2, 'pza', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (3, 'Cebolla', 0.5, 'pza', false);

insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (4, 'Pechuga de pollo', 1, 'kg', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (4, 'Zanahoria', 4, 'pza', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (4, 'Ajo', 2, 'pza', false);

insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (5, 'Limón', 4, 'pza', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (5, 'Chía', 1, 'cda', false);

insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (6, 'Arroz', 1, 'taza', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (6, 'Jitomate', 3, 'pza', true);
insert into receta_ingrediente (id_receta, nombre, cantidad, unidad, principal) values (6, 'Cebolla', 0.25, 'pza', false);

-- Calificaciones (una por usuario y receta; nadie califica su propia receta)
insert into receta_calificacion (id_receta, id_usuario, puntuacion, fecha) values (1, 1, 5, now());
insert into receta_calificacion (id_receta, id_usuario, puntuacion, fecha) values (1, 2, 4, now());
insert into receta_calificacion (id_receta, id_usuario, puntuacion, fecha) values (2, 2, 4, now());
insert into receta_calificacion (id_receta, id_usuario, puntuacion, fecha) values (2, 5, 5, now());
insert into receta_calificacion (id_receta, id_usuario, puntuacion, fecha) values (3, 1, 5, now());
insert into receta_calificacion (id_receta, id_usuario, puntuacion, fecha) values (3, 2, 5, now());
insert into receta_calificacion (id_receta, id_usuario, puntuacion, fecha) values (6, 5, 4, now());
insert into receta_calificacion (id_receta, id_usuario, puntuacion, fecha) values (6, 2, 3, now());
