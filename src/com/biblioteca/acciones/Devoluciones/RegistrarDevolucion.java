package com.biblioteca.acciones.Devoluciones;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RegistrarDevolucion extends JPanel {
    private JTable prestamosTable;
    private DefaultTableModel tableModel;
    private JTextField idPrestamoField, idDocumentoField;
    private JComboBox<String> estadoComboBox;
    private JButton registrarButton;

    public RegistrarDevolucion() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Registrar Devolución",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Tabla para mostrar los préstamos
        tableModel = new DefaultTableModel(new String[]{"ID Préstamo", "ID Documento", "Fecha Préstamo", "Estado"}, 0);
        prestamosTable = new JTable(tableModel);
        prestamosTable.setFont(new Font("Arial", Font.PLAIN, 14));
        prestamosTable.setRowHeight(25);
        prestamosTable.setFillsViewportHeight(true);
        prestamosTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(prestamosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Préstamos Actuales",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        add(scrollPane, BorderLayout.CENTER);

        // Formulario de devolución
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campo ID Préstamo
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(createStyledLabel("ID Préstamo:"), gbc);

        idPrestamoField = createStyledTextField();
        idPrestamoField.setEditable(false);
        gbc.gridx = 1;
        formPanel.add(idPrestamoField, gbc);

        // Campo ID Documento
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(createStyledLabel("ID Documento:"), gbc);

        idDocumentoField = createStyledTextField();
        idDocumentoField.setEditable(false);
        gbc.gridx = 1;
        formPanel.add(idDocumentoField, gbc);

        // Campo Estado del Documento
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(createStyledLabel("Estado del Documento:"), gbc);

        estadoComboBox = createStyledComboBox(new String[]{"Bueno", "Dañado", "En Reparación"});
        gbc.gridx = 1;
        formPanel.add(estadoComboBox, gbc);

        // Botón Registrar Devolución
        registrarButton = createStyledButton("Registrar Devolución", new Color(34, 139, 34), new Color(0, 100, 0));
        registrarButton.addActionListener(e -> registrarDevolucion());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(registrarButton, gbc);

        add(formPanel, BorderLayout.SOUTH);

        // Cargar la lista de préstamos automáticamente
        cargarPrestamos();

        // Evento de selección en la tabla
        prestamosTable.getSelectionModel().addListSelectionListener(event -> {
            int selectedRow = prestamosTable.getSelectedRow();
            if (selectedRow != -1) {
                idPrestamoField.setText(prestamosTable.getValueAt(selectedRow, 0).toString());
                idDocumentoField.setText(prestamosTable.getValueAt(selectedRow, 1).toString());
            }
        });
    }

    private void cargarPrestamos() {
        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Consulta para obtener los préstamos pendientes
            String prestamosQuery =
                    "SELECT id AS id_prestamo, id_documento, fecha_prestamo, estado " +
                    "FROM prestamos " +
                    "WHERE estado != 'Devuelto'";
            PreparedStatement stmt = conn.prepareStatement(prestamosQuery);
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0); // Limpiar resultados previos

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_prestamo"),
                        rs.getString("id_documento"),
                        rs.getDate("fecha_prestamo"),
                        rs.getString("estado")
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No se encontraron préstamos pendientes.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar los préstamos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarDevolucion() {
        String idPrestamo = idPrestamoField.getText().trim();
        String idDocumento = idDocumentoField.getText().trim();
        String estadoDocumento = (String) estadoComboBox.getSelectedItem();

        if (idPrestamo.isEmpty() || idDocumento.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un préstamo para registrar la devolución.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Verificar y registrar devolución
            String updatePrestamoQuery = "UPDATE prestamos SET estado = 'Devuelto', dias_mora = ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updatePrestamoQuery);

            // Calcular días de mora
            String fechaQuery = "SELECT fecha_devolucion FROM prestamos WHERE id = ?";
            PreparedStatement fechaStmt = conn.prepareStatement(fechaQuery);
            fechaStmt.setString(1, idPrestamo);
            ResultSet fechaRs = fechaStmt.executeQuery();
            long diasMora = 0;
            if (fechaRs.next()) {
                LocalDate fechaDevolucion = fechaRs.getDate("fecha_devolucion").toLocalDate();
                LocalDate fechaActual = LocalDate.now();
                if (fechaActual.isAfter(fechaDevolucion)) {
                    diasMora = ChronoUnit.DAYS.between(fechaDevolucion, fechaActual);
                }
            }

            updateStmt.setInt(1, (int) diasMora);
            updateStmt.setString(2, idPrestamo);
            updateStmt.executeUpdate();

            // Actualizar el estado del documento
            String updateDocumentoQuery = "UPDATE ? SET estado = ? WHERE id = ?";
            PreparedStatement updateDocStmt = conn.prepareStatement(updateDocumentoQuery);
            updateDocStmt.setString(1, obtenerTablaDocumento(idDocumento));
            updateDocStmt.setString(2, estadoDocumento);
            updateDocStmt.setString(3, idDocumento);
            updateDocStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Devolución registrada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarPrestamos(); // Recargar la tabla
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar devolución: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String obtenerTablaDocumento(String idDocumento) throws SQLException {
        String prefijo = idDocumento.substring(0, 3).toUpperCase();

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT nombre FROM tipos_documentos")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String nombreTabla = rs.getString("nombre");
                String prefijoTabla = nombreTabla.length() >= 3 ? nombreTabla.substring(0, 3).toUpperCase() : nombreTabla.toUpperCase();

                if (prefijo.equals(prefijoTabla)) {
                    return nombreTabla;
                }
            }
        }

        return null;
    }

    // Métodos auxiliares para crear componentes estilizados
    private JButton createStyledButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 40));
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(defaultColor);
            }
        });
        return button;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        textField.setBackground(Color.WHITE);
        return textField;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
    }
}
