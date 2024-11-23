
package com.biblioteca.utilidades;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class ConfigRecuperacionContra {
    
    // Aqui creamos el metodo que se encargara de enviar el correo
    public static void enviarCorreo(String destinatario, String asunto, String mensaje) {
        String correo_remitente = "guilleacc26@gmail.com";
        String clave_remitente = "hles ehwb yhvz tdbb";
        
        // Configuramos la conexion al servidor de Gmail
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        propiedades.put("mail.smtp.port", "587");
        
        // Obtenemos la sesión de correo
        Session session = Session.getInstance(propiedades, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(correo_remitente, clave_remitente);
            }
        });
        
        try {
            // Crear el mensaje de correo
            Message mensajeCorreo = new MimeMessage(session);
            mensajeCorreo.setFrom(new InternetAddress(correo_remitente));
            mensajeCorreo.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensajeCorreo.setSubject(asunto);
            mensajeCorreo.setText(mensaje);

            // Enviar el correo
            Transport.send(mensajeCorreo);
            System.out.println("Correo enviado con éxito a " + destinatario);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
