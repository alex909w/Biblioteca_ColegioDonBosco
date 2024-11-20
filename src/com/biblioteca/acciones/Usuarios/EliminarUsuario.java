package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.border.TitledBorder;

public class EliminarUsuario extends JPanel {
    private JComboBox<String> idUsuarioComboBox;
    private JTextField nombreField, emailField, telefonoField, direccionField, fechaNacimientoField, departamentoField;
    private JButton eliminarButton;

    public EliminarUsuario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)),
                "Eliminar Usuario", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16), new Color(50, 50, 50)));

        // Panel superior con ID del usuario
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        idPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel idLabel = new JLabel("ID Usuario:");
        idLabel.setFont(new Font("Arial", Font.BOLD, 14));
        idPanel.add(idLabel);

        idUsuarioComboBox = new JComboBox<>();
        idUsuarioComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        idUsuarioComboBox.setPreferredSize(new Dimension(200, 30));
        cargarUsuariosEnComboBox(); // Cargar IDs en el combo box
        idUsuarioComboBox.addActionListener(e -> cargarDatosUsuario()); // Cargar datos al seleccionar
        idPanel.add(idUsuarioComboBox);

        add(idPanel, BorderLayout.NORTH);

        // Panel central para mostrar datos
        JPanel datosPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        datosPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        datosPanel.add(new JLabel("Nombre:"));
        nombreField = new JTextField();
        nombreField.setEnabled(false); // Solo lectura
        datosPanel.add(nombreField);

        datosPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        emailField.setEnabled(false); // Solo lectura
        datosPanel.add(emailField);

        datosPanel.add(new JLabel("Teléfono:"));
        telefonoField = new JTextField();
        telefonoField.setEnabled(false); // Solo lectura
        datosPanel.add(telefonoField);

        datosPanel.add(new JLabel("Dirección:"));
        direccionField = new JTextField();
        direccionField.setEnabled(false); // Solo lectura
        datosPanel.add(direccionField);

        datosPanel.add(new JLabel("Fecha Nacimiento:"));
        fechaNacimientoField = new JTextField();
        fechaNacimientoField.setEnabled(false); // Solo lectura
        datosPanel.add(fechaNacimientoField);

        datosPanel.add(new JLabel("Departamento:"));
        departamentoField = new JTextField();
        departamentoField.setEnabled(false); // Solo lectura
        datosPanel.add(departamentoField);

        add(datosPanel, BorderLayout.CENTER);

        // Botón para eliminar
        eliminarButton = new JButton("Eliminar");
        eliminarButton.setFont(new Font("Arial", Font.BOLD, 14));
        eliminarButton.setBackground(new Color(220, 53, 69)); // Rojo
        eliminarButton.setForeground(Color.WHITE);
        eliminarButton.setFocusPainted(false);
        eliminarButton.setPreferredSize(new Dimension(150, 40));
        eliminarButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        eliminarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                eliminarButton.setBackground(new Color(176, 0, 32)); // Rojo oscuro al pasar
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                eliminarButton.setBackground(new Color(220, 53, 69)); // Color original
            }
        });
        eliminarButton.addActionListener(e -> eliminarUsuario());

        // Panel inferior con botón
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(eliminarButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void cargarUsuariosEnComboBox() {
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM usuarios")) {
            ResultSet rs = stmt.executeQuery();
            idUsuarioComboBox.addItem(""); // Agregar opción vacía
            while (rs.next()) {
                idUsuarioComboBox.addItem(rs.getString("id"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage());
        }
    }

    private void cargarDatosUsuario() {
        String idUsuario = (String) idUsuarioComboBox.getSelectedItem();
        if (idUsuario == null || idUsuario.isEmpty()) {
            limpiarCampos();
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT * FROM usuarios WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idUsuario);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nombreField.setText(rs.getString("nombre"));
                emailField.setText(rs.getString("email"));
                telefonoField.setText(rs.getString("telefono"));
                direccionField.setText(rs.getString("direccion"));
                fechaNacimientoField.setText(rs.getString("fecha_nacimiento"));
                departamentoField.setText(rs.getString("departamento"));
            } else {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
                limpiarCampos();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos del usuario: " + ex.getMessage());
        }
    }

    private void eliminarUsuario() {
        String idUsuario = (String) idUsuarioComboBox.getSelectedItem();

        if (idUsuario == null || idUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un ID de usuario.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar al usuario con ID: " + idUsuario + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = ConexionBaseDatos.getConexion()) {
                String deleteQuery = "DELETE FROM usuarios WHERE id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                deleteStmt.setString(1, idUsuario);

                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Usuario eliminado exitosamente.");
                    idUsuarioComboBox.removeItem(idUsuario); // Eliminar del combo box
                    limpiarCampos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el usuario.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar usuario: " + ex.getMessage());
            }
        }
    }

    private void limpiarCampos() {
        nombreField.setText("");
        emailField.setText("");
        telefonoField.setText("");
        direccionField.setText("");
        fechaNacimientoField.setText("");
        departamentoField.setText("");
    }
}
