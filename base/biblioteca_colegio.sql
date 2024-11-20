-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1:3306
-- Tiempo de generación: 20-11-2024 a las 05:08:53
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
CREATE DATABASE IF NOT EXISTS `biblioteca_colegio` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `biblioteca_colegio`;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `asdasd`
--

DROP TABLE IF EXISTS `asdasd`;
CREATE TABLE IF NOT EXISTS `asdasd` (
  `id_asdasd` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `a` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `b` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ubicacion_fisica` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cantidad_disponible` int DEFAULT '0',
  `estado` enum('Bueno','Dañado','En Reparación') COLLATE utf8mb4_unicode_ci DEFAULT 'Bueno',
  `palabras_clave` text COLLATE utf8mb4_unicode_ci,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_asdasd`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cds`
--

DROP TABLE IF EXISTS `cds`;
CREATE TABLE IF NOT EXISTS `cds` (
  `id_cds` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `a` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `b` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `c` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ubicacion_fisica` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cantidad_total` int DEFAULT '0',
  `cantidad_disponible` int DEFAULT '0',
  `estado` enum('Bueno','Dañado','En Reparación') COLLATE utf8mb4_unicode_ci DEFAULT 'Bueno',
  `palabras_clave` text COLLATE utf8mb4_unicode_ci,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `Si` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Prueba` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id_cds`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `configuraciones`
--

DROP TABLE IF EXISTS `configuraciones`;
CREATE TABLE IF NOT EXISTS `configuraciones` (
  `id` int NOT NULL AUTO_INCREMENT,
  `clave` varchar(191) COLLATE utf8mb4_unicode_ci NOT NULL,
  `valor` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_modificacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `clave` (`clave`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `configuraciones`
--

INSERT INTO `configuraciones` (`id`, `clave`, `valor`, `fecha_modificacion`) VALUES
(1, 'mora_diaria', '1.50', '2024-11-20 01:40:13'),
(2, 'limite_prestamos', '3', '2024-11-20 01:40:13');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `libros`
--

DROP TABLE IF EXISTS `libros`;
CREATE TABLE IF NOT EXISTS `libros` (
  `id_libros` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `DEscripcion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Amigo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `YA_CAsi` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ubicacion_fisica` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cantidad_total` int DEFAULT '0',
  `cantidad_disponible` int DEFAULT '0',
  `estado` enum('Bueno','Dañado','En Reparación') COLLATE utf8mb4_unicode_ci DEFAULT 'Bueno',
  `palabras_clave` text COLLATE utf8mb4_unicode_ci,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_libros`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `moras`
--

DROP TABLE IF EXISTS `moras`;
CREATE TABLE IF NOT EXISTS `moras` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_prestamo` int NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `estado_pago` enum('Pendiente','Pagado') COLLATE utf8mb4_unicode_ci DEFAULT 'Pendiente',
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_prestamo` (`id_prestamo`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `prestamos`
--

DROP TABLE IF EXISTS `prestamos`;
CREATE TABLE IF NOT EXISTS `prestamos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `id_documento` int NOT NULL,
  `fecha_prestamo` date NOT NULL,
  `fecha_devolucion` date NOT NULL,
  `estado` enum('Pendiente','Devuelto','Mora') COLLATE utf8mb4_unicode_ci DEFAULT 'Pendiente',
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_usuario` (`id_usuario`),
  KEY `id_documento` (`id_documento`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipos_documentos`
--

DROP TABLE IF EXISTS `tipos_documentos`;
CREATE TABLE IF NOT EXISTS `tipos_documentos` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_creacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `tipos_documentos`
--

INSERT INTO `tipos_documentos` (`id`, `nombre`, `fecha_creacion`) VALUES
(9, 'LIBROS', '2024-11-20 04:07:09'),
(11, 'CDs', '2024-11-20 04:28:04'),
(12, 'asdasd', '2024-11-20 05:06:13');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE IF NOT EXISTS `usuarios` (
  `id` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rol` enum('Administrador','Profesor','Alumno') COLLATE utf8mb4_unicode_ci NOT NULL,
  `contraseña` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `direccion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `departamento` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `email`, `rol`, `contraseña`, `telefono`, `direccion`, `departamento`, `fecha_nacimiento`, `fecha_registro`) VALUES
('AD00001', 'Administrador General', 'admin@colegio.com', 'Administrador', 'admin123', '555-1234', 'Oficina Central', NULL, NULL, '2024-11-20 01:46:11'),
('AD00002', 'Alex VAldez', 'asd@asd.com', 'Administrador', '12345678', '12345678', 'asdfasdf', 'asdfasdfsadf', '1992-08-28', '2024-11-20 01:55:36'),
('AL00001', 'Alumno Pérez', 'alumno.perez@colegio.com', 'Alumno', 'alumno123', '555-9101', 'Residencia Universitaria', NULL, NULL, '2024-11-20 01:46:11'),
('PR00001', 'Profesor López', 'profesor.lopez@colegio.com', 'Profesor', 'profesor123', '555-5678', 'Facultad de Ciencias', NULL, NULL, '2024-11-20 01:46:11');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
