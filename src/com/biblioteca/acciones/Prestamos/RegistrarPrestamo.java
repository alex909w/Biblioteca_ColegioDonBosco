package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegistrarPrestamo extends JPanel {
    private JTextField correoUsuarioField, documentoField;
    private JLabel infoLibroLabel;
    private JComboBox<Integer> diasPrestamoComboBox;
    private JComboBox<String> estadoComboBox;
    private JTable resultadosTable;
    private DefaultTableModel tableModel;
    private JButton buscarButton, registrarButton;

    public RegistrarPrestamo() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Registrar Préstamo",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Correo del usuario
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        searchPanel.add(createStyledLabel("Correo Usuario:"), gbc);

        correoUsuarioField = createStyledTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        searchPanel.add(correoUsuarioField, gbc);

        // Campo para buscar documento
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        searchPanel.add(createStyledLabel("Buscar Documento:"), gbc);

        documentoField = createStyledTextField();
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        searchPanel.add(documentoField, gbc);

        // Botón para buscar documento
        buscarButton = createStyledButton("Buscar");
        buscarButton.addActionListener(e -> buscarDocumentos());
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        searchPanel.add(buscarButton, gbc);

        add(searchPanel, BorderLayout.NORTH);

        // Tabla para mostrar los resultados con diseño mejorado
        tableModel = new DefaultTableModel();
        resultadosTable = new JTable(tableModel);
        resultadosTable.setFont(new Font("Arial", Font.PLAIN, 14));
        resultadosTable.setRowHeight(25);
        resultadosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultadosTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        resultadosTable.getTableHeader().setReorderingAllowed(false);

        // Estilo para el encabezado de la tabla
        JTableHeader header = resultadosTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(70, 130, 180));
        header.setReorderingAllowed(false);

        // ScrollPane para la tabla con diseño estilizado
        JScrollPane scrollPane = new JScrollPane(resultadosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Resultados de la Búsqueda",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(70, 130, 180)
        ));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior con información y acciones
        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.gridwidth = 1;

        actionPanel.add(createStyledLabel("Días de Préstamo:"), gbc);

        diasPrestamoComboBox = createStyledComboBox(new Integer[]{7, 14, 21});
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        actionPanel.add(diasPrestamoComboBox, gbc);

        // Estado del préstamo
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        actionPanel.add(createStyledLabel("Estado:"), gbc);

        estadoComboBox = createStyledComboBox(new String[]{"Pendiente", "Devuelto", "Mora"});
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        actionPanel.add(estadoComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        infoLibroLabel = createStyledLabel("Seleccione un documento para ver detalles.");
        infoLibroLabel.setHorizontalAlignment(SwingConstants.CENTER);
        actionPanel.add(infoLibroLabel, gbc);

        registrarButton = createStyledButton("Registrar Préstamo");
        registrarButton.addActionListener(e -> registrarPrestamo());
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        actionPanel.add(registrarButton, gbc);

        add(actionPanel, BorderLayout.SOUTH);
    }

    private void buscarDocumentos() {
        String titulo = documentoField.getText().trim();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un título para buscar.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            List<String> tablas = obtenerTablas(conn);
            if (tablas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron tablas en 'tipos_documentos'.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            tableModel.setRowCount(0); // Limpiar resultados previos
            tableModel.setColumnCount(0); // Limpiar columnas previas

            for (String tabla : tablas) {
                buscarEnTabla(conn, tabla, titulo);
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No se encontraron resultados para el documento especificado.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar documentos: " + ex.getMessage());
        }
    }

    private List<String> obtenerTablas(Connection conn) throws SQLException {
        List<String> tablas = new ArrayList<>();
        String sql = "SELECT nombre FROM tipos_documentos";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tablas.add(rs.getString("nombre"));
            }
        }
        return tablas;
    }

    private void buscarEnTabla(Connection conn, String tabla, String titulo) throws SQLException {
        String sql = "SELECT * FROM " + tabla + " WHERE CONCAT_WS(' ', " + obtenerColumnas(conn, tabla) + ") LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + titulo + "%");
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            if (tableModel.getColumnCount() == 0) {
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    tableModel.addColumn(formatColumnName(metaData.getColumnLabel(i)));
                }
            }

            while (rs.next()) {
                Object[] row = new Object[metaData.getColumnCount()];
                for (int i = 0; i < row.length; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                tableModel.addRow(row);
            }
        }
    }

    private String obtenerColumnas(Connection conn, String tabla) throws SQLException {
        List<String> columnas = new ArrayList<>();
        String sql = "DESCRIBE " + tabla;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                columnas.add("`" + rs.getString("Field") + "`");
            }
        }
        return String.join(", ", columnas);
    }

    private void registrarPrestamo() {
        int filaSeleccionada = resultadosTable.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un documento para registrar el préstamo.");
            return;
        }

        String correoUsuario = correoUsuarioField.getText().trim();
        if (correoUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese el correo del usuario.");
            return;
        }

        Integer diasPrestamo = (Integer) diasPrestamoComboBox.getSelectedItem();
        if (diasPrestamo == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione los días de préstamo.");
            return;
        }

        String estado = (String) estadoComboBox.getSelectedItem();

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String usuarioQuery = "SELECT id FROM usuarios WHERE email = ?";
            PreparedStatement usuarioStmt = conn.prepareStatement(usuarioQuery);
            usuarioStmt.setString(1, correoUsuario);
            ResultSet usuarioRs = usuarioStmt.executeQuery();

            if (!usuarioRs.next()) {
                JOptionPane.showMessageDialog(this, "El correo no está registrado en el sistema.");
                return;
            }

            String idUsuario = usuarioRs.getString("id");
            String idDocumento = tableModel.getValueAt(filaSeleccionada, 0).toString();

            String moraQuery = "SELECT d.dias_mora " +
                    "FROM devoluciones d " +
                    "JOIN prestamos p ON d.id_prestamo = p.id " +
                    "WHERE p.id_usuario = ? AND d.dias_mora > 0";
            try (PreparedStatement moraStmt = conn.prepareStatement(moraQuery)) {
                moraStmt.setString(1, idUsuario);
                ResultSet moraRs = moraStmt.executeQuery();
                if (moraRs.next()) {
                    JOptionPane.showMessageDialog(this, "El usuario tiene mora pendiente. No puede registrar préstamos.");
                    return;
                }
            }

            String registrarQuery = "INSERT INTO prestamos (id_usuario, id_documento, fecha_prestamo, fecha_devolucion, dias_mora, estado, fecha_registro, email, dias_prestamo) " +
                    "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY), 0, ?, CURDATE(), ?, ?)";
            try (PreparedStatement registrarStmt = conn.prepareStatement(registrarQuery)) {
                registrarStmt.setString(1, idUsuario);
                registrarStmt.setString(2, idDocumento);
                registrarStmt.setInt(3, diasPrestamo);
                registrarStmt.setString(4, estado);
                registrarStmt.setString(5, correoUsuario);
                registrarStmt.setInt(6, diasPrestamo);
                registrarStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Préstamo registrado exitosamente.");

                limpiarFormulario();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar préstamo: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        correoUsuarioField.setText("");
        documentoField.setText("");
        tableModel.setRowCount(0);
        diasPrestamoComboBox.setSelectedIndex(0);
        estadoComboBox.setSelectedIndex(0);
        infoLibroLabel.setText("Seleccione un documento para ver detalles.");
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(34, 139, 34));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 40));
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 100, 0));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(34, 139, 34));
            }
        });
        return button;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        textField.setBackground(Color.WHITE);
        return textField;
    }

    private <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
    }

    private String formatColumnName(String input) {
        return input.toUpperCase().replace("_", " ");
    }
}
