package com.biblioteca.controller;

import com.biblioteca.dao.FormularioDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FormularioController {
    public FormularioDAO formularioDAO;

    public FormularioController() {
        this.formularioDAO = new FormularioDAO();
    }

    //Verifica si el nombre del formulario ya existe.
    
    public boolean verificarNombreExistente(String nombreTabla) throws SQLException {
        return formularioDAO.verificarNombreExistente(nombreTabla);
    }

    //Crea una nueva tabla y registra el tipo de documento.
    
    public void crearTabla(String nombreTabla, List<String> columnas) throws SQLException {
        formularioDAO.crearTabla(nombreTabla, columnas);
    }

    //Se obtiene todas las tablas registradas.
    
    public List<String> obtenerTablas() throws SQLException {
        return formularioDAO.obtenerTablas();
    }

    //Obtiene las columnas de una tabla específica.
   
    public List<String> obtenerColumnas(String nombreTabla) throws SQLException {
        return formularioDAO.obtenerColumnas(nombreTabla);
    }

    //Actualiza el nombre de una columna existente.
  
    public void actualizarNombreColumna(String nombreTabla, String nombreActual, String nuevoNombre, String tipoDato) throws SQLException {
        formularioDAO.actualizarNombreColumna(nombreTabla, nombreActual, nuevoNombre, tipoDato);
    }

    //Agrega una nueva columna a una tabla existente.
    
    public void eliminarColumna(String nombreTabla, String nombreColumna) throws SQLException {
    String sql = "ALTER TABLE " + nombreTabla + " DROP COLUMN " + nombreColumna;
    try (Connection conn = formularioDAO.getConexion();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.executeUpdate();
    }
    }
   
    public void agregarNuevaColumna(String nombreTabla, String nuevoNombre, String tipoDato) throws SQLException {
        formularioDAO.agregarNuevaColumna(nombreTabla, nuevoNombre, tipoDato);
    }

    // Elimina una tabla y sus registro.
   
    public void eliminarTabla(String nombreTabla) throws SQLException {
        formularioDAO.eliminarTabla(nombreTabla);
    }

    //Obtener los datos de una tabla con un límite de filas.
   
    public ResultSet obtenerDatosTabla(String nombreTabla, int limite) throws SQLException {
        return formularioDAO.obtenerDatosTabla(nombreTabla, limite);
    }
}
