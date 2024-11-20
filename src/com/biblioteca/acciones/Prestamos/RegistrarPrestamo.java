package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrarPrestamo extends JPanel {
    private JTextField correoUsuarioField, documentoField;
    private JLabel infoLibroLabel;
    private JComboBox<Integer> diasPrestamoComboBox;
    private JComboBox<String> estadoComboBox; // ComboBox para seleccionar el estado
    private JTable resultadosTable;
    private DefaultTableModel tableModel;

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
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Correo del usuario
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(new JLabel("Correo Usuario:"), gbc);

        correoUsuarioField = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        searchPanel.add(correoUsuarioField, gbc);

        // Campo para buscar documento
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        searchPanel.add(new JLabel("Buscar Documento:"), gbc);

        documentoField = new JTextField();
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        searchPanel.add(documentoField, gbc);

        // Botón para buscar documento
        JButton buscarButton = new JButton("Buscar");
        buscarButton.addActionListener(e -> buscarDocumentos());
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        searchPanel.add(buscarButton, gbc);

        add(searchPanel, BorderLayout.NORTH);

        // Tabla para mostrar los resultados
        tableModel = new DefaultTableModel();
        resultadosTable = new JTable(tableModel);
        resultadosTable.setFont(new Font("Arial", Font.PLAIN, 14));
        resultadosTable.setRowHeight(25);
        resultadosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(resultadosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Resultados",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));

        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior con información y acciones
        JPanel actionPanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;

        actionPanel.add(new JLabel("Días de Préstamo:"), gbc);

        diasPrestamoComboBox = new JComboBox<>(new Integer[]{7, 14, 21});
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        actionPanel.add(diasPrestamoComboBox, gbc);

        // Estado del préstamo
        gbc.gridx = 0;
        gbc.gridy = 1;
        actionPanel.add(new JLabel("Estado:"), gbc);

        estadoComboBox = new JComboBox<>(new String[] { "Pendiente", "Devuelto", "Mora" });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        actionPanel.add(estadoComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        infoLibroLabel = new JLabel("Seleccione un documento para ver detalles.");
        infoLibroLabel.setHorizontalAlignment(SwingConstants.CENTER);
        actionPanel.add(infoLibroLabel, gbc);

        JButton registrarButton = new JButton("Registrar Préstamo");
        registrarButton.addActionListener(e -> registrarPrestamo());
        gbc.gridy = 3;
        gbc.gridwidth = 2;
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
                    tableModel.addColumn(metaData.getColumnLabel(i));
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

    Integer diasPrestamo = (Integer) diasPrestamoComboBox.getSelectedItem(); // Obtener el valor seleccionado de días de préstamo
    if (diasPrestamo == null) {  // Validar que se haya seleccionado un valor
        JOptionPane.showMessageDialog(this, "Por favor, seleccione los días de préstamo.");
        return;
    }

    String estado = (String) estadoComboBox.getSelectedItem(); // Obtener estado seleccionado

    try (Connection conn = ConexionBaseDatos.getConexion()) {
        // Buscar el id_usuario usando el correo
        String usuarioQuery = "SELECT id FROM usuarios WHERE email = ?";
        PreparedStatement usuarioStmt = conn.prepareStatement(usuarioQuery);
        usuarioStmt.setString(1, correoUsuario);
        ResultSet usuarioRs = usuarioStmt.executeQuery();

        if (!usuarioRs.next()) {
            JOptionPane.showMessageDialog(this, "El correo no está registrado en el sistema.");
            return;
        }

        String idUsuario = usuarioRs.getString("id"); // Obtener el id_usuario como String
        String idDocumento = tableModel.getValueAt(filaSeleccionada, 0).toString();

        // Validar mora pendiente
        String moraQuery = "SELECT d.dias_mora " +
                           "FROM devoluciones d " +
                           "JOIN prestamos p ON d.id_prestamo = p.id " +
                           "WHERE p.id_usuario = ? AND d.dias_mora > 0";
        try (PreparedStatement moraStmt = conn.prepareStatement(moraQuery)) {
            moraStmt.setString(1, idUsuario); // Usar id_usuario como String
            ResultSet moraRs = moraStmt.executeQuery();
            if (moraRs.next()) {
                JOptionPane.showMessageDialog(this, "El usuario tiene mora pendiente. No puede registrar préstamos.");
                return;
            }
        }

        // Registrar el préstamo
        String registrarQuery = "INSERT INTO prestamos (id_usuario, id_documento, fecha_prestamo, fecha_devolucion, dias_mora, estado, fecha_registro, email, dias_prestamo) " +
                                "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY), 0, ?, CURDATE(), ?, ?)";
        try (PreparedStatement registrarStmt = conn.prepareStatement(registrarQuery)) {
            registrarStmt.setString(1, idUsuario); // Usar id_usuario como String
            registrarStmt.setString(2, idDocumento);
            registrarStmt.setInt(3, diasPrestamo); // Pasar el valor entero de los días de préstamo
            registrarStmt.setString(4, estado);
            registrarStmt.setString(5, correoUsuario); // Aquí asignamos el correo del usuario
            registrarStmt.setInt(6, diasPrestamo); // Asegúrate de pasar los días de préstamo aquí
            registrarStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Préstamo registrado exitosamente.");
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al registrar préstamo: " + ex.getMessage());
    }
}
}
