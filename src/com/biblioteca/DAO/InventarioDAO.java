package com.biblioteca.dao;

import com.biblioteca.basedatos.ConexionBaseDatos;

import java.sql.*;
import java.util.*;

public class InventarioDAO {

    // Obtiene la lista de formularios desde 'tipos_documentos'.

    public List<String> obtenerFormularios() throws SQLException {
        List<String> formularios = new ArrayList<>();
        String sql = "SELECT nombre FROM tipos_documentos";

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                formularios.add(rs.getString("nombre"));
            }
        }
        return formularios;
    }

    // Obtiene la información de las columnas de una tabla específica.

    public List<Map<String, String>> obtenerColumnasTabla(String tabla) throws SQLException {
        List<Map<String, String>> columnas = new ArrayList<>();
        String sql = "DESCRIBE `" + tabla + "`";

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, String> columna = new HashMap<>();
                columna.put("Field", rs.getString("Field"));
                columna.put("Type", rs.getString("Type"));
                columna.put("Key", rs.getString("Key"));
                columnas.add(columna);
            }
        }
        return columnas;
    }

    // Genera un nuevo ID basado en el prefijo y el máximo ID existente.
    
    public String generarNuevoId(String tabla, String prefijo) throws SQLException {
        String nuevoId = prefijo + "0001";
        String columnaId = "id_" + tabla.toLowerCase();
        String sql = "SELECT MAX(CAST(SUBSTRING(" + columnaId + ", LENGTH(?) + 1) AS UNSIGNED)) AS max_id FROM " + tabla;

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prefijo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("max_id") > 0) {
                    int maxId = rs.getInt("max_id");
                    nuevoId = prefijo + String.format("%04d", maxId + 1);
                }
            }
        }
        return nuevoId;
    }

    //Registra datos en una tabla específica.

    public void registrarDatos(String tabla, List<String> columnas, List<Object> valores) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO `" + tabla + "` (");
        StringBuilder placeholders = new StringBuilder(" VALUES (");

        for (String columna : columnas) {
            sql.append("`").append(columna).append("`, ");
            placeholders.append("?, ");
        }

        sql.setLength(sql.length() - 2);
        placeholders.setLength(placeholders.length() - 2);
        sql.append(")");
        placeholders.append(")");
        sql.append(placeholders);

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < valores.size(); i++) {
                Object valor = valores.get(i);
                if (valor instanceof java.sql.Date) {
                    ps.setDate(i + 1, (java.sql.Date) valor);
                } else {
                    ps.setString(i + 1, valor.toString());
                }
            }
            ps.executeUpdate();
        }
    }

    // Obtiene todos los datos de una tabla específica.

    public List<Map<String, Object>> obtenerDatosTabla(String tablaSeleccionada) throws SQLException {
        List<Map<String, Object>> datos = new ArrayList<>();
        String sql = "SELECT * FROM `" + tablaSeleccionada + "`";

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnas = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                for (int i = 1; i <= columnas; i++) {
                    String nombreColumna = metaData.getColumnName(i);
                    fila.put(nombreColumna, rs.getObject(i));
                }
                datos.add(fila);
            }
        }
        return datos;
    }

    // Actualiza datos en una tabla específica.

    public void actualizarDatos(String tabla, String columnaID, String idValor, Map<String, String> datos) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE `" + tabla + "` SET ");
        for (String columna : datos.keySet()) {
            sql.append("`").append(columna).append("` = ?, ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE `").append(columnaID).append("` = ?");

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            for (String valor : datos.values()) {
                ps.setString(index++, valor);
            }
            ps.setString(index, idValor);
            ps.executeUpdate();
        }
    }

    // Elimina un registro de una tabla específica.

    public void eliminarRegistro(String tabla, String columnaID, String idValor) throws SQLException {
        String sql = "DELETE FROM `" + tabla + "` WHERE `" + columnaID + "` = ?";

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idValor);
            ps.executeUpdate();
        }
    }
    
    
}
