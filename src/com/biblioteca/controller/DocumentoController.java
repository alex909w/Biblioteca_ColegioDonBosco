package com.biblioteca.controller;

import com.biblioteca.basedatos.ConexionBaseDatos;
import com.biblioteca.dao.DocumentoDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DocumentoController {

    private final DocumentoDAO documentoDAO;
    
        public DocumentoController() {
        this.documentoDAO = new DocumentoDAO();
    }
        
   public List<Map<String, Object>> buscarEnVistaUnificada(String criterio) throws SQLException {
        return documentoDAO.buscarEnTodasLasTablas(criterio);
    }
   
  public List<Map<String, Object>> buscarPorCriterioEnTodasLasTablas(String criterio) throws SQLException {
        List<Map<String, Object>> resultadosTotales = new ArrayList<>();
        List<String> tablas = documentoDAO.obtenerNombresTablas();

        for (String tabla : tablas) {
            List<Map<String, Object>> resultados = documentoDAO.buscarEnTabla(tabla, criterio);
            for (Map<String, Object> fila : resultados) {
                // Añade el nombre de la tabla al mapa de resultados
                fila.put("Tipo de Documento", tabla);
                resultadosTotales.add(fila);
            }
        }

        return resultadosTotales;
    }

  public List<Map<String, Object>> buscarPorCriterioEnTabla(String tabla, String criterio) throws SQLException {
        List<Map<String, Object>> resultados = documentoDAO.buscarEnTabla(tabla, criterio);
        for (Map<String, Object> fila : resultados) {
            // Opcional: Añade información adicional si es necesario
            fila.put("Tipo de Documento", tabla);
        }
        return resultados;
    }
  
  public List<Map<String, Object>> buscarPorCriterioOrdenado(String tabla, String criterio) throws SQLException {
    List<Map<String, Object>> resultados = documentoDAO.buscarEnTabla(tabla, criterio);
    List<String> columnasOrdenadas = documentoDAO.obtenerColumnasOrdenadas(tabla);

    // Reordenar los mapas según el orden de las columnas
    List<Map<String, Object>> resultadosOrdenados = new ArrayList<>();
    for (Map<String, Object> fila : resultados) {
        Map<String, Object> filaOrdenada = new LinkedHashMap<>();
        for (String columna : columnasOrdenadas) {
            filaOrdenada.put(columna, fila.getOrDefault(columna, null));
        }
        resultadosOrdenados.add(filaOrdenada);
    }

    return resultadosOrdenados;
}

  
  public List<String> obtenerNombresTablas() throws SQLException {
        return documentoDAO.obtenerNombresTablas();
    }

    // Busca documentos en todas las tablas que coincidan con el criterio dado.
    public List<Map<String, Object>> buscarEnTodasLasTablas(String criterio) throws SQLException {
        List<Map<String, Object>> resultados = new ArrayList<>();

        // Consulta para obtener los nombres de las tablas dinámicas desde `tipos_documentos`
        String sqlObtenerTablas = "SELECT nombre FROM tipos_documentos";

        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmtTablas = conexion.prepareStatement(sqlObtenerTablas);
             ResultSet rsTablas = stmtTablas.executeQuery()) {

            while (rsTablas.next()) {
                String nombreTabla = rsTablas.getString("nombre");

                // Realizar la búsqueda en los datos de cada tabla
                resultados.addAll(buscarEnDatosDeTabla(nombreTabla, criterio, conexion));
            }
        }

        return resultados;
    }

    // Busca en los datos de una tabla dinámica
    private List<Map<String, Object>> buscarEnDatosDeTabla(String nombreTabla, String criterio, Connection conexion) throws SQLException {
        List<Map<String, Object>> resultados = new ArrayList<>();

        // Obtener todas las columnas de la tabla
        List<String> columnas = obtenerColumnasDeTabla(nombreTabla, conexion);

        if (columnas.isEmpty()) {
            return resultados; // Si no hay columnas, no hay nada que buscar
        }

        // Construir la cláusula WHERE dinámica para buscar en todas las columnas
        StringBuilder whereClause = new StringBuilder();
        for (String columna : columnas) {
            if (whereClause.length() > 0) {
                whereClause.append(" OR ");
            }
            whereClause.append(columna).append(" LIKE ?");
        }

        String sql = "SELECT * FROM " + nombreTabla + " WHERE " + whereClause;

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            // Establecer el parámetro de búsqueda para todas las columnas
            for (int i = 1; i <= columnas.size(); i++) {
                stmt.setString(i, "%" + criterio + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        fila.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    resultados.add(fila);
                }
            }
        }

        return resultados;
    }

    // Obtiene las columnas de una tabla
    private List<String> obtenerColumnasDeTabla(String nombreTabla, Connection conexion) throws SQLException {
        List<String> columnas = new ArrayList<>();
        String sqlDescribe = "DESCRIBE " + nombreTabla;

        try (PreparedStatement stmtDescribe = conexion.prepareStatement(sqlDescribe);
             ResultSet rsDescribe = stmtDescribe.executeQuery()) {

            while (rsDescribe.next()) {
                columnas.add(rsDescribe.getString("Field"));
            }
        }

        return columnas;
    }
    
   
}
