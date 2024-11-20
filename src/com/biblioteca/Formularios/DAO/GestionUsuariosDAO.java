package com.biblioteca.Formularios.DAO;

import com.biblioteca.base_datos.ConexionBaseDatos;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestionUsuariosDAO {

    private boolean esRolValido(String rol) {
        return rol.equals("Administrador") || rol.equals("Profesor") || rol.equals("Alumno");
    }

    public boolean actualizarUsuario(String idUsuario, String nombre, String email, String telefono, String direccion, String rol) {
    String query = "UPDATE usuarios SET nombre = ?, email = ?, telefono = ?, direccion = ?, rol = ? WHERE id_usuario = ?";
    Connection conexion = null;

    try {
        conexion = ConexionBaseDatos.getConexion();
        if (conexion == null || conexion.isClosed()) {
            throw new SQLException("Conexión cerrada o no disponible.");
        }

        try (PreparedStatement statement = conexion.prepareStatement(query)) {
            statement.setString(1, nombre);
            statement.setString(2, email);
            statement.setString(3, telefono);
            statement.setString(4, direccion);
            statement.setString(5, rol);
            statement.setString(6, idUsuario);

            return statement.executeUpdate() > 0;
        }
    } catch (SQLException e) {
        System.err.println("Error al actualizar el usuario: " + e.getMessage());
        return false;
    } finally {
        cerrarConexion(conexion);
    }
}

    private void cerrarConexion(Connection conexion) {
    if (conexion != null) {
        try {
            if (!conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}

   public boolean registrarUsuario(String nombre, String email, String contraseña, String rol,
                                String telefono, String direccion, String preguntaSeguridad, String respuestaSeguridad) {
    String idUsuario = generarIdUsuario(rol); // Generar el ID basado en la categoría
    if (idUsuario == null) {
        System.err.println("No se pudo generar el ID para el usuario.");
        return false;
    }

    String query = "INSERT INTO usuarios (id_usuario, nombre, email, contraseña, rol, telefono, direccion, pregunta_seguridad, respuesta_seguridad) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    Connection conexion = null;
    try {
        conexion = ConexionBaseDatos.getConexion();
        if (conexion == null || conexion.isClosed()) {
            throw new SQLException("Conexión cerrada o no disponible.");
        }

        try (PreparedStatement statement = conexion.prepareStatement(query)) {
            statement.setString(1, idUsuario);
            statement.setString(2, nombre);
            statement.setString(3, email);
            statement.setString(4, contraseña);
            statement.setString(5, rol);
            statement.setString(6, telefono);
            statement.setString(7, direccion);
            statement.setString(8, preguntaSeguridad);
            statement.setString(9, respuestaSeguridad);

            return statement.executeUpdate() > 0;
        }
    } catch (SQLException e) {
        System.err.println("Error al registrar el usuario: " + e.getMessage());
        return false;
    } finally {
        cerrarConexion(conexion);
    }
}


    public boolean editarUsuario(String idUsuario, String nuevoNombre, String nuevoTelefono, String nuevaDireccion, String nuevoRol) {
    String query = "UPDATE usuarios SET nombre = ?, telefono = ?, direccion = ?, rol = ? WHERE id_usuario = ?";

    try (Connection conexion = ConexionBaseDatos.getConexion();
         PreparedStatement statement = conexion.prepareStatement(query)) {

        statement.setString(1, nuevoNombre);
        statement.setString(2, nuevoTelefono);
        statement.setString(3, nuevaDireccion);
        statement.setString(4, nuevoRol);
        statement.setString(5, idUsuario);

        return statement.executeUpdate() > 0;

    } catch (SQLException e) {
        System.err.println("Error al editar el usuario: " + e.getMessage());
        return false;
    }
}

    public boolean eliminarUsuario(String idUsuario) {
    String query = "DELETE FROM usuarios WHERE id_usuario = ?";

    try (Connection conexion = ConexionBaseDatos.getConexion();
         PreparedStatement statement = conexion.prepareStatement(query)) {

        statement.setString(1, idUsuario);

        return statement.executeUpdate() > 0;

    } catch (SQLException e) {
        System.err.println("Error al eliminar el usuario: " + e.getMessage());
        return false;
    }
}


    public boolean restablecerContraseña(String email, String nuevaContraseña) throws SQLException {
        String query = "UPDATE usuarios SET contraseña = ? WHERE email = ?";

        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            if (!existeEmail(email, conexion)) {
                throw new SQLException("El email no está registrado: " + email);
            }

            String hashedPassword = BCrypt.hashpw(nuevaContraseña, BCrypt.gensalt());

            try (PreparedStatement statement = conexion.prepareStatement(query)) {
                statement.setString(1, hashedPassword);
                statement.setString(2, email);

                boolean actualizado = statement.executeUpdate() > 0;

                if (actualizado) {
                    registrarLog("Restablecer Contraseña", email, conexion);
                }

                return actualizado;
            }
        }
    }

    public boolean esEmailUnico(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement statement = conexion.prepareStatement(query)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) == 0;
                }
            }

            return false;
        }
    }

    public boolean existeEmail(String email, Connection conexion) throws SQLException {
    String query = "SELECT 1 FROM usuarios WHERE email = ? LIMIT 1";

    try (PreparedStatement statement = conexion.prepareStatement(query)) {
        statement.setString(1, email);

        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next(); // Retorna true si encuentra el email
        }
    }
}

    public void registrarLog(String accion, String usuarioAfectado, Connection conexion) throws SQLException {
        String query = "INSERT INTO logs (accion, usuario_afectado) VALUES (?, ?)";

        try (PreparedStatement statement = conexion.prepareStatement(query)) {
            statement.setString(1, accion);
            statement.setString(2, usuarioAfectado);
            statement.executeUpdate();
        }
    }

    public List<String[]> buscarUsuarios(String criterio) throws SQLException {
        String query = "SELECT id_usuario, nombre, email, rol, fecha_registro FROM usuarios " +
                       "WHERE nombre LIKE ? OR email LIKE ?";
        List<String[]> usuarios = new ArrayList<>();

        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement statement = conexion.prepareStatement(query)) {

            statement.setString(1, "%" + criterio + "%");
            statement.setString(2, "%" + criterio + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    usuarios.add(new String[]{
                            resultSet.getString("id_usuario"),
                            resultSet.getString("nombre"),
                            resultSet.getString("email"),
                            resultSet.getString("rol"),
                            resultSet.getString("fecha_registro")
                    });
                }
            }
        }

        return usuarios;
    }

    public List<String[]> obtenerUsuarios() throws SQLException {
    String query = "SELECT id_usuario, nombre, email, telefono, direccion, rol, fecha_registro FROM usuarios";
    List<String[]> usuarios = new ArrayList<>();

    try (Connection conexion = ConexionBaseDatos.getConexion();
         PreparedStatement statement = conexion.prepareStatement(query);
         ResultSet resultSet = statement.executeQuery()) {

        while (resultSet.next()) {
            usuarios.add(new String[]{
                    resultSet.getString("id_usuario"),
                    resultSet.getString("nombre"),
                    resultSet.getString("email"),
                    resultSet.getString("telefono"),
                    resultSet.getString("direccion"),
                    resultSet.getString("rol"),
                    resultSet.getString("fecha_registro")
            });
        }
    } catch (SQLException e) {
        System.err.println("Error al obtener usuarios: " + e.getMessage());
        throw e;
    }

    return usuarios;
}
    
   public boolean verificarPreguntaSeguridad(String email, String respuesta) {
    String query = "SELECT respuesta_seguridad FROM usuarios WHERE email = ?";

    try (Connection conexion = ConexionBaseDatos.getConexion();
         PreparedStatement statement = conexion.prepareStatement(query)) {

        statement.setString(1, email);

        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return respuesta.equalsIgnoreCase(resultSet.getString("respuesta_seguridad"));
            }
        }

    } catch (SQLException e) {
        System.err.println("Error al verificar la respuesta de seguridad: " + e.getMessage());
    }

    return false;
}

    public void registrarLog(String accion, String usuarioAfectado) {
    String query = "INSERT INTO logs (accion, usuario_afectado, fecha) VALUES (?, ?, NOW())";

    try (Connection conexion = ConexionBaseDatos.getConexion();
         PreparedStatement statement = conexion.prepareStatement(query)) {

        statement.setString(1, accion);
        statement.setString(2, usuarioAfectado);
        statement.executeUpdate();

    } catch (SQLException e) {
        System.err.println("Error al registrar el log: " + e.getMessage());
    }
}

    public String generarIdUsuario(String categoria) {
    String prefijo = "";

    // Determinar el prefijo según la categoría
    switch (categoria.toLowerCase()) {
        case "administrador":
            prefijo = "AD";
            break;
        case "profesor":
            prefijo = "PR";
            break;
        case "alumno":
            prefijo = "AL";
            break;
        default:
            throw new IllegalArgumentException("Categoría no válida: " + categoria);
    }

    // Consulta para obtener el número secuencial
    String query = "SELECT COUNT(*) + 1 AS secuencia FROM usuarios WHERE rol = ?";
    try (Connection conexion = ConexionBaseDatos.getConexion();
         PreparedStatement statement = conexion.prepareStatement(query)) {

        statement.setString(1, categoria);
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                int secuencia = resultSet.getInt("secuencia");
                return String.format("%s%03d", prefijo, secuencia); // Generar ID con formato <Prefijo><Número>
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al generar el ID de usuario: " + e.getMessage());
    }

    return null; // Retorna null si ocurre un error
}

}
