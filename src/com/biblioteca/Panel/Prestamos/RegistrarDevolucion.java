package com.biblioteca.Panel.Prestamos;

import com.biblioteca.basedatos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class RegistrarDevolucion extends JPanel {
    private String emailAdministrador; 
    private String idUsuario;    
    private JTable prestamosTable;
    private DefaultTableModel tableModel;
    private JTextField idPrestamoField, idDocumentoField, fechaDevolucionField;
    private JComboBox<String> estadoComboBox;
    private JButton registrarButton;
    private JLabel correoUsuarioLabel; 
    private JTextField correoBusquedaField;
    private JButton buscarButton;

    public RegistrarDevolucion(String emailAdministrador) {
        this.emailAdministrador = emailAdministrador; 
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Registrar Devolución",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel Superior: Información del Administrador y Búsqueda de Usuario
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(new Color(255, 255, 255));
        panelSuperior.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Información del Administrador
        JPanel infoAdminPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoAdminPanel.setBackground(new Color(255, 255, 255));
        JLabel adminLabel = createStyledLabel("Administrador: " + this.emailAdministrador);
        infoAdminPanel.add(adminLabel);
        panelSuperior.add(infoAdminPanel, BorderLayout.NORTH);

        // Panel de Búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.setBackground(new Color(255, 255, 255));
        panelBusqueda.setBorder(new EmptyBorder(10, 0, 10, 0));

        correoBusquedaField = createStyledTextField();
        correoBusquedaField.setPreferredSize(new Dimension(250, 30));
        panelBusqueda.add(createStyledLabel("Buscar Usuario por Correo:"));
        panelBusqueda.add(correoBusquedaField);

        buscarButton = createStyledButton("Buscar", new Color(70, 130, 180), new Color(0, 100, 180));
        panelBusqueda.add(buscarButton);

        panelSuperior.add(panelBusqueda, BorderLayout.SOUTH);

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

        // Listener para el botón "Buscar"
        buscarButton.addActionListener(e -> {
            String correo = correoBusquedaField.getText().trim();
            if (correo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, ingresa un correo para buscar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            buscarUsuarioYPrestamos(correo);
        });

        // Listener para la selección de filas en la tabla
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

    // Método para buscar usuario y cargar sus préstamos
    private void buscarUsuarioYPrestamos(String correo) {
        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Buscar el ID del usuario por correo
            String usuarioQuery = "SELECT id FROM usuarios WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(usuarioQuery);
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                idUsuario = rs.getString("id");
                correoUsuarioLabel = updateCorreoUsuarioLabel("Usuario: " + correo);
                cargarPrestamos(); // Cargar préstamos del usuario
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró un usuario con el correo proporcionado.", "Error", JOptionPane.ERROR_MESSAGE);
                tableModel.setRowCount(0); // Limpiar tabla si no hay resultados
                limpiarFormulario();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar el usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Actualiza o crea la etiqueta de usuario en el panel superior
    private JLabel updateCorreoUsuarioLabel(String texto) {
        if (correoUsuarioLabel == null) {
            correoUsuarioLabel = createStyledLabel(texto);
            JPanel panelSuperior = (JPanel) getComponent(0);
            JPanel infoAdminPanel = (JPanel) panelSuperior.getComponent(0);
            infoAdminPanel.add(correoUsuarioLabel);
        } else {
            correoUsuarioLabel.setText(texto);
        }
        return correoUsuarioLabel;
    }

    // Carga los préstamos actuales del usuario autenticado o del usuario buscado que no han sido devueltos.
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
                JOptionPane.showMessageDialog(this, "El usuario no tiene préstamos pendientes.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar los préstamos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Registra la devolución de un préstamo seleccionado.
    private void registrarDevolucion() {
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, busca y selecciona un usuario válido antes de registrar una devolución.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

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

        // Validar que la fecha no sea anterior a la actual
        if (fechaDevolucion.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "La fecha de devolución no puede ser anterior a la fecha actual.", "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "No se encontraron detalles del préstamo o no pertenece al usuario seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Manejar caso donde `fecha_devolucion_programada` sea nula
            Date fechaProgramadaSQL = detallesRs.getDate("fecha_devolucion_programada");
            LocalDate fechaDevolucionProgramada = (fechaProgramadaSQL != null) ? fechaProgramadaSQL.toLocalDate() : null;

            long diasMora = 0;
            if (fechaDevolucionProgramada != null && fechaDevolucion.isAfter(fechaDevolucionProgramada)) {
                diasMora = ChronoUnit.DAYS.between(fechaDevolucionProgramada, fechaDevolucion);

                // Calcular la mora
                double moraDiaria = obtenerMoraDiariaPorRol(conn);
                double montoMora = calcularMora(diasMora, moraDiaria);

                // Mostrar mensaje de mora acumulada
                int respuesta = JOptionPane.showConfirmDialog(this,
                        "El préstamo tiene mora acumulada.\n" +
                        "Días de retraso: " + diasMora + "\n" +
                        "Mora diaria: $" + String.format("%.2f", moraDiaria) + "\n" +
                        "Total de mora: $" + String.format("%.2f", montoMora) + "\n\n" +
                        "¿Deseas pagar la mora ahora?",
                        "Mora detectada", JOptionPane.YES_NO_OPTION);

                if (respuesta == JOptionPane.YES_OPTION) {
                    // Si el administrador decide pagar la mora
                    pagarMora(conn, idPrestamoStr, montoMora);
                    registrarDevolucionYActualizar(conn, idPrestamoStr, idDocumento, estadoDocumento, fechaDevolucion, diasMora, 0);
                    JOptionPane.showMessageDialog(this, "Devolución registrada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Si el administrador decide no pagar la mora
                    actualizarEstadoMora(conn, idPrestamoStr, diasMora, montoMora);
                    JOptionPane.showMessageDialog(this, "La mora seguirá acumulándose y el estado del préstamo se ha actualizado a 'En Mora'.", "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                // Si no hay mora, registrar la devolución directamente
                registrarDevolucionYActualizar(conn, idPrestamoStr, idDocumento, estadoDocumento, fechaDevolucion, 0, 0);
                JOptionPane.showMessageDialog(this, "Devolución registrada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }

            cargarPrestamos();
            limpiarFormulario();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar devolución: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Calcula la mora acumulada según los días de retraso y la mora diaria.
    private double calcularMora(long diasMora, double moraDiaria) {
        return diasMora * moraDiaria;
    }

    // Registra el pago de mora y actualiza el estado del préstamo.
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

    // Actualiza el estado del préstamo a 'Mora' con los días y monto de mora.
    private void actualizarEstadoMora(Connection conn, String idPrestamoStr, long diasMora, double montoMora) throws SQLException {
        String actualizarPrestamoQuery = "UPDATE prestamos SET estado = 'Mora', dias_mora = ?, monto_mora = ? WHERE id = ?";
        PreparedStatement actualizarPrestamoStmt = conn.prepareStatement(actualizarPrestamoQuery);
        actualizarPrestamoStmt.setLong(1, diasMora);
        actualizarPrestamoStmt.setDouble(2, montoMora);
        actualizarPrestamoStmt.setInt(3, Integer.parseInt(idPrestamoStr));
        actualizarPrestamoStmt.executeUpdate();
    }

    // Registra la devolución y actualiza las tablas correspondientes.
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
        String actualizarDocumentoQuery = "UPDATE libros SET cantidad_disponible = cantidad_disponible + 1 WHERE id_libros = ?";
        PreparedStatement actualizarDocumentoStmt = conn.prepareStatement(actualizarDocumentoQuery);
        actualizarDocumentoStmt.setString(1, idDocumento);
        actualizarDocumentoStmt.executeUpdate();
    }

    // Obtiene la mora diaria basada en el rol del usuario.
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

        return 1.50; 
    }

    // Crea una etiqueta estilizada.
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); 
        return label;
    }

    // Crea un campo de texto estilizado.
    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        textField.setBackground(Color.WHITE);
        return textField;
    }

    // Crea un combobox estilizado.
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
    }

    // Crea un botón estilizado con efectos de hover.
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

    // Limpia los campos del formulario después de registrar una devolución.
    private void limpiarFormulario() {
        idPrestamoField.setText("");
        idDocumentoField.setText("");
        fechaDevolucionField.setText("");
        estadoComboBox.setSelectedIndex(0);
        prestamosTable.clearSelection();
    }
}
