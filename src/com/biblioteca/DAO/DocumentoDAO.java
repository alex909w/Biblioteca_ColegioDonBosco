package com.biblioteca.dao;

import com.biblioteca.basedatos.ConexionBaseDatos;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentoDAO {
    
    // Busca documentos en la vista unificada que coincidan con el criterio dado.
     
    public List<Map<String, Object>> buscarEnVistaUnificada(String criterio) throws SQLException {
        List<Map<String, Object>> resultados = new ArrayList<>();
        String sql = "SELECT tipo, contenido FROM vista_documentos WHERE contenido LIKE ?";

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + criterio + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("tipo", rs.getString("tipo"));
                    fila.put("contenido", rs.getString("contenido"));
                    resultados.add(fila);
                }
            }
        }

        return resultados;
    }
    
    //Obtiene los nombres de las tablas desde la tabla 'tipos_documentos'.
   
     public List<String> obtenerNombresTablas() throws SQLException {
        List<String> tablas = new ArrayList<>();
        String sql = "SELECT nombre FROM tipos_documentos";

        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tablas.add(rs.getString("nombre"));
            }
        }

        return tablas;
    }

    // Obtiene las columnas de una tabla específica.
     
   public List<String> obtenerColumnasTabla(String tabla) throws SQLException {
        List<String> columnas = new ArrayList<>();
        String sql = "DESCRIBE `" + tabla + "`"; // Escapar el nombre de la tabla

        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                columnas.add(rs.getString("Field"));
            }
        }

        return columnas;
    }


    // Busca documentos en una tabla específica que coincidan con el título dado.
     
   public List<Map<String, Object>> buscarEnTabla(String tabla, String criterio) throws SQLException {
        List<Map<String, Object>> resultados = new ArrayList<>();
        List<String> columnas = obtenerColumnasTabla(tabla);

        if (columnas.isEmpty()) {
            return resultados; // No hay columnas para buscar
        }

        // Construir la cláusula WHERE dinámica para buscar en todas las columnas
        StringBuilder whereClause = new StringBuilder();
        for (String columna : columnas) {
            if (whereClause.length() > 0) {
                whereClause.append(" OR ");
            }
            // Escapar el nombre de la columna
            whereClause.append("`").append(columna).append("` LIKE ?");
        }

        String sql = "SELECT * FROM `" + tabla + "` WHERE " + whereClause.toString();

        // Registrar la consulta SQL para depuración
        System.out.println("Consulta SQL: " + sql);

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Establecer los parámetros de búsqueda
            for (int i = 1; i <= columnas.size(); i++) {
                stmt.setString(i, "%" + criterio + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        fila.put(metaData.getColumnLabel(i), rs.getObject(i));
                    }
                    resultados.add(fila);
                }
            }
        } catch (SQLException ex) {
            // Manejar excepciones específicas de columnas inexistentes
            if (ex.getMessage().contains("does not exist")) {
                System.err.println("La tabla '" + tabla + "' no contiene una columna específica: " + ex.getMessage());
            } else {
                throw ex; // Re-lanzar otras excepciones
            }
        }

        return resultados;
    }


    // Busca documentos en todas las tablas que coincidan con el título dado.
     
     public List<Map<String, Object>> buscarEnTodasLasTablas(String criterio) throws SQLException {
        List<Map<String, Object>> resultadosTotales = new ArrayList<>();
        List<String> tablas = obtenerNombresTablas();

        for (String tabla : tablas) {
            resultadosTotales.addAll(buscarEnTabla(tabla, criterio));
        }

        return resultadosTotales;
    }

    public List<Map<String, Object>> obtenerTodosLosDocumentos(String tipoDocumento) throws SQLException {
    // Validar que tipoDocumento es una tabla válida
    List<String> tablasValidas = obtenerNombresTablas();
    if (!tablasValidas.contains(tipoDocumento)) {
        throw new SQLException("Tipo de documento no válido.");
    }

    List<Map<String, Object>> documentos = new ArrayList<>();
    String sql = "SELECT * FROM " + tipoDocumento;
    try (Connection conn = ConexionBaseDatos.getConexion();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            documentos.add(row);
        }
    }
    return documentos;
}
    
    public String obtenerColumnaCantidad(String tabla) throws SQLException {
        String sql = "DESCRIBE " + tabla;
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String columna = rs.getString("Field");
                if (columna.toLowerCase().contains("cantidad")) {
                    return columna;
                }
            }
        }
        throw new SQLException("No se encontró una columna de cantidad en la tabla " + tabla);
    }
    
    public List<String> obtenerTablasDinamicas() throws SQLException {
    List<String> tablas = new ArrayList<>();
    String sql = "SELECT nombre FROM tipos_documentos";
    try (Connection conexion = ConexionBaseDatos.getConexion();
         PreparedStatement stmt = conexion.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            tablas.add(rs.getString("nombre"));
        }
    }
    return tablas;
}

public void registrarDevolucionDinamica(String tabla, String idDocumento, String idUsuario, Date fechaDevolucion) throws SQLException {
    String sql = "INSERT INTO " + tabla + " (id_documento, id_usuario, fecha_devolucion) VALUES (?, ?, ?)";
    try (Connection conexion = ConexionBaseDatos.getConexion();
         PreparedStatement stmt = conexion.prepareStatement(sql)) {
        stmt.setString(1, idDocumento);
        stmt.setString(2, idUsuario);
        stmt.setDate(3, fechaDevolucion);
        stmt.executeUpdate();
    }
}




}
