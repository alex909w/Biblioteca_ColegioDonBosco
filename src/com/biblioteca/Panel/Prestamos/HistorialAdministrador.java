package com.biblioteca.Panel.Prestamos;

import com.biblioteca.basedatos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HistorialAdministrador extends JPanel {
    private JTable historialTable;
    private DefaultTableModel tableModel;
    private JTextField correoUsuarioField; // Campo para ingresar el correo del usuario
    private JButton buscarButton, cargarTodosButton;

    public HistorialAdministrador() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Historial de Préstamos",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel superior para buscar por correo
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel searchLabel = createStyledLabel("Correo del Usuario:");
        searchPanel.add(searchLabel);

        correoUsuarioField = new JTextField(20);
        searchPanel.add(correoUsuarioField);

        buscarButton = createStyledButton("Buscar", new Color(34, 139, 34), new Color(0, 100, 0));
        buscarButton.addActionListener(e -> cargarHistorial(correoUsuarioField.getText().trim()));
        searchPanel.add(buscarButton);

        cargarTodosButton = createStyledButton("Cargar Todos", new Color(70, 130, 180), new Color(0, 50, 130));
        cargarTodosButton.addActionListener(e -> cargarHistorial(null));
        searchPanel.add(cargarTodosButton);

        add(searchPanel, BorderLayout.NORTH);

        // Configuración de la tabla
        tableModel = new DefaultTableModel(new String[]{"ID", "ID Usuario", "Correo", "ID Documento", "Fecha Préstamo", "Estado"}, 0);
        historialTable = createStyledTable(tableModel);

        JScrollPane scrollPane = createStyledScrollPane(historialTable, "Resultados");
        add(scrollPane, BorderLayout.CENTER);
    }

    private void cargarHistorial(String correoUsuario) {
        try (Connection conn = ConexionBaseDatos.getConexion()) {
            tableModel.setRowCount(0); // Limpiar resultados previos

            String historialQuery;
            PreparedStatement historialStmt;

            if (correoUsuario != null && !correoUsuario.isEmpty()) {
                // Consultar historial para un usuario específico
                historialQuery = "SELECT p.id, p.id_usuario, u.email, p.id_documento, p.fecha_prestamo, p.estado " +
                                 "FROM prestamos p JOIN usuarios u ON p.id_usuario = u.id WHERE u.email = ?";
                historialStmt = conn.prepareStatement(historialQuery);
                historialStmt.setString(1, correoUsuario);
            } else {
                // Consultar historial para todos los usuarios
                historialQuery = "SELECT p.id, p.id_usuario, u.email, p.id_documento, p.fecha_prestamo, p.estado " +
                                 "FROM prestamos p JOIN usuarios u ON p.id_usuario = u.id";
                historialStmt = conn.prepareStatement(historialQuery);
            }

            ResultSet historialRs = historialStmt.executeQuery();
            while (historialRs.next()) {
                tableModel.addRow(new Object[]{
                        historialRs.getInt("id"),
                        historialRs.getString("id_usuario"),
                        historialRs.getString("email"),
                        historialRs.getString("id_documento"),
                        historialRs.getDate("fecha_prestamo"),
                        historialRs.getString("estado")
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, correoUsuario != null && !correoUsuario.isEmpty()
                                ? "No se encontraron préstamos para el usuario: " + correoUsuario
                                : "No se encontraron préstamos.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar historial de préstamos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Métodos auxiliares para crear componentes estilizados

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        // Centrar el texto en columnas pequeñas
        centrarTextoColumnas(table);

        return table;
    }

    private void centrarTextoColumnas(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            String columnName = columnModel.getColumn(i).getHeaderValue().toString().toLowerCase();
            if (columnName.contains("id") || columnName.contains("estado")) {
                columnModel.getColumn(i).setCellRenderer(centerRenderer);
                columnModel.getColumn(i).setPreferredWidth(80);
            } else {
                columnModel.getColumn(i).setPreferredWidth(150);
            }
        }

        JTableHeader header = table.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private JScrollPane createStyledScrollPane(Component component, String title) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        return scrollPane;
    }

    private JButton createStyledButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 30));
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
}
