package com.biblioteca.utilidades;

import java.util.regex.Pattern;

public class Validaciones {

     public static boolean esEmailValido(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

     public static boolean esContraseñaSegura(String contraseña) {
        if (contraseña.length() < 8) {
            return false;
        }
        boolean tieneLetra = false;
        boolean tieneNumero = false;
        for (char c : contraseña.toCharArray()) {
            if (Character.isLetter(c)) {
                tieneLetra = true;
            }
            if (Character.isDigit(c)) {
                tieneNumero = true;
            }
            if (tieneLetra && tieneNumero) {
                return true;
            }
        }
        return false;
    }

    public static boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
    


   

   
}
