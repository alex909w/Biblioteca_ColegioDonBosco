package com.biblioteca.basedatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBaseDatos {

    // Variables de configuración
    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca_colegio";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Variable estática para el Singleton
    private static Connection conexion;

    // Constructor privado para evitar instanciación directa
    private ConexionBaseDatos() {}

    // Método para obtener la conexión
    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexión establecida con éxito.");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return conexion;
    }

    // Método para cerrar la conexión
    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                conexion = null;
                System.out.println("Conexión cerrada correctamente.");
            } else {
                System.out.println("La conexión ya está cerrada o no se ha establecido.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
