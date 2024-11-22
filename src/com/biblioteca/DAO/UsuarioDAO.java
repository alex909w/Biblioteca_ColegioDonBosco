package com.biblioteca.dao;

import com.biblioteca.basedatos.ConexionBaseDatos;
import com.biblioteca.modelos.Usuario;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    //Obtiene todos los IDs de usuarios.
     
    public List<String> obtenerTodosLosIDs() throws SQLException {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT id FROM usuarios";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        }
        return ids;
    }

    // Obtiene un usuario por su ID.
    public Usuario obtenerUsuarioPorID(String id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    //Verifica si un correo electrónico ya está registrado.

    public boolean validarCorreoExiste(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Cuenta la cantidad de usuarios por rol.
     *
     * @param rol Rol a contar.
     * @return Cantidad de usuarios.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    
    public int contarUsuariosPorRol(String rol) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol = ?";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rol);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // Agrega un nuevo usuario a la base de datos.
   
    public void agregarUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (id, nombre, email, rol, contraseña, telefono, direccion, fecha_nacimiento) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getId());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getRol());
            stmt.setString(5, usuario.getContraseña());
            stmt.setString(6, usuario.getTelefono());
            stmt.setString(7, usuario.getDireccion());
            stmt.setDate(8, usuario.getFechaNacimiento());
            stmt.executeUpdate();
        }
    }

    //Actualiza un usuario existente.

    public void actualizarUsuario(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, email = ?, rol = ?, contraseña = ?, telefono = ?, direccion = ? WHERE id = ?";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getRol());
            stmt.setString(4, usuario.getContraseña());
            stmt.setString(5, usuario.getTelefono());
            stmt.setString(6, usuario.getDireccion());
            stmt.setString(7, usuario.getId());
            stmt.executeUpdate();
        }
    }

    // Elimina un usuario por su ID.

    public void eliminarUsuario(String id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    // Busca usuarios según un parámetro y valor.

    public List<Usuario> buscarUsuarios(String parametro, String valor) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE " + parametro + " LIKE ?";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + valor + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearUsuario(rs));
                }
            }
        }
        return usuarios;
    }

    // Obtiene todos los usuarios.

    public List<Usuario> obtenerTodosLosUsuarios() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    //Mapea un ResultSet a un objeto Usuario.

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getString("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setEmail(rs.getString("email"));
        usuario.setRol(rs.getString("rol"));
        usuario.setContraseña(rs.getString("contraseña"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setDireccion(rs.getString("direccion"));
        usuario.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
        usuario.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        return usuario;
    }

    // Inserta un nuevo usuario en la tabla 'usuarios' con los datos proporcionados y la fecha de registro actual.
    
    public void insertarUsuario(String id, String nombre, String email, String rol, String contraseña, String telefono, String direccion, Date fechaNacimiento) throws SQLException {
    String sql = "INSERT INTO usuarios (id, nombre, email, rol, contraseña, telefono, direccion, fecha_nacimiento, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    try (Connection conn = ConexionBaseDatos.getConexion();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, id);
        stmt.setString(2, nombre);
        stmt.setString(3, email);
        stmt.setString(4, rol);
        stmt.setString(5, contraseña);
        stmt.setString(6, telefono);
        stmt.setString(7, direccion);
        stmt.setDate(8, fechaNacimiento);
        stmt.executeUpdate();
    }
}

    // Obtiene un usuario de la base de datos según su email y retorna un objeto Usuario con sus datos.

    public Usuario obtenerUsuarioPorEmail(String emailUsuario) throws SQLException {
    String sql = "SELECT * FROM usuarios WHERE email = ?";
    try (Connection conn = ConexionBaseDatos.getConexion();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, emailUsuario);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getString("id"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setEmail(rs.getString("email"));
                usuario.setRol(rs.getString("rol"));
                // Asigna otros campos si es necesario
                return usuario;
            }
        }
    }
    return null; 
}

}
