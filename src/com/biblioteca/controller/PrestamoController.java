package com.biblioteca.controller;

import com.biblioteca.basedatos.ConexionBaseDatos;
import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.modelos.Prestamo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.List;

public class PrestamoController {
    private PrestamoDAO prestamoDAO;

    public PrestamoController() {
        this.prestamoDAO = new PrestamoDAO();
    }

    //Obtiene los préstamos vigentes de un usuario.

    public List<Prestamo> obtenerPrestamosVigentes(String idUsuario) throws SQLException {
        return prestamoDAO.obtenerPrestamosVigentes(idUsuario);
    }

    //Registra un nuevo préstamo.
   
    public void registrarPrestamo(Prestamo prestamo) throws SQLException {
        prestamoDAO.registrarPrestamo(prestamo);
    }

    // Valida si el usuario ha alcanzado el límite de préstamos permitidos.

    public boolean validarLimitePrestamos(String idUsuario, int limite) throws SQLException {
        return prestamoDAO.validarLimitePrestamos(idUsuario, limite);
    }

    // Obtiene la mora diaria configurada para un rol específico.

    public double obtenerMoraDiariaPorRol(String rolUsuario) throws SQLException {
        return prestamoDAO.obtenerMoraDiariaPorRol(rolUsuario);
    }

    // Actualiza la disponibilidad de un documento.

    public void actualizarDisponibilidadDocumento(String tipoDocumento, String idDocumento, int cantidadCambio) throws SQLException {
        prestamoDAO.actualizarDisponibilidadDocumento(tipoDocumento, idDocumento, cantidadCambio);
    }

    // Obtiene el límite de préstamos configurado para un rol específico.
 
    public int obtenerLimitePrestamosPorRol(String rolUsuario) throws SQLException {
    // Consulta SQL para obtener el límite de préstamos desde la tabla configuraciones
    String sql = "SELECT valor FROM configuraciones WHERE clave = ?";
    PreparedStatement stmt = ConexionBaseDatos.getConexion().prepareStatement(sql);

    // Ajustar la clave dependiendo del rol
    String claveLimite = "limite_prestamos_" + rolUsuario.toLowerCase();
    stmt.setString(1, claveLimite);

    ResultSet rs = stmt.executeQuery();

    // Verificar si se obtiene un valor para el límite de préstamos
    if (rs.next()) {
        return rs.getInt("valor");
    } else {
        // En caso de que no se encuentre el límite, se puede retornar un valor predeterminado o lanzar una excepción
        throw new SQLException("No se encontró el límite de préstamos para el rol: " + rolUsuario);
    }
}


    //Registra la devolución de un préstamo.
    
    public void registrarDevolucion(Prestamo prestamo) throws SQLException {
        prestamoDAO.registrarDevolucion(prestamo);
    }

    // Actualiza el estado de un préstamo a 'Mora'.
    
    public void actualizarEstadoMora(Prestamo prestamo) throws SQLException {
        prestamoDAO.actualizarEstadoMora(prestamo);
    }

    // Registra el pago de mora de un préstamo.

    public void registrarPagoMora(int idPrestamo, double montoMora) throws SQLException {
        prestamoDAO.registrarPagoMora(idPrestamo, montoMora);
    }

    //Obtiene los detalles de un préstamo por ID.

    public Prestamo obtenerPrestamoPorId(int idPrestamo) throws SQLException {
        return prestamoDAO.obtenerPrestamoPorId(idPrestamo);
    }
}
