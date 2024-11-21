-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1:3306
-- Tiempo de generación: 21-11-2024 a las 14:52:54
-- Versión del servidor: 8.3.0
-- Versión de PHP: 8.2.18

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `biblioteca_colegio`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `auditoria`
--

DROP TABLE IF EXISTS `auditoria`;
CREATE TABLE IF NOT EXISTS `auditoria` (
  `id` int NOT NULL AUTO_INCREMENT,
  `usuario` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `accion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `fecha` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `configuraciones`
--

DROP TABLE IF EXISTS `configuraciones`;
CREATE TABLE IF NOT EXISTS `configuraciones` (
  `id` int NOT NULL AUTO_INCREMENT,
  `clave` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `valor` decimal(10,2) NOT NULL DEFAULT '0.00',
  `fecha_modificacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `clave` (`clave`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `configuraciones`
--

INSERT INTO `configuraciones` (`id`, `clave`, `valor`, `fecha_modificacion`) VALUES
(1, 'mora_diaria', 1.50, '2024-11-21 14:22:49'),
(2, 'limite_prestamos', 3.00, '2024-11-21 14:22:49');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `devoluciones`
--

DROP TABLE IF EXISTS `devoluciones`;
CREATE TABLE IF NOT EXISTS `devoluciones` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_prestamo` int NOT NULL,
  `id_usuario` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_documento` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_devolucion_real` date NOT NULL,
  `dias_mora` int DEFAULT '0',
  `monto_mora` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `id_prestamo` (`id_prestamo`),
  KEY `id_usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `devoluciones`
--

INSERT INTO `devoluciones` (`id`, `id_prestamo`, `id_usuario`, `id_documento`, `fecha_devolucion_real`, `dias_mora`, `monto_mora`) VALUES
(1, 1, 'AD00001', 'LIB0010', '2024-11-21', 0, 0.00),
(2, 4, 'AD00001', 'LIB0008', '2024-11-21', 0, 0.00),
(3, 3, 'AD00001', 'LIB0009', '2024-11-21', 0, 0.00),
(4, 2, 'AD00001', 'LIB0010', '2024-11-21', 0, 0.00),
(5, 6, 'AD00001', 'LIB0010', '2024-12-01', 3, 4.50),
(6, 6, 'AD00001', 'LIB0010', '2024-12-15', 17, 25.50);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `historial_prestamos`
--

DROP TABLE IF EXISTS `historial_prestamos`;
CREATE TABLE IF NOT EXISTS `historial_prestamos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_prestamo` int NOT NULL,
  `id_usuario` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_documento` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `accion` enum('Creación','Devolución','Mora') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `fecha` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_prestamo` (`id_prestamo`),
  KEY `id_usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `libros`
--

DROP TABLE IF EXISTS `libros`;
CREATE TABLE IF NOT EXISTS `libros` (
  `id_libros` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `Título` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Autor(es)` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Categoría` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Fecha_de_Publicación` date DEFAULT NULL,
  `Editorial` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ISBN` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Edición` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Idioma` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Sinopsis` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ubicacion_fisica` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cantidad_disponible` int DEFAULT '0',
  `cantidad_total` int DEFAULT '0',
  `estado` enum('Bueno','Dañado','En Reparación') COLLATE utf8mb4_unicode_ci DEFAULT 'Bueno',
  `palabras_clave` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id_libros`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `libros`
--

INSERT INTO `libros` (`id_libros`, `Título`, `Autor(es)`, `Categoría`, `Fecha_de_Publicación`, `Editorial`, `ISBN`, `Edición`, `Idioma`, `Sinopsis`, `ubicacion_fisica`, `cantidad_disponible`, `cantidad_total`, `estado`, `palabras_clave`) VALUES
('LIB0001', 'Cien Años de Soledad', 'Gabriel García Márquez', 'Literatura y Ficción', '1967-06-05', 'Editorial Sudamericana', '978-3-16-148410-0', 'Primera edición', 'Español', 'La historia de la familia Buendía en el pueblo de Macondo.', 'Sección A, Estantería 1, Nivel 2', 10, 10, 'Bueno', 'Realismo Mágico, Familia Buendía, Colombia'),
('LIB0002', 'Don Quijote de la Mancha', 'Miguel de Cervantes', 'Clásicos', '1605-01-16', 'Francisco de Robles', '978-84-376-0494-7', 'Primera edición', 'Español', 'Las aventuras de Don Quijote y Sancho Panza.', 'Sección B, Estantería 3, Nivel 4', 8, 8, 'Bueno', 'Clásico, Caballería, España'),
('LIB0003', '1984', 'George Orwell', 'Ciencia Ficción', '1949-06-08', 'Secker & Warburg', '978-0-452-28423-4', 'Primera edición', 'Inglés', 'Una visión distópica de una sociedad totalitaria.', 'Sección C, Estantería 2, Nivel 1', 6, 6, 'Bueno', 'Distopía, Totalitarismo, Política'),
('LIB0004', 'El Principito', 'Antoine de Saint-Exupéry', 'Ficción Infantil', '1943-04-06', 'Reynal & Hitchcock', '978-2-07-061275-8', 'Primera edición', 'Francés', 'Un piloto se encuentra con un niño de otro planeta.', 'Sección D, Estantería 4, Nivel 3', 10, 10, 'Bueno', 'Infantil, Filosofía, Amistad'),
('LIB0005', 'Orgullo y Prejuicio', 'Jane Austen', 'Romance', '1813-01-28', 'T. Egerton', '978-1-85326-000-1', 'Primera edición', 'Inglés', 'La historia de Elizabeth Bennet y su relación con el Sr. Darcy.', 'Sección E, Estantería 5, Nivel 2', 5, 5, 'Bueno', 'Romance, Inglaterra, Clásico'),
('LIB0006', 'Crimen y Castigo', 'Fiódor Dostoyevski', 'Filosofía y Psicología', '1866-01-01', 'The Russian Messenger', '978-0-679-42335-5', 'Primera edición', 'Ruso', 'La lucha moral y psicológica de un joven estudiante.', 'Sección F, Estantería 1, Nivel 1', 5, 5, 'Bueno', 'Rusia, Filosofía, Crimen'),
('LIB0007', 'La Odisea', 'Homero', 'Épico', '0800-01-01', 'Ediciones Gredos', '978-0-14-026886-7', 'Primera edición', 'Griego', 'El viaje de Odiseo para regresar a Ítaca.', 'Sección G, Estantería 3, Nivel 4', 5, 5, 'Bueno', 'Mitología, Épico, Grecia'),
('LIB0008', 'La Sombra del Viento', 'Carlos Ruiz Zafón', 'Misterio', '2001-01-01', 'Planeta', '978-84-08-03444-5', 'Primera edición', 'Español', 'Un joven encuentra un libro que cambiará su vida.', 'Sección H, Estantería 2, Nivel 3', 7, 7, 'Bueno', 'Misterio, Barcelona, Literatura'),
('LIB0009', 'Harry Potter y la Piedra Filosofal', 'J.K. Rowling', 'Fantasía', '1997-06-26', 'Bloomsbury', '978-0-7475-3269-9', 'Primera edición', 'Inglés', 'El inicio de las aventuras de Harry Potter.', 'Sección I, Estantería 5, Nivel 1', 10, 10, 'Bueno', 'Magia, Hogwarts, Fantasía'),
('LIB0010', 'El Alquimista', 'Paulo Coelho', 'Ficción', '1988-01-01', 'Rocco', '978-0-06-112241-5', 'Primera edición', 'Portugués', 'Un joven pastor busca su leyenda personal.', 'Sección J, Estantería 4, Nivel 2', 7, 7, 'Bueno', 'Filosofía, Sueños, Viaje');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pagos_mora`
--

DROP TABLE IF EXISTS `pagos_mora`;
CREATE TABLE IF NOT EXISTS `pagos_mora` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_prestamo` int NOT NULL,
  `monto_pagado` decimal(10,2) NOT NULL,
  `fecha_pago` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_prestamo` (`id_prestamo`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `pagos_mora`
--

INSERT INTO `pagos_mora` (`id`, `id_prestamo`, `monto_pagado`, `fecha_pago`) VALUES
(1, 6, 25.50, '2024-11-21 14:45:40');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `prestamos`
--

DROP TABLE IF EXISTS `prestamos`;
CREATE TABLE IF NOT EXISTS `prestamos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_usuario` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_documento` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `dias_prestamo` int NOT NULL,
  `fecha_prestamo` date NOT NULL,
  `fecha_devolucion` date NOT NULL,
  `estado` enum('Pendiente','Devuelto','Mora') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Pendiente',
  `dias_mora` int DEFAULT NULL,
  `monto_mora` decimal(10,2) DEFAULT NULL,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `prestamos`
--

INSERT INTO `prestamos` (`id`, `id_usuario`, `id_documento`, `dias_prestamo`, `fecha_prestamo`, `fecha_devolucion`, `estado`, `dias_mora`, `monto_mora`, `fecha_registro`) VALUES
(1, 'AD00001', 'LIB0010', 7, '2024-11-21', '2024-11-28', 'Devuelto', 0, 0.00, '2024-11-21 14:33:29'),
(2, 'AD00001', 'LIB0010', 7, '2024-11-21', '2024-11-28', 'Devuelto', 0, 0.00, '2024-11-21 14:37:16'),
(3, 'AD00001', 'LIB0009', 7, '2024-11-21', '2024-11-28', 'Devuelto', 0, 0.00, '2024-11-21 14:37:19'),
(4, 'AD00001', 'LIB0008', 7, '2024-11-21', '2024-11-28', 'Devuelto', 0, 0.00, '2024-11-21 14:37:22'),
(5, 'AD00001', 'LIB0010', 7, '2024-11-21', '2024-11-28', 'Pendiente', NULL, NULL, '2024-11-21 14:39:11'),
(6, 'AD00001', 'LIB0010', 7, '2024-11-21', '2024-11-28', 'Devuelto', 17, 25.50, '2024-11-21 14:39:14');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipos_documentos`
--

DROP TABLE IF EXISTS `tipos_documentos`;
CREATE TABLE IF NOT EXISTS `tipos_documentos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `tipos_documentos`
--

INSERT INTO `tipos_documentos` (`id`, `nombre`, `fecha_creacion`) VALUES
(2, 'Libros', '2024-11-21 14:28:05');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE IF NOT EXISTS `usuarios` (
  `id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `rol` enum('Administrador','Profesor','Alumno') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `contraseña` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `direccion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `email`, `rol`, `contraseña`, `telefono`, `direccion`, `fecha_nacimiento`, `fecha_registro`) VALUES
('AD00001', 'Administrador General', 'admin@colegio.com', 'Administrador', 'admin123', '555-1234', 'Oficina Central', '1975-05-20', '2024-11-21 14:22:49'),
('AL00001', 'Alumno Pérez', 'alumno.perez@colegio.com', 'Alumno', 'alumno123', '555-9101', 'Residencia Universitaria', '2001-08-15', '2024-11-21 14:22:49'),
('PR00001', 'Profesor López', 'profesor.lopez@colegio.com', 'Profesor', 'profesor123', '555-5678', 'Facultad de Ciencias', '1985-11-10', '2024-11-21 14:22:49');

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `devoluciones`
--
ALTER TABLE `devoluciones`
  ADD CONSTRAINT `devoluciones_ibfk_1` FOREIGN KEY (`id_prestamo`) REFERENCES `prestamos` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `devoluciones_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `historial_prestamos`
--
ALTER TABLE `historial_prestamos`
  ADD CONSTRAINT `historial_prestamos_ibfk_1` FOREIGN KEY (`id_prestamo`) REFERENCES `prestamos` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `historial_prestamos_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `prestamos`
--
ALTER TABLE `prestamos`
  ADD CONSTRAINT `prestamos_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
