package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
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
import javax.swing.border.EmptyBorder;

public class RegistrarDevolucion extends JPanel {
    private JTable prestamosTable; // Tabla de préstamos
    private DefaultTableModel tableModel; // Modelo de la tabla
    private JTextField idPrestamoField, idDocumentoField; // Campos de texto
    private JComboBox<String> estadoComboBox; // ComboBox para estado
    private JButton registrarButton; // Botón para registrar devolución

    public RegistrarDevolucion() {
        setLayout(new BorderLayout(10, 10)); // Layout del panel
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Registrar Devolución",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        )); // Borde del panel

        // Configuración de la tabla de préstamos
        tableModel = new DefaultTableModel(new String[]{"ID Préstamo", "ID Documento", "Fecha Préstamo", "Estado"}, 0);
        prestamosTable = new JTable(tableModel);
        prestamosTable.setFont(new Font("Arial", Font.PLAIN, 14));
        prestamosTable.setRowHeight(25);
        prestamosTable.setFillsViewportHeight(true);
        prestamosTable.getTableHeader().setReorderingAllowed(false); // Evitar reordenar columnas

        JScrollPane scrollPane = new JScrollPane(prestamosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Préstamos Actuales",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        )); // Borde del scroll
        add(scrollPane, BorderLayout.CENTER); // Añadir scroll al centro

        // Configuración del formulario de devolución
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20)); // Espaciado
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Márgenes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campo ID Préstamo
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(createStyledLabel("ID Préstamo:"), gbc); // Añadir etiqueta

        idPrestamoField = createStyledTextField();
        idPrestamoField.setEditable(false); // No editable
        gbc.gridx = 1;
        formPanel.add(idPrestamoField, gbc); // Añadir campo

        // Campo ID Documento
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(createStyledLabel("ID Documento:"), gbc); // Añadir etiqueta

        idDocumentoField = createStyledTextField();
        idDocumentoField.setEditable(false); // No editable
        gbc.gridx = 1;
        formPanel.add(idDocumentoField, gbc); // Añadir campo

        // Campo Estado del Documento
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(createStyledLabel("Estado del Documento:"), gbc); // Añadir etiqueta

        estadoComboBox = createStyledComboBox(new String[]{"Bueno", "Dañado", "En Reparación"});
        gbc.gridx = 1;
        formPanel.add(estadoComboBox, gbc); // Añadir ComboBox

        // Botón Registrar Devolución
        registrarButton = createStyledButton("Registrar Devolución", new Color(34, 139, 34), new Color(0, 100, 0));
        registrarButton.addActionListener(e -> registrarDevolucion()); // Acción del botón
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(registrarButton, gbc); // Añadir botón

        add(formPanel, BorderLayout.SOUTH); // Añadir formulario al sur

        cargarPrestamos(); // Cargar préstamos al iniciar

