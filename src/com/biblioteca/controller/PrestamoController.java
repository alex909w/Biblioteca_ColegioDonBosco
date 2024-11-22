package com.biblioteca.controller;

import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.modelos.Prestamo;

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
        return prestamoDAO.obtenerLimitePrestamosPorRol(rolUsuario);
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
