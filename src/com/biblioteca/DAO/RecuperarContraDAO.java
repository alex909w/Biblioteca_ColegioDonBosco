
package com.biblioteca.DAO;

import com.biblioteca.basedatos.ConexionBaseDatos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecuperarContraDAO {
    
    // Método para verificar si el correo existe en la base de datos
    public boolean verificarCorreoExistente(String correo) {
        boolean existe = false;
        try {
            Connection con = ConexionBaseDatos.getConexion();
            String query = "SELECT 1 FROM Usuarios WHERE email = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, correo);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                existe = true; 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return existe;
    }
    
    // Método para obtener la contraseña asociada al correo
    public String obtenerContraseñaPorCorreo(String correo) {
        String contraseña = null;
        try {
            Connection con = ConexionBaseDatos.getConexion();
            String query = "SELECT contraseña FROM Usuarios WHERE email = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, correo);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                contraseña = rs.getString("contraseña");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contraseña; 
    }
}
