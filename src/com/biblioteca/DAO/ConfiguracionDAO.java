package com.biblioteca.dao;

import com.biblioteca.base_datos.ConexionBaseDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ConfiguracionDAO {

    //Obtiene las configuraciones para un rol específico.
   
    public Map<String, Double> obtenerConfiguracionPorRol(String rol) throws SQLException {
        Map<String, Double> configuraciones = new HashMap<>();
        String query = "SELECT clave, valor FROM configuraciones WHERE clave IN (?, ?, ?)";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "mora_" + rol);
            stmt.setString(2, "limite_prestamos_" + rol);
            stmt.setString(3, "limite_dias_" + rol);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String clave = rs.getString("clave");
                    Double valor = rs.getDouble("valor");
                    configuraciones.put(clave, valor);
                }
            }
        }
        return configuraciones;
    }

    // Actualiza las configuraciones para un rol específico.
 
    public void actualizarConfiguracionPorRol(String rol, Map<String, Double> valores) throws SQLException {
        String updateQuery = "UPDATE configuraciones SET valor = ? WHERE clave = ?";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            for (Map.Entry<String, Double> entry : valores.entrySet()) {
                stmt.setDouble(1, entry.getValue());
                stmt.setString(2, entry.getKey());
                stmt.executeUpdate();
            }
        }
    }
}
