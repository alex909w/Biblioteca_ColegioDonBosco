package com.biblioteca.base_datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBaseDatos {

    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca_colegiodonbosco";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection conexion;

    private ConexionBaseDatos() {}

    public static Connection getConexion() {
        try {
            // Verifica si la conexión es nula o está cerrada
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexión exitosa a la base de datos.");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
            throw new RuntimeException("Error al conectar con la base de datos.", e);
        }
        return conexion;
    }
}