        // Evento de selección en la tabla
        prestamosTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                int selectedRow = prestamosTable.getSelectedRow();
                if (selectedRow != -1) {
                    idPrestamoField.setText(prestamosTable.getValueAt(selectedRow, 0).toString()); // Set ID Préstamo
                    idDocumentoField.setText(prestamosTable.getValueAt(selectedRow, 1).toString()); // Set ID Documento
                }
            }
        });
    }

    private void cargarPrestamos() {
        try (Connection conn = ConexionBaseDatos.getConexion()) { // Conexión a BD
            String prestamosQuery =
                    "SELECT id AS id_prestamo, id_documento, fecha_prestamo, estado " +
                    "FROM prestamos " +
                    "WHERE estado != 'Devuelto'"; // Consulta de préstamos pendientes
            PreparedStatement stmt = conn.prepareStatement(prestamosQuery);
            ResultSet rs = stmt.executeQuery(); // Ejecutar consulta

            tableModel.setRowCount(0); // Limpiar tabla

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_prestamo"),
                        rs.getString("id_documento"),
                        rs.getDate("fecha_prestamo"),
                        rs.getString("estado")
                }); // Añadir fila
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No se encontraron préstamos pendientes.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) { // Manejar excepciones SQL
            JOptionPane.showMessageDialog(this, "Error al cargar los préstamos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarDevolucion() {
        String idPrestamo = idPrestamoField.getText().trim(); // Obtener ID Préstamo
        String idDocumento = idDocumentoField.getText().trim(); // Obtener ID Documento
        String estadoDocumento = (String) estadoComboBox.getSelectedItem(); // Obtener estado

        if (idPrestamo.isEmpty() || idDocumento.isEmpty()) { // Validar campos
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un préstamo para registrar la devolución.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) { // Conexión a BD
            // Obtener detalles del préstamo
            String detallesPrestamoQuery = "SELECT fecha_prestamo, fecha_devolucion, id_usuario FROM prestamos WHERE id = ?";
            PreparedStatement detallesStmt = conn.prepareStatement(detallesPrestamoQuery);
            detallesStmt.setString(1, idPrestamo);
            ResultSet detallesRs = detallesStmt.executeQuery(); // Ejecutar consulta

            if (!detallesRs.next()) { // Verificar existencia
                JOptionPane.showMessageDialog(this, "No se encontraron detalles del préstamo seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            java.sql.Date fechaPrestamoSql = detallesRs.getDate("fecha_prestamo");
            java.sql.Date fechaDevolucionSql = detallesRs.getDate("fecha_devolucion");
            String idUsuario = detallesRs.getString("id_usuario"); // Obtener ID Usuario

            LocalDate fechaPrestamo = fechaPrestamoSql.toLocalDate();
            LocalDate fechaDevolucion = fechaDevolucionSql.toLocalDate();
            LocalDate fechaActual = LocalDate.now();

            long diasPrestamo = ChronoUnit.DAYS.between(fechaPrestamo, fechaActual); // Calcular días prestado
            long diasMora = 0;
            if (fechaActual.isAfter(fechaDevolucion)) { // Calcular mora
                diasMora = ChronoUnit.DAYS.between(fechaDevolucion, fechaActual);
            }

            // Obtener mora diaria desde configuraciones
            double moraDiaria = obtenerMoraDiaria(conn); // Obtener mora diaria
            double montoMora = diasMora * moraDiaria; // Calcular monto mora

            String tipoDocumento = determinarTipoDocumento(idDocumento, conn); // Determinar tipo documento
            if (tipoDocumento == null) { // Verificar tipo
                JOptionPane.showMessageDialog(this, "No se pudo determinar el tipo de documento.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String tablaEspecifica = obtenerTablaEspecifica(tipoDocumento, conn); // Obtener tabla específica
            if (tablaEspecifica == null) { // Verificar tabla
                JOptionPane.showMessageDialog(this, "No se encontró la tabla específica para el tipo de documento seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Obtener detalles del libro
            String libroQuery = "SELECT titulo, autor, cantidad_total, cantidad_disponible FROM " + tablaEspecifica + " WHERE id_libros = ?";
            PreparedStatement libroStmt = conn.prepareStatement(libroQuery);
            libroStmt.setString(1, idDocumento);
            ResultSet libroRs = libroStmt.executeQuery(); // Ejecutar consulta

            if (!libroRs.next()) { // Verificar existencia libro
                JOptionPane.showMessageDialog(this, "No se encontró información del libro seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String tituloLibro = libroRs.getString("titulo");
            String autorLibro = libroRs.getString("autor");
            int cantidadTotal = libroRs.getInt("cantidad_total");
            int cantidadDisponible = libroRs.getInt("cantidad_disponible");

            // Actualizar cantidad_disponible, asegurando que no exceda cantidad_total
            int nuevaCantidadDisponible = cantidadDisponible + 1;
            if (nuevaCantidadDisponible > cantidadTotal) {
                nuevaCantidadDisponible = cantidadTotal;
            }

            // Actualizar la tabla específica del documento
            String actualizarDocumentoQuery = "UPDATE " + tablaEspecifica + " SET cantidad_disponible = ?, estado = ? WHERE id_libros = ?";
            PreparedStatement actualizarDocStmt = conn.prepareStatement(actualizarDocumentoQuery);
            actualizarDocStmt.setInt(1, nuevaCantidadDisponible);
            actualizarDocStmt.setString(2, estadoDocumento);
            actualizarDocStmt.setString(3, idDocumento);
            actualizarDocStmt.executeUpdate(); // Ejecutar actualización

            // Actualizar estado del préstamo
            String actualizarPrestamoQuery = "UPDATE prestamos SET estado = 'Devuelto', dias_mora = ?, monto_mora = ?, fecha_devolucion_real = ? WHERE id = ?";
            PreparedStatement actualizarPrestamoStmt = conn.prepareStatement(actualizarPrestamoQuery);
            actualizarPrestamoStmt.setLong(1, diasMora);
            actualizarPrestamoStmt.setDouble(2, montoMora);
            actualizarPrestamoStmt.setDate(3, java.sql.Date.valueOf(fechaActual));
            actualizarPrestamoStmt.setString(4, idPrestamo);
            actualizarPrestamoStmt.executeUpdate(); // Ejecutar actualización

            // Insertar registro en la tabla devoluciones
            String insertarDevolucionQuery = "INSERT INTO devoluciones (id_prestamo, id_usuario, id_documento, fecha_devolucion_real, dias_mora, monto_mora) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertarDevolucionStmt = conn.prepareStatement(insertarDevolucionQuery);
            insertarDevolucionStmt.setString(1, idPrestamo);
            insertarDevolucionStmt.setString(2, idUsuario);
            insertarDevolucionStmt.setString(3, idDocumento);
            insertarDevolucionStmt.setDate(4, java.sql.Date.valueOf(fechaActual));
            insertarDevolucionStmt.setLong(5, diasMora);
            insertarDevolucionStmt.setDouble(6, montoMora);
            insertarDevolucionStmt.executeUpdate(); // Ejecutar inserción

            // Insertar en historial_prestamos
            String insertarHistorialQuery = "INSERT INTO historial_prestamos (id_prestamo, id_usuario, id_documento, accion, descripcion) VALUES (?, ?, ?, 'Devolución', ?)";
            PreparedStatement insertarHistorialStmt = conn.prepareStatement(insertarHistorialQuery);
            insertarHistorialStmt.setString(1, idPrestamo);
            insertarHistorialStmt.setString(2, idUsuario);
            insertarHistorialStmt.setString(3, idDocumento);
            insertarHistorialStmt.setString(4, "Devolución del libro: " + tituloLibro);
            insertarHistorialStmt.executeUpdate(); // Ejecutar inserción

            // Insertar en auditoria
            String usuarioActual = obtenerNombreUsuario(idUsuario, conn); // Obtener nombre usuario
            String accion = "Registrar Devolución";
            String descripcion = "Usuario " + usuarioActual + " devolvió el libro ID: " + idDocumento + ", Estado: " + estadoDocumento;
            String insertarAuditoriaQuery = "INSERT INTO auditoria (usuario, accion, descripcion) VALUES (?, ?, ?)";
            PreparedStatement insertarAuditoriaStmt = conn.prepareStatement(insertarAuditoriaQuery);
            insertarAuditoriaStmt.setString(1, usuarioActual);
            insertarAuditoriaStmt.setString(2, accion);
            insertarAuditoriaStmt.setString(3, descripcion);
            insertarAuditoriaStmt.executeUpdate(); // Ejecutar inserción

            // Mostrar ventana de confirmación con detalles
            mostrarConfirmacionDevolucion(idUsuario, usuarioActual, idDocumento, tituloLibro, autorLibro, diasPrestamo, diasMora, montoMora, estadoDocumento);

            cargarPrestamos(); // Recargar préstamos
        } catch (SQLException ex) { // Manejar excepciones SQL
            JOptionPane.showMessageDialog(this, "Error al registrar devolución: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { // Manejar otras excepciones
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double obtenerMoraDiaria(Connection conn) throws SQLException {
        String query = "SELECT valor FROM configuraciones WHERE clave = 'mora_diaria'";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getDouble("valor"); // Retornar mora diaria
        }
        return 1.50; // Valor por defecto
    }

    private String determinarTipoDocumento(String idDocumento, Connection conn) throws SQLException {
    String query = "SELECT nombre FROM tipos_documentos WHERE nombre LIKE ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, idDocumento + "%");
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
        return rs.getString("nombre");
    }
    return null;
}

    private String obtenerTablaEspecifica(String tipoDocumento, Connection conexion) throws SQLException {
        String query = "SELECT nombre_tabla FROM tipos_documentos WHERE tipo = ?";
        PreparedStatement stmt = conexion.prepareStatement(query);
        stmt.setString(1, tipoDocumento);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString("nombre_tabla"); // Retornar tabla específica
        }
        return null;
    }

    private String obtenerNombreUsuario(String idUsuario, Connection conn) throws SQLException {
        String query = "SELECT nombre FROM usuarios WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, idUsuario);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString("nombre"); // Retornar nombre usuario
        }
        return "Desconocido"; // Valor por defecto
    }

    private void mostrarConfirmacionDevolucion(String idUsuario, String nombreUsuario, String idDocumento, String tituloLibro, String autorLibro, long diasPrestamo, long diasMora, double montoMora, String estadoDocumento) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Confirmación de Devolución", true); // Crear diálogo
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setLocationRelativeTo(this); // Posicionar ventana

        JPanel panelInfo = new JPanel(new GridLayout(9, 2, 10, 10)); // Panel de información
        panelInfo.setBorder(new EmptyBorder(20, 20, 20, 20));

        panelInfo.add(new JLabel("ID del Usuario:")); // Añadir etiqueta
        panelInfo.add(new JLabel(idUsuario)); // Añadir dato

        panelInfo.add(new JLabel("Nombre del Usuario:")); // Añadir etiqueta
        panelInfo.add(new JLabel(nombreUsuario)); // Añadir dato

        panelInfo.add(new JLabel("ID del Documento:")); // Añadir etiqueta
        panelInfo.add(new JLabel(idDocumento)); // Añadir dato

        panelInfo.add(new JLabel("Título del Libro:")); // Añadir etiqueta
        panelInfo.add(new JLabel(tituloLibro)); // Añadir dato

        panelInfo.add(new JLabel("Autor del Libro:")); // Añadir etiqueta
        panelInfo.add(new JLabel(autorLibro)); // Añadir dato

        panelInfo.add(new JLabel("Días de Préstamo:")); // Añadir etiqueta
        panelInfo.add(new JLabel(String.valueOf(diasPrestamo))); // Añadir dato

        panelInfo.add(new JLabel("Días de Mora:")); // Añadir etiqueta
        panelInfo.add(new JLabel(diasMora > 0 ? String.valueOf(diasMora) : "Sin Mora")); // Añadir dato

        panelInfo.add(new JLabel("Monto de Mora:")); // Añadir etiqueta
        panelInfo.add(new JLabel(diasMora > 0 ? String.format("$%.2f", montoMora) : "$0.00")); // Añadir dato

        panelInfo.add(new JLabel("Estado del Documento:")); // Añadir etiqueta
        panelInfo.add(new JLabel(estadoDocumento)); // Añadir dato

        dialog.add(panelInfo, BorderLayout.CENTER); // Añadir información al diálogo

        JPanel panelInferiorDialog = new JPanel(new BorderLayout()); // Panel inferior
        panelInferiorDialog.setBorder(new EmptyBorder(10, 20, 20, 20));

        JLabel mensaje = new JLabel("Devolución registrada exitosamente.", SwingConstants.CENTER); // Mensaje
        mensaje.setFont(new Font("Segoe UI", Font.BOLD, 16));
        mensaje.setForeground(new Color(0, 128, 0)); // Color verde

        JButton cerrarButton = new JButton("Cerrar"); // Botón cerrar
        cerrarButton.setBackground(new Color(0, 123, 255));
        cerrarButton.setForeground(Color.WHITE);
        cerrarButton.setFocusPainted(false);
        cerrarButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cerrarButton.addActionListener(e -> dialog.dispose()); // Acción cerrar

        panelInferiorDialog.add(mensaje, BorderLayout.NORTH); // Añadir mensaje
        panelInferiorDialog.add(cerrarButton, BorderLayout.SOUTH); // Añadir botón

        dialog.add(panelInferiorDialog, BorderLayout.SOUTH); // Añadir panel inferior al diálogo
        dialog.setVisible(true); // Mostrar diálogo
    }

    // Métodos auxiliares para crear componentes estilizados
    private JButton createStyledButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text); // Crear botón
        button.setFont(new Font("Arial", Font.BOLD, 14)); // Fuente
        button.setBackground(defaultColor); // Color fondo
        button.setForeground(Color.WHITE); // Color texto
        button.setFocusPainted(false); // Sin foco
        button.setPreferredSize(new Dimension(200, 40)); // Tamaño
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); // Borde
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor mano

        button.addMouseListener(new java.awt.event.MouseAdapter() { // Listener hover
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor); // Cambiar color al pasar
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(defaultColor); // Restaurar color
            }
        });
        return button; // Retornar botón
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text); // Crear etiqueta
        label.setFont(new Font("Arial", Font.BOLD, 14)); // Fuente
        label.setForeground(new Color(70, 130, 180)); // Color texto
        return label; // Retornar etiqueta
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(); // Crear campo de texto
        textField.setFont(new Font("Arial", Font.PLAIN, 14)); // Fuente
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1)); // Borde
        textField.setBackground(Color.WHITE); // Fondo
        return textField; // Retornar campo
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items); // Crear ComboBox
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14)); // Fuente
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1)); // Borde
        return comboBox; // Retornar ComboBox
    }
}
