package com.biblioteca.controller;

import com.biblioteca.dao.ConfiguracionDAO;

import java.sql.SQLException;
import java.util.Map;

public class ConfiguracionController {
    private ConfiguracionDAO configuracionDAO;

    public ConfiguracionController() {
        this.configuracionDAO = new ConfiguracionDAO();
    }

    //Obtiene las configuraciones para un rol específico.
  
    public Map<String, Double> obtenerConfiguracionPorRol(String rol) throws SQLException {
        return configuracionDAO.obtenerConfiguracionPorRol(rol);
    }

    //Actualiza las configuraciones para un rol específico.

    public void actualizarConfiguracionPorRol(String rol, Map<String, Double> valores) throws SQLException {
        configuracionDAO.actualizarConfiguracionPorRol(rol, valores);
    }
}
