package com.biblioteca.dao;

import com.biblioteca.basedatos.ConexionBaseDatos;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FormularioDAO {
    
    public Connection getConexion() throws SQLException {
        return ConexionBaseDatos.getConexion();
    }
    // Verifica si el nombre del formulario ya existe en tipos_documentos.
   
    public boolean verificarNombreExistente(String nombreTabla) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM tipos_documentos WHERE nombre = ?";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreTabla);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
            return false;
        }
    }

    // Crea una nueva tabla en la base de datos y registra el tipo de documento.
   
    public void crearTabla(String nombreTabla, List<String> columnas) throws SQLException {
        StringBuilder sql = new StringBuilder("CREATE TABLE `").append(nombreTabla).append("` (");
        String idColumna = "id_" + nombreTabla.toLowerCase();
        sql.append("`").append(idColumna).append("` VARCHAR(15) PRIMARY KEY, ");

        for (String columna : columnas) {
            String nombreColumnaDB = sanitizeName(columna);
            if (columna.toLowerCase().contains("fecha")) {
                sql.append("`").append(nombreColumnaDB).append("` DATE, ");
            } else {
                sql.append("`").append(nombreColumnaDB).append("` VARCHAR(255), ");
            }
        }

        sql.append("`ubicacion_fisica` VARCHAR(255), ");
        sql.append("`cantidad_disponible` INT DEFAULT 0, ");
        sql.append("`cantidad_total` INT DEFAULT 0, ");
        sql.append("`estado` ENUM('Bueno', 'Dañado', 'En Reparación') DEFAULT 'Bueno', ");
        sql.append("`palabras_clave` TEXT);");

        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql.toString());
        }

        String registrarTipoSQL = "INSERT INTO tipos_documentos (nombre, fecha_creacion) VALUES (?, NOW())";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement ps = conn.prepareStatement(registrarTipoSQL)) {
            ps.setString(1, nombreTabla);
            ps.executeUpdate();
        }
    }

    //Obtiene una lista de todas las tablas registradas en tipos_documentos.
     
    public List<String> obtenerTablas() throws SQLException {
        List<String> tablas = new ArrayList<>();
        String sql = "SELECT nombre FROM tipos_documentos";
        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tablas.add(rs.getString("nombre"));
            }
        }
        return tablas;
    }

    // Obtiene las columnas de una tabla específica, excluyendo columnas predeterminadas.
    
    public List<String> obtenerColumnas(String nombreTabla) throws SQLException {
        List<String> columnas = new ArrayList<>();
        String idColumna = "id_" + nombreTabla.toLowerCase();
        String sql = "DESCRIBE `" + nombreTabla + "`";
        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String campo = rs.getString("Field").trim();
                if (!esColumnaPredeterminada(campo, idColumna)) {
                    columnas.add(campo);
                }
            }
        }
        return columnas;
    }

    // Actualiza el nombre y tipo de una columna existente.
  
    public void actualizarNombreColumna(String nombreTabla, String nombreActual, String nuevoNombre, String tipoDato) throws SQLException {
        String sql = "ALTER TABLE `" + nombreTabla + "` CHANGE `" + nombreActual + "` `" + nuevoNombre + "` " + tipoDato;
        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    //Agrega una nueva columna a una tabla existente.
     
    public void agregarNuevaColumna(String nombreTabla, String nuevoNombre, String tipoDato) throws SQLException {
        String sql = "ALTER TABLE `" + nombreTabla + "` ADD `" + nuevoNombre + "` " + tipoDato;
        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    //Elimina una tabla de la base de datos y su registro en tipos_documentos.
    
    public void eliminarTabla(String nombreTabla) throws SQLException {
        String sqlDropTable = "DROP TABLE " + nombreTabla;
        String sqlDeleteTipo = "DELETE FROM tipos_documentos WHERE nombre = ?";

        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlDropTable);
        }

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement ps = conn.prepareStatement(sqlDeleteTipo)) {
            ps.setString(1, nombreTabla);
            ps.executeUpdate();
        }
    }

    // Obtiene los datos de una tabla limitada a un número específico de filas.
    
    public ResultSet obtenerDatosTabla(String nombreTabla, int limite) throws SQLException {
        String sql = "SELECT * FROM " + nombreTabla + " LIMIT " + limite;
        Connection conn = ConexionBaseDatos.getConexion();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // Reemplazando espacios con guiones bajos.
   
    private String sanitizeName(String name) {
        return name.trim().replaceAll(" +", "_");
    }

    // Verifica si una columna es predeterminada.
   
    private boolean esColumnaPredeterminada(String campo, String idColumna) {
        List<String> columnasExcluidas = Arrays.asList(
            "fecha_registro",
            "ubicacion_fisica",
            "cantidad_total",
            "cantidad_disponible",
            "estado",
            "palabras_clave"
        );
        return columnasExcluidas.contains(campo.toLowerCase()) || campo.equalsIgnoreCase(idColumna);
}

}
