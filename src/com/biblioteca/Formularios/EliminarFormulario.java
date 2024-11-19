package com.biblioteca.Formularios;



import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EliminarFormulario extends JPanel {
    private JComboBox<String> tablasComboBox;

    public EliminarFormulario() {
        setLayout(new GridLayout(3, 1, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Eliminar Formulario Existente"));

        add(new JLabel("Seleccione una Tabla para Eliminar:"));

        tablasComboBox = new JComboBox<>();
        cargarTablasExistentes();
        add(tablasComboBox);

        JButton eliminarButton = new JButton("Eliminar Tabla");
        eliminarButton.addActionListener(e -> eliminarTabla());
        add(eliminarButton);
    }

    private void cargarTablasExistentes() {
        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {

            while (rs.next()) {
                tablasComboBox.addItem(rs.getString(1));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas: " + ex.getMessage());
        }
    }

    private void eliminarTabla() {
        String nombreTabla = (String) tablasComboBox.getSelectedItem();
        if (nombreTabla == null || nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar la tabla '" + nombreTabla + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = ConexionBaseDatos.getConexion();
                 Statement stmt = conn.createStatement()) {

                String sql = "DROP TABLE " + nombreTabla;
                stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(this, "Tabla eliminada exitosamente.");
                tablasComboBox.removeItem(nombreTabla);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar la tabla: " + ex.getMessage());
            }
        }
    }
}
