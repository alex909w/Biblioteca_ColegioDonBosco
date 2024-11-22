package com.biblioteca.dao;

import com.biblioteca.basedatos.ConexionBaseDatos;
import com.biblioteca.modelos.Prestamo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    // Obtiene los préstamos vigentes de un usuario.
 
    public List<Prestamo> obtenerPrestamosVigentes(String idUsuario) throws SQLException {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = "SELECT id, id_documento, fecha_prestamo, fecha_devolucion, estado, dias_mora, monto_mora " +
                     "FROM prestamos WHERE id_usuario = ? AND estado IN ('Pendiente', 'Mora')";
        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prestamo prestamo = new Prestamo();
                    prestamo.setId(rs.getInt("id"));
                    prestamo.setIdUsuario(idUsuario);
                    prestamo.setIdDocumento(rs.getString("id_documento"));
                    prestamo.setFechaPrestamo(rs.getDate("fecha_prestamo"));
                    prestamo.setFechaDevolucion(rs.getDate("fecha_devolucion"));
                    prestamo.setEstado(rs.getString("estado"));
                    prestamo.setDiasMora(rs.getInt("dias_mora"));
                    prestamo.setMontoMora(rs.getBigDecimal("monto_mora"));
                    prestamos.add(prestamo);
                }
            }
        }
        return prestamos;
    }

    // Registra un nuevo préstamo.

    public void registrarPrestamo(Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO prestamos (id_usuario, id_documento, mora_diaria, fecha_prestamo, fecha_devolucion, estado, fecha_devolucion_programada) " +
                     "VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY), 'Pendiente', DATE_ADD(CURDATE(), INTERVAL ? DAY))";
        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, prestamo.getIdUsuario());
            stmt.setString(2, prestamo.getIdDocumento());
            stmt.setDouble(3, prestamo.getMoraDiaria());
            stmt.setInt(4, prestamo.getDiasMora()); // Usando diasMora como dias de préstamo
            stmt.setInt(5, prestamo.getDiasMora());
            stmt.executeUpdate();
        }
    }

    // Valida si el usuario ha alcanzado el límite de préstamos permitidos.

    public boolean validarLimitePrestamos(String idUsuario, int limite) throws SQLException {
        String sql = "SELECT COUNT(*) AS prestamos_activos FROM prestamos WHERE id_usuario = ? AND estado = 'Pendiente'";
        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int prestamosActivos = rs.getInt("prestamos_activos");
                    return prestamosActivos < limite;
                }
            }
        }
        return false;
    }

    // Obtiene la mora diaria configurada para un rol específico.

    public double obtenerMoraDiariaPorRol(String rolUsuario) throws SQLException {
        String sql = "SELECT valor FROM configuraciones WHERE clave = ?";
        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, "mora_" + rolUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("valor");
                }
            }
        }
        return 1.50; // Valor por defecto
    }

    // Actualiza la disponibilidad de un documento.

    public void actualizarDisponibilidadDocumento(String tipoDocumento, String idDocumento, int cantidadCambio) throws SQLException {
        String sql = "UPDATE " + tipoDocumento + " SET cantidad_disponible = cantidad_disponible + ? WHERE id_libros = ?";
        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, cantidadCambio);
            stmt.setString(2, idDocumento);
            stmt.executeUpdate();
        }
    }

    // Obtiene el límite de préstamos configurado para un rol específico.

    public int obtenerLimitePrestamosPorRol(String rolUsuario) throws SQLException {
        String sql = "SELECT valor FROM configuraciones WHERE clave = ?";
        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, "limite_prestamos_" + rolUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("valor");
                }
            }
        }
        return 3; // Valor por defecto
    }

    //Registra la devolución de un préstamo.

    public void registrarDevolucion(Prestamo prestamo) throws SQLException {
        String sqlDevolucion = "INSERT INTO devoluciones (id_prestamo, id_usuario, id_documento, fecha_devolucion_real, dias_mora, monto_mora) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlHistorial = "INSERT INTO historial_prestamos (id_prestamo, id_usuario, id_documento, accion, descripcion) VALUES (?, ?, ?, 'Devolución', ?)";
        String sqlActualizarPrestamo = "UPDATE prestamos SET estado = 'Devuelto', dias_mora = ?, monto_mora = ? WHERE id = ?";
        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            conexion.setAutoCommit(false);
            try (PreparedStatement stmtDevolucion = conexion.prepareStatement(sqlDevolucion);
                 PreparedStatement stmtHistorial = conexion.prepareStatement(sqlHistorial);
                 PreparedStatement stmtActualizarPrestamo = conexion.prepareStatement(sqlActualizarPrestamo)) {

                // Insertar en devoluciones
                stmtDevolucion.setInt(1, prestamo.getId());
                stmtDevolucion.setString(2, prestamo.getIdUsuario());
                stmtDevolucion.setString(3, prestamo.getIdDocumento());
                stmtDevolucion.setDate(4, prestamo.getFechaDevolucion());
                stmtDevolucion.setInt(5, prestamo.getDiasMora());
                stmtDevolucion.setBigDecimal(6, prestamo.getMontoMora());
                stmtDevolucion.executeUpdate();

                // Insertar en historial
                stmtHistorial.setInt(1, prestamo.getId());
                stmtHistorial.setString(2, prestamo.getIdUsuario());
                stmtHistorial.setString(3, prestamo.getIdDocumento());
                stmtHistorial.setString(4, "Artículo devuelto correctamente.");
                stmtHistorial.executeUpdate();

                // Actualizar préstamo
                stmtActualizarPrestamo.setInt(1, prestamo.getDiasMora());
                stmtActualizarPrestamo.setBigDecimal(2, prestamo.getMontoMora());
                stmtActualizarPrestamo.setInt(3, prestamo.getId());
                stmtActualizarPrestamo.executeUpdate();

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    // Actualiza el estado de un préstamo a 'Mora'.

    public void actualizarEstadoMora(Prestamo prestamo) throws SQLException {
        String sql = "UPDATE prestamos SET estado = 'Mora', dias_mora = ?, monto_mora = ? WHERE id = ?";
        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, prestamo.getDiasMora());
            stmt.setBigDecimal(2, prestamo.getMontoMora());
            stmt.setInt(3, prestamo.getId());
            stmt.executeUpdate();
        }
    }

    // Registra el pago de mora de un préstamo.

    public void registrarPagoMora(int idPrestamo, double montoMora) throws SQLException {
        String sqlPagoMora = "INSERT INTO pagos_mora (id_prestamo, monto_pagado, fecha_pago) VALUES (?, ?, NOW())";
        String sqlActualizarPrestamo = "UPDATE prestamos SET estado = 'Devuelto', dias_mora = 0, monto_mora = 0 WHERE id = ?";
        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            conexion.setAutoCommit(false);
            try (PreparedStatement stmtPagoMora = conexion.prepareStatement(sqlPagoMora);
                 PreparedStatement stmtActualizarPrestamo = conexion.prepareStatement(sqlActualizarPrestamo)) {

                // Insertar en pagos_mora
                stmtPagoMora.setInt(1, idPrestamo);
                stmtPagoMora.setDouble(2, montoMora);
                stmtPagoMora.executeUpdate();

                // Actualizar préstamo
                stmtActualizarPrestamo.setInt(1, idPrestamo);
                stmtActualizarPrestamo.executeUpdate();

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    // Obtiene los detalles de un préstamo por ID.

    public Prestamo obtenerPrestamoPorId(int idPrestamo) throws SQLException {
        String sql = "SELECT * FROM prestamos WHERE id = ?";
        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idPrestamo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Prestamo prestamo = new Prestamo();
                    prestamo.setId(rs.getInt("id"));
                    prestamo.setIdUsuario(rs.getString("id_usuario"));
                    prestamo.setIdDocumento(rs.getString("id_documento"));
                    prestamo.setFechaPrestamo(rs.getDate("fecha_prestamo"));
                    prestamo.setFechaDevolucion(rs.getDate("fecha_devolucion"));
                    prestamo.setFechaDevolucionProgramada(rs.getDate("fecha_devolucion_programada"));
                    prestamo.setEstado(rs.getString("estado"));
                    prestamo.setDiasMora(rs.getInt("dias_mora"));
                    prestamo.setMontoMora(rs.getBigDecimal("monto_mora"));
                    return prestamo;
                }
            }
        }
        return null;
    }
}
