package com.biblioteca.controller;

import com.biblioteca.dao.InventarioDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class InventarioController {
    private InventarioDAO inventarioDAO;

    public InventarioController() {
        this.inventarioDAO = new InventarioDAO();
    }

    //Obtiene la lista de formularios.

    public List<String> obtenerFormularios() throws SQLException {
        return inventarioDAO.obtenerFormularios();
    }

    // Obtiene la información de las columnas de una tabla.

    public List<Map<String, String>> obtenerColumnasTabla(String tabla) throws SQLException {
        return inventarioDAO.obtenerColumnasTabla(tabla);
    }

    // Genera un nuevo ID para un registro en la tabla especificada.

    public String generarNuevoId(String tabla, String prefijo) throws SQLException {
        return inventarioDAO.generarNuevoId(tabla, prefijo);
    }

    // Registra datos en una tabla específica.

    public void registrarDatos(String tabla, List<String> columnas, List<Object> valores) throws SQLException {
        inventarioDAO.registrarDatos(tabla, columnas, valores);
    }

    // Obtiene todos los datos de una tabla específica.

    public List<Map<String, Object>> obtenerDatosTabla(String tabla) throws SQLException {
        return inventarioDAO.obtenerDatosTabla(tabla);
    }

    // Actualiza datos en una tabla específica.

    public void actualizarDatos(String tabla, String columnaID, String idValor, Map<String, String> datos) throws SQLException {
        inventarioDAO.actualizarDatos(tabla, columnaID, idValor, datos);
    }

    // Elimina un registro de una tabla específica.

    public void eliminarRegistro(String tabla, String columnaID, String idValor) throws SQLException {
        inventarioDAO.eliminarRegistro(tabla, columnaID, idValor);
    }
}
