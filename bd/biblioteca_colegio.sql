-- Configuración inicial
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- Base de datos
CREATE DATABASE IF NOT EXISTS `biblioteca_colegio` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `biblioteca_colegio`;

-- --------------------------------------------------------
-- Tabla de configuraciones
-- --------------------------------------------------------
DROP TABLE IF EXISTS `configuraciones`;
CREATE TABLE configuraciones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  clave VARCHAR(191) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
  valor DECIMAL(10,2) NOT NULL DEFAULT '0.00',
  fecha_modificacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO configuraciones (clave, valor) VALUES
('mora_diaria', 1.50),
('limite_prestamos', 3.00);

-- --------------------------------------------------------
-- Tabla de usuarios
-- --------------------------------------------------------
DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE usuarios (
  id VARCHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  nombre VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  email VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
  rol ENUM('Administrador', 'Profesor', 'Alumno') COLLATE utf8mb4_unicode_ci NOT NULL,
  contraseña VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  telefono VARCHAR(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  direccion VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  fecha_nacimiento DATE DEFAULT NULL,
  fecha_registro TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO usuarios (id, nombre, email, rol, contraseña, telefono, direccion, fecha_nacimiento) VALUES
('AD00001', 'Administrador General', 'admin@colegio.com', 'Administrador', 'admin123', '555-1234', 'Oficina Central', '1975-05-20'),
('AL00001', 'Alumno Pérez', 'alumno.perez@colegio.com', 'Alumno', 'alumno123', '555-9101', 'Residencia Universitaria', '2001-08-15'),
('PR00001', 'Profesor López', 'profesor.lopez@colegio.com', 'Profesor', 'profesor123', '555-5678', 'Facultad de Ciencias', '1985-11-10');

-- --------------------------------------------------------
-- Tabla de préstamos
-- --------------------------------------------------------
DROP TABLE IF EXISTS `prestamos`;
CREATE TABLE prestamos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario VARCHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  id_documento VARCHAR(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  dias_prestamo INT NOT NULL,
  fecha_prestamo DATE NOT NULL,
  fecha_devolucion DATE NOT NULL,
  estado ENUM('Pendiente', 'Devuelto', 'Mora') COLLATE utf8mb4_unicode_ci DEFAULT 'Pendiente',
  dias_mora INT DEFAULT NULL,
  monto_mora DECIMAL(10,2) DEFAULT NULL,
  fecha_registro TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_usuario) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Tabla de devoluciones
-- --------------------------------------------------------
DROP TABLE IF EXISTS `devoluciones`;
CREATE TABLE devoluciones (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_prestamo INT NOT NULL,
  id_usuario VARCHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  id_documento VARCHAR(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  fecha_devolucion_real DATE NOT NULL,
  dias_mora INT DEFAULT '0',
  monto_mora DECIMAL(10,2) DEFAULT '0.00',
  FOREIGN KEY (id_prestamo) REFERENCES prestamos (id) ON DELETE CASCADE,
  FOREIGN KEY (id_usuario) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Tabla de auditoría
-- --------------------------------------------------------
DROP TABLE IF EXISTS `auditoria`;
CREATE TABLE auditoria (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  accion VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  descripcion TEXT COLLATE utf8mb4_unicode_ci,
  fecha TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Tabla de tipos de documentos
-- --------------------------------------------------------
DROP TABLE IF EXISTS `tipos_documentos`;
CREATE TABLE tipos_documentos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
  fecha_creacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tipos_documentos (nombre) VALUES ('Libros');

-- --------------------------------------------------------
-- Tabla de historial de préstamos
-- --------------------------------------------------------
DROP TABLE IF EXISTS `historial_prestamos`;
CREATE TABLE historial_prestamos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  id_prestamo INT NOT NULL,
  id_usuario VARCHAR(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  id_documento VARCHAR(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  accion ENUM('Creación', 'Devolución', 'Mora') COLLATE utf8mb4_unicode_ci NOT NULL,
  descripcion TEXT COLLATE utf8mb4_unicode_ci,
  fecha TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_prestamo) REFERENCES prestamos (id) ON DELETE CASCADE,
  FOREIGN KEY (id_usuario) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Tabla de libros
-- --------------------------------------------------------
DROP TABLE IF EXISTS `libros`;
CREATE TABLE libros (
  id_libros VARCHAR(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  `Título del Libro` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Autor(es)` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Categoría` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Fecha de Publicación` DATE DEFAULT NULL,
  `Editorial` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ISBN` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ubicación_física` VARCHAR(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  cantidad_total INT DEFAULT '0',
  cantidad_disponible INT DEFAULT '0',
  estado ENUM('Bueno', 'Dañado', 'En Reparación') COLLATE utf8mb4_unicode_ci DEFAULT 'Bueno',
  palabras_clave TEXT COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (id_libros)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO libros (id_libros, `Título del Libro`, `Autor(es)`, `Categoría`, `Fecha de Publicación`, `Editorial`, `ISBN`, `ubicación_física`, cantidad_total, cantidad_disponible, estado, palabras_clave) VALUES
('LIB00001', 'Introducción a la Programación en Java', 'Herbert Schildt', 'Programación', '2018-09-15', 'McGraw-Hill Education', '978-1260440232', 'Sección A, Estantería 3, Nivel 1', 5, 5, 'Bueno', 'Java, Programación, Orientación a Objetos');

COMMIT;
