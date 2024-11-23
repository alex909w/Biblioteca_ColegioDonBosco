
package com.biblioteca.controller;

import com.biblioteca.DAO.RecuperarContraDAO;
import com.biblioteca.utilidades.ConfigRecuperacionContra;
import com.biblioteca.utilidades.Validaciones;
import javax.swing.JOptionPane;

public class RecuperacionContraController {
    
    private RecuperarContraDAO recuperarContraDAO = new RecuperarContraDAO();
    
    public void enviarCredencialesController(String correo) {
        if (!Validaciones.esEmailValido(correo)) {
            JOptionPane.showMessageDialog(null, "Porfavor, ingresa un correo electrónico válido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (recuperarContraDAO.verificarCorreoExistente(correo)) {
            String contraseña = recuperarContraDAO.obtenerContraseñaPorCorreo(correo);

            if (contraseña != null) {
                String asunto = "Recuperación de Contraseña";
                String mensaje = "Estimado usuario,\n\nSu contraseña es: " + contraseña;
                ConfigRecuperacionContra.enviarCorreo(correo, asunto, mensaje);
                
                JOptionPane.showMessageDialog(null, "Se han enviado las credenciales a tu correo.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.out.println("Error: Contraseña no encontrada.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "No existe un usuario con ese correo electrónico.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
