package com.biblioteca.controller;

import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.modelos.Usuario;

import java.sql.SQLException;
import java.util.List;


public class UsuarioController {
    private final UsuarioDAO usuarioDAO;

    public UsuarioController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    // Obtiene todos los IDs de usuarios.
  
    public List<String> obtenerTodosLosIDs() throws SQLException {
        return usuarioDAO.obtenerTodosLosIDs();
    }

    // Obtiene un usuario por su ID.
   
    public Usuario obtenerUsuarioPorID(String id) throws SQLException {
        return usuarioDAO.obtenerUsuarioPorID(id);
    }

    // Verifica si un correo ya existe.
    
    public boolean validarCorreoExiste(String email) throws SQLException {
        return usuarioDAO.validarCorreoExiste(email);
    }

    // Cuenta usuarios por rol.

    public int contarUsuariosPorRol(String rol) throws SQLException {
        return usuarioDAO.contarUsuariosPorRol(rol);
    }

    // Agrega un nuevo usuario.

    public void agregarUsuario(Usuario usuario) throws SQLException {
        usuarioDAO.agregarUsuario(usuario);
    }

    // Actualiza un usuario existente.
    
    public void actualizarUsuario(Usuario usuario) throws SQLException {
        usuarioDAO.actualizarUsuario(usuario);
    }

    // Elimina un usuario por su ID.
  
    public void eliminarUsuario(String id) throws SQLException {
        usuarioDAO.eliminarUsuario(id);
    }

    // Busca usuarios por parámetro y valor.
   
    public List<Usuario> buscarUsuarios(String parametro, String valor) throws SQLException {
        return usuarioDAO.buscarUsuarios(parametro, valor);
    }

    //Obtiene todos los usuarios.
  
    public List<Usuario> obtenerTodosLosUsuarios() throws SQLException {
        return usuarioDAO.obtenerTodosLosUsuarios();
    }
}
