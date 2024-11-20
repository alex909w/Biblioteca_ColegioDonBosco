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
    private JTextField nombreField, emailField, telefonoField, direccionField, fechaNacimientoField, departamentoField, fechaRegistroField;
    private JButton eliminarButton;
    
    private final Color FONDO_LATERAL = new Color(248, 249, 250);
    private final Font FUENTE_PRINCIPAL = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);

    public EliminarUsuario() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(FONDO_LATERAL);

        // Panel superior para ID del usuario
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        idPanel.setBackground(getBackground());
        idPanel.add(createLabel("ID Usuario:"));
        idUsuarioComboBox = crearCombobox();
        cargarUsuariosEnComboBox();
        idUsuarioComboBox.addActionListener(e -> cargarDatosUsuario());
        idPanel.add(idUsuarioComboBox);
        add(idPanel, BorderLayout.NORTH);

        // Panel central
        JPanel formularioPanel = new JPanel();
        formularioPanel.setLayout(new BoxLayout(formularioPanel, BoxLayout.Y_AXIS));
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        formularioPanel.setBackground(FONDO_LATERAL);

        formularioPanel.add(crearCampo("Nombre:", nombreField = new JTextField(), false));
        formularioPanel.add(crearCampo("Email:", emailField = new JTextField(), false));
        formularioPanel.add(crearCampo("Teléfono:", telefonoField = new JTextField(), false));
        formularioPanel.add(crearCampo("Dirección:", direccionField = new JTextField(), false));
        formularioPanel.add(crearCampo("Fecha de Nacimiento (YYYY-MM-DD):", fechaNacimientoField = new JTextField(), false));
        formularioPanel.add(crearCampo("Departamento:", departamentoField = new JTextField(), false));

        add(formularioPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton eliminarButton = crearBoton("Eliminar", new Color(193, 42, 46));
        eliminarButton.addActionListener(e -> eliminarUsuario());
        buttonPanel.add(eliminarButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
  
    private JComboBox<String> crearCombobox(String... items){
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(FUENTE_PRINCIPAL);
        return comboBox;
        
    }
    
        private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FUENTE_TITULO);
        return label;
    }
    
    private JPanel crearCampo(String etiqueta, JComponent campo, boolean soloLectura) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(250, 250, 250));

        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(200, 30));
        label.setForeground(new Color(50, 50, 50));

        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setPreferredSize(new Dimension(200, 30));
        campo.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        if (campo instanceof JTextField) {
            ((JTextField) campo).setEnabled(!soloLectura);
        }

        panel.add(label, BorderLayout.WEST);
        panel.add(campo, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        return panel;
    }

    private JButton crearBoton(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(colorFondo);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo);
            }
        });

        return boton;
    }    

    private void cargarUsuariosEnComboBox() {
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM usuarios")) {
            ResultSet rs = stmt.executeQuery();
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
                fechaRegistroField.setText(rs.getString("fecha_registro"));
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
        fechaRegistroField.setText("");

    }

    private JTextField createReadOnlyTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setForeground(Color.BLACK);
        textField.setEditable(false);
        return textField;
    }
}
