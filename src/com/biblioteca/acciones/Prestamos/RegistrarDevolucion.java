package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Clase para gestionar la devolución de préstamos en la biblioteca.
 * Muestra automáticamente el correo del usuario autenticado en la parte superior.
 */
public class RegistrarDevolucion extends JPanel {
    private String emailUsuario; // Almacenar el correo del usuario autenticado
    private String idUsuario;    // Almacenar el ID del usuario autenticado
    private JTable prestamosTable;
    private DefaultTableModel tableModel;
    private JTextField idPrestamoField, idDocumentoField, fechaDevolucionField;
    private JComboBox<String> estadoComboBox;
    private JButton registrarButton;
    private JLabel correoUsuarioLabel; // Etiqueta para mostrar el correo del usuario autenticado

    public RegistrarDevolucion(String emailUsuario) {
        this.emailUsuario = emailUsuario; // Asignar el correo recibido
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Registrar Devolución",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel Superior: Etiqueta de correo del usuario
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.setBackground(new Color(255, 255, 255));
        panelSuperior.setBorder(new EmptyBorder(10, 20, 10, 20));

        String correoUsuario = this.emailUsuario; // Obtener correo del usuario autenticado
        correoUsuarioLabel = createStyledLabel("Usuario: " + correoUsuario);
        panelSuperior.add(correoUsuarioLabel);

        add(panelSuperior, BorderLayout.NORTH);

        // Configuración de la tabla de préstamos
        tableModel = new DefaultTableModel(new Object[]{"ID Préstamo", "ID Documento", "Fecha Préstamo", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que las celdas no sean editables
            }
        };
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

        // Configuración del formulario de devolución
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

        // Campo Fecha Real de Devolución
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(createStyledLabel("Fecha Real de Devolución (YYYY-MM-DD):"), gbc);

        fechaDevolucionField = createStyledTextField();
        gbc.gridx = 1;
        formPanel.add(fechaDevolucionField, gbc);

        // Campo Estado del Documento
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(createStyledLabel("Estado del Documento:"), gbc);

        estadoComboBox = createStyledComboBox(new String[]{"Bueno", "Dañado", "En Reparación"});
        gbc.gridx = 1;
        formPanel.add(estadoComboBox, gbc);

        // Botón Registrar Devolución
        registrarButton = createStyledButton("Registrar Devolución", new Color(34, 139, 34), new Color(0, 100, 0));
        registrarButton.addActionListener(e -> registrarDevolucion());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(registrarButton, gbc);

        add(formPanel, BorderLayout.SOUTH);

        cargarUsuario();    // Cargar ID del usuario
        cargarPrestamos();  // Cargar préstamos del usuario

        prestamosTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                int selectedRow = prestamosTable.getSelectedRow();
                if (selectedRow != -1) {
                    idPrestamoField.setText(prestamosTable.getValueAt(selectedRow, 0).toString());
                    idDocumentoField.setText(prestamosTable.getValueAt(selectedRow, 1).toString());
                }
            }
        });
    }

    /**
     * Carga el ID del usuario basado en el correo electrónico.
     */
    private void cargarUsuario() {
        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String usuarioQuery = "SELECT id FROM usuarios WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(usuarioQuery);
            stmt.setString(1, emailUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                idUsuario = rs.getString("id");
            } else {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener el ID del usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Carga los préstamos actuales del usuario autenticado que no han sido devueltos.
     */
    private void cargarPrestamos() {
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se pudo obtener el ID del usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String prestamosQuery =
                    "SELECT id AS id_prestamo, id_documento, fecha_prestamo, estado " +
                    "FROM prestamos WHERE id_usuario = ? AND estado != 'Devuelto'";
            PreparedStatement stmt = conn.prepareStatement(prestamosQuery);
            stmt.setString(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            tableModel.setRowCount(0); // Limpiar tabla

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_prestamo"),
                        rs.getString("id_documento"),
                        rs.getDate("fecha_prestamo"),
                        rs.getString("estado")
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No tienes préstamos pendientes.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar los préstamos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Registra la devolución de un préstamo seleccionado.
     */
    private void registrarDevolucion() {
        String idPrestamoStr = idPrestamoField.getText().trim();
        String idDocumento = idDocumentoField.getText().trim();
        String estadoDocumento = (String) estadoComboBox.getSelectedItem();
        String fechaDevolucionStr = fechaDevolucionField.getText().trim();

        if (idPrestamoStr.isEmpty() || idDocumento.isEmpty() || fechaDevolucionStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate fechaDevolucion;
        try {
            fechaDevolucion = LocalDate.parse(fechaDevolucionStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha incorrecto. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Obtener detalles del préstamo
            String detallesPrestamoQuery = "SELECT fecha_devolucion_programada FROM prestamos WHERE id = ? AND id_usuario = ?";
            PreparedStatement detallesStmt = conn.prepareStatement(detallesPrestamoQuery);
            detallesStmt.setInt(1, Integer.parseInt(idPrestamoStr));
            detallesStmt.setString(2, idUsuario);
            ResultSet detallesRs = detallesStmt.executeQuery();

            if (!detallesRs.next()) {
                JOptionPane.showMessageDialog(this, "No se encontraron detalles del préstamo o no te pertenece.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate fechaDevolucionProgramada = detallesRs.getDate("fecha_devolucion_programada").toLocalDate();

            long diasMora = 0;
            if (fechaDevolucion.isAfter(fechaDevolucionProgramada)) {
                diasMora = ChronoUnit.DAYS.between(fechaDevolucionProgramada, fechaDevolucion);
            }

            double moraDiaria = obtenerMoraDiariaPorRol(conn);

            double montoMora = diasMora * moraDiaria;

            if (diasMora > 0) {
                int respuesta = JOptionPane.showConfirmDialog(this,
                        "El préstamo tiene mora de $" + String.format("%.2f", montoMora) + ".\n¿Deseas pagar la mora ahora?",
                        "Mora detectada",
                        JOptionPane.YES_NO_OPTION);

                if (respuesta == JOptionPane.YES_OPTION) {
                    pagarMora(conn, idPrestamoStr, montoMora);
                } else {
                    actualizarEstadoMora(conn, idPrestamoStr, diasMora, montoMora);
                    JOptionPane.showMessageDialog(this, "El estado del préstamo permanece en Mora.\nLos días seguirán acumulándose.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    cargarPrestamos();
                    return;
                }
            }

            registrarDevolucionYActualizar(conn, idPrestamoStr, idDocumento, estadoDocumento, fechaDevolucion, diasMora, montoMora);
            JOptionPane.showMessageDialog(this, "Devolución registrada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarPrestamos();
            limpiarFormulario();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar devolución: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Registra el pago de mora y actualiza el estado del préstamo.
     *
     * @param conn           Conexión a la base de datos.
     * @param idPrestamoStr  ID del préstamo.
     * @param montoMora      Monto de la mora.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    private void pagarMora(Connection conn, String idPrestamoStr, double montoMora) throws SQLException {
        String registrarPagoQuery = "INSERT INTO pagos_mora (id_prestamo, monto_pagado, fecha_pago) VALUES (?, ?, NOW())";
        PreparedStatement registrarPagoStmt = conn.prepareStatement(registrarPagoQuery);
        registrarPagoStmt.setInt(1, Integer.parseInt(idPrestamoStr));
        registrarPagoStmt.setDouble(2, montoMora);
        registrarPagoStmt.executeUpdate();

        String actualizarPrestamoQuery = "UPDATE prestamos SET estado = 'Devuelto', dias_mora = 0, monto_mora = 0 WHERE id = ?";
        PreparedStatement actualizarPrestamoStmt = conn.prepareStatement(actualizarPrestamoQuery);
        actualizarPrestamoStmt.setInt(1, Integer.parseInt(idPrestamoStr));
        actualizarPrestamoStmt.executeUpdate();

        JOptionPane.showMessageDialog(this, "La mora de $" + String.format("%.2f", montoMora) + " ha sido pagada.\nEl préstamo se ha registrado como Devuelto.", "Pago exitoso", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Actualiza el estado del préstamo a 'Mora' y registra los días y monto de mora.
     *
     * @param conn           Conexión a la base de datos.
     * @param idPrestamoStr  ID del préstamo.
     * @param diasMora       Días de mora.
     * @param montoMora      Monto de la mora.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    private void actualizarEstadoMora(Connection conn, String idPrestamoStr, long diasMora, double montoMora) throws SQLException {
        String actualizarPrestamoQuery = "UPDATE prestamos SET estado = 'Mora', dias_mora = ?, monto_mora = ? WHERE id = ?";
        PreparedStatement actualizarPrestamoStmt = conn.prepareStatement(actualizarPrestamoQuery);
        actualizarPrestamoStmt.setLong(1, diasMora);
        actualizarPrestamoStmt.setDouble(2, montoMora);
        actualizarPrestamoStmt.setInt(3, Integer.parseInt(idPrestamoStr));
        actualizarPrestamoStmt.executeUpdate();
    }

    /**
     * Registra la devolución en las tablas correspondientes y actualiza la disponibilidad del documento.
     *
     * @param conn             Conexión a la base de datos.
     * @param idPrestamoStr    ID del préstamo.
     * @param idDocumento      ID del documento.
     * @param estadoDocumento  Estado del documento.
     * @param fechaDevolucion  Fecha real de devolución.
     * @param diasMora         Días de mora.
     * @param montoMora        Monto de la mora.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    private void registrarDevolucionYActualizar(Connection conn, String idPrestamoStr, String idDocumento, String estadoDocumento,
                                               LocalDate fechaDevolucion, long diasMora, double montoMora) throws SQLException {
        // Insertar en la tabla `devoluciones`
        String insertarDevolucionQuery = "INSERT INTO devoluciones (id_prestamo, id_usuario, id_documento, fecha_devolucion_real, dias_mora, monto_mora) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement insertarDevolucionStmt = conn.prepareStatement(insertarDevolucionQuery);
        insertarDevolucionStmt.setInt(1, Integer.parseInt(idPrestamoStr));
        insertarDevolucionStmt.setString(2, idUsuario);
        insertarDevolucionStmt.setString(3, idDocumento);
        insertarDevolucionStmt.setDate(4, java.sql.Date.valueOf(fechaDevolucion));
        insertarDevolucionStmt.setLong(5, diasMora);
        insertarDevolucionStmt.setDouble(6, montoMora);
        insertarDevolucionStmt.executeUpdate();

        // Registrar el historial del préstamo
        String registrarHistorialQuery = "INSERT INTO historial_prestamos (id_prestamo, id_usuario, id_documento, accion, descripcion) VALUES (?, ?, ?, 'Devolución', ?)";
        PreparedStatement registrarHistorialStmt = conn.prepareStatement(registrarHistorialQuery);
        registrarHistorialStmt.setInt(1, Integer.parseInt(idPrestamoStr));
        registrarHistorialStmt.setString(2, idUsuario);
        registrarHistorialStmt.setString(3, idDocumento);
        registrarHistorialStmt.setString(4, "Artículo devuelto correctamente.");
        registrarHistorialStmt.executeUpdate();

        // Actualizar estado del préstamo
        String actualizarPrestamoQuery = "UPDATE prestamos SET estado = 'Devuelto', dias_mora = ?, monto_mora = ? WHERE id = ?";
        PreparedStatement actualizarPrestamoStmt = conn.prepareStatement(actualizarPrestamoQuery);
        actualizarPrestamoStmt.setLong(1, diasMora);
        actualizarPrestamoStmt.setDouble(2, montoMora);
        actualizarPrestamoStmt.setInt(3, Integer.parseInt(idPrestamoStr));
        actualizarPrestamoStmt.executeUpdate();

        // Actualizar disponibilidad del documento
        // Asegúrate de actualizar la tabla correcta según el tipo de documento
        String actualizarDocumentoQuery = "UPDATE libros SET cantidad_disponible = cantidad_disponible + 1 WHERE id_libros = ?";
        PreparedStatement actualizarDocumentoStmt = conn.prepareStatement(actualizarDocumentoQuery);
        actualizarDocumentoStmt.setString(1, idDocumento);
        actualizarDocumentoStmt.executeUpdate();
    }

    /**
     * Obtiene la mora diaria asociada al rol del usuario.
     *
     * @param conn Conexión a la base de datos.
     * @return Mora diaria.
     * @throws SQLException Si ocurre un error en la consulta.
     */
    private double obtenerMoraDiariaPorRol(Connection conn) throws SQLException {
        String rolQuery = "SELECT rol FROM usuarios WHERE id = ?";
        PreparedStatement rolStmt = conn.prepareStatement(rolQuery);
        rolStmt.setString(1, idUsuario);
        ResultSet rs = rolStmt.executeQuery();

        if (rs.next()) {
            String rol = rs.getString("rol").toLowerCase();
            String moraQuery = "SELECT valor FROM configuraciones WHERE clave = ?";
            PreparedStatement moraStmt = conn.prepareStatement(moraQuery);
            moraStmt.setString(1, "mora_" + rol);
            ResultSet moraRs = moraStmt.executeQuery();

            if (moraRs.next()) {
                return moraRs.getDouble("valor");
            }
        }

        return 1.50; // Valor predeterminado si no se encuentra
    }

    /**
     * Crea una etiqueta estilizada.
     *
     * @param text Texto de la etiqueta.
     * @return JLabel estilizada.
     */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label;
    }

    /**
     * Crea un campo de texto estilizado.
     *
     * @return JTextField estilizado.
     */
    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        textField.setBackground(Color.WHITE);
        return textField;
    }

    /**
     * Crea un JComboBox estilizado.
     *
     * @param items Elementos del JComboBox.
     * @return JComboBox estilizado.
     */
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
    }

    /**
     * Crea un botón estilizado con colores por defecto y de hover.
     *
     * @param text         Texto del botón.
     * @param defaultColor Color de fondo por defecto.
     * @param hoverColor   Color de fondo al pasar el mouse.
     * @return JButton estilizado.
     */
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

    /**
     * Limpia los campos del formulario después de registrar una devolución.
     */
    private void limpiarFormulario() {
        idPrestamoField.setText("");
        idDocumentoField.setText("");
        fechaDevolucionField.setText("");
        estadoComboBox.setSelectedIndex(0);
        prestamosTable.clearSelection();
    }
}
