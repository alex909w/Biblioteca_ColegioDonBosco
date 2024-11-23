package com.biblioteca.Panel.Prestamos;

import com.biblioteca.basedatos.ConexionBaseDatos;
import com.biblioteca.controller.PrestamoController;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.dao.DocumentoDAO;
import com.biblioteca.modelos.Prestamo;
import com.biblioteca.modelos.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.swing.table.TableCellRenderer;

// Clase para gestionar los préstamos.
public class GestionPrestamos extends JPanel {

    private String emailUsuario; // Almacenar el correo del usuario autenticado
    private String rolUsuario;   // Almacenar el rol del usuario autenticado
    private JTable tablaLibros;
    private DefaultTableModel tableModel;
    private JComboBox<String> tiposDocumentosComboBox;
    private JComboBox<Integer> diasPrestamoComboBox;
    private JComboBox<String> usuarioComboBox; // ComboBox para seleccionar usuario (solo para administradores)
    private JButton buscarButton;
    private JButton registrarButton;
    private JLabel infoLabel; // Etiqueta para mostrar el correo del usuario
    private JLabel rolLabel;  // Etiqueta para mostrar el rol del usuario

    private PrestamoController prestamoController;
    private UsuarioDAO usuarioDAO;
    private DocumentoDAO documentoDAO;
    private int diasPrestamo;

    public GestionPrestamos(String emailUsuario) {
        this.emailUsuario = emailUsuario;
        prestamoController = new PrestamoController();
        usuarioDAO = new UsuarioDAO();
        documentoDAO = new DocumentoDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 240, 240));

        // Panel Norte: Información del Usuario y Búsqueda de Documentos
        JPanel panelNorth = new JPanel();
        panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
        panelNorth.setBackground(new Color(240, 240, 240));

        // Obtener información del usuario autenticado
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        if (usuarioAutenticado != null) {
            rolUsuario = usuarioAutenticado.getRol();
        } else {
            rolUsuario = "Desconocido";
        }

        // Etiqueta superior: Información del Usuario
        infoLabel = createStyledLabel("Préstamos gestionados por: " + emailUsuario);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorth.add(infoLabel);

        // Etiqueta inferior: Rol del Usuario
        rolLabel = createStyledLabel("Rol del usuario: " + rolUsuario);
        rolLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorth.add(rolLabel);

        // Panel de Búsqueda de Tipo de Documento
        JPanel panelBusqueda = new JPanel();
        panelBusqueda.setLayout(new BoxLayout(panelBusqueda, BoxLayout.X_AXIS));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                "Buscar Documentos",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(70, 130, 180)
        ));
        panelBusqueda.setBackground(new Color(255, 255, 255));

        // Etiqueta Tipo de Documento
        JLabel tipoDocLabel = new JLabel("Tipo de Documento:");
        tipoDocLabel.setFont(new Font("Arial", Font.BOLD, 14));
        tipoDocLabel.setForeground(new Color(70, 130, 180));
        panelBusqueda.add(Box.createRigidArea(new Dimension(10, 0)));
        panelBusqueda.add(tipoDocLabel);
        panelBusqueda.add(Box.createRigidArea(new Dimension(10, 0)));

        // ComboBox Tipos de Documentos
        tiposDocumentosComboBox = new JComboBox<>();
        tiposDocumentosComboBox.setPreferredSize(new Dimension(200, 25));
        panelBusqueda.add(tiposDocumentosComboBox);
        panelBusqueda.add(Box.createRigidArea(new Dimension(20, 0)));

        // Botón Buscar Documentos
        buscarButton = new JButton("Buscar Documentos");
        buscarButton.setBackground(new Color(70, 130, 180));
        buscarButton.setForeground(Color.WHITE);
        buscarButton.setFocusPainted(false);
        buscarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buscarButton.setFont(new Font("Arial", Font.BOLD, 14));
        buscarButton.setPreferredSize(new Dimension(180, 30));
        panelBusqueda.add(buscarButton);
        panelBusqueda.add(Box.createRigidArea(new Dimension(10, 0)));

        panelNorth.add(Box.createRigidArea(new Dimension(0, 10)));
        panelNorth.add(panelBusqueda);
        panelNorth.add(Box.createRigidArea(new Dimension(0, 10)));

        // Si el usuario es Administrador, añadir el ComboBox de selección de usuario
        if (rolUsuario.equalsIgnoreCase("Administrador")) {
            JPanel panelSeleccionUsuario = new JPanel();
            panelSeleccionUsuario.setLayout(new BoxLayout(panelSeleccionUsuario, BoxLayout.X_AXIS));
            panelSeleccionUsuario.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                    "Asignar Préstamo a Usuario",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 14),
                    new Color(70, 130, 180)
            ));
            panelSeleccionUsuario.setBackground(new Color(255, 255, 255));

            JLabel usuarioLabel = new JLabel("Seleccione Usuario:");
            usuarioLabel.setFont(new Font("Arial", Font.BOLD, 14));
            usuarioLabel.setForeground(new Color(70, 130, 180));
            panelSeleccionUsuario.add(Box.createRigidArea(new Dimension(10, 0)));
            panelSeleccionUsuario.add(usuarioLabel);
            panelSeleccionUsuario.add(Box.createRigidArea(new Dimension(10, 0)));

            usuarioComboBox = new JComboBox<>();
            usuarioComboBox.setPreferredSize(new Dimension(200, 25));
            panelSeleccionUsuario.add(usuarioComboBox);
            panelSeleccionUsuario.add(Box.createRigidArea(new Dimension(10, 0)));

            // Cargar los usuarios en el ComboBox
            cargarUsuariosAdministradores();

            panelNorth.add(Box.createRigidArea(new Dimension(0, 10)));
            panelNorth.add(panelSeleccionUsuario);
            panelNorth.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        add(panelNorth, BorderLayout.NORTH);

        // Panel Central: Tabla de Documentos Disponibles
        tableModel = new DefaultTableModel();
        tablaLibros = new JTable(tableModel);
        tablaLibros.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaLibros.setRowHeight(25);
        tablaLibros.setFillsViewportHeight(true);
        tablaLibros.getTableHeader().setReorderingAllowed(false);

        // Personalizar el encabezado de la tabla
        tablaLibros.getTableHeader().setBackground(new Color(70, 130, 180));
        tablaLibros.getTableHeader().setForeground(Color.WHITE);
        tablaLibros.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        // Configurar selección de filas
        tablaLibros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Selección de una sola fila
        tablaLibros.setRowSelectionAllowed(true);
        tablaLibros.setColumnSelectionAllowed(false);
        tablaLibros.setSelectionBackground(new Color(70, 130, 180)); // Azul Steel
        tablaLibros.setSelectionForeground(Color.WHITE); // Texto en blanco cuando está seleccionado

        // Alternar colores de filas y mantener color de selección en azul
        tablaLibros.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color COLOR_BASE_PAR = new Color(245, 245, 245);
            private final Color COLOR_BASE_IMPAR = new Color(255, 255, 255);
            private final Color COLOR_SELECCION = new Color(70, 130, 180); // Azul Steel

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(COLOR_SELECCION);
                    c.setForeground(Color.WHITE); // Texto en blanco para mejor legibilidad
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(COLOR_BASE_PAR);
                    } else {
                        c.setBackground(COLOR_BASE_IMPAR);
                    }
                    c.setForeground(Color.BLACK); // Texto en negro para filas no seleccionadas
                }

                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Arial", Font.PLAIN, 14));
                return c;
            }
        });

        // Configurar el TableRowSorter para permitir la ordenación de columnas
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        tablaLibros.setRowSorter(sorter);

        // Opcional: Deshabilitar la ordenación para columnas específicas si es necesario
        // Por ejemplo, deshabilitar la ordenación para la columna 'Sinopsis' si es muy larga
        int sinopsisIndex = getColumnIndex("Sinopsis");
        if (sinopsisIndex != -1) {
            sorter.setSortable(sinopsisIndex, false);
        }

        // Asegurar que no haya ninguna ordenación aplicada inicialmente
        sorter.setSortsOnUpdates(true);
        sorter.setSortKeys(null); // No aplicar ninguna ordenación por defecto

        JScrollPane scrollPane = new JScrollPane(tablaLibros);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Documentos Disponibles",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        add(scrollPane, BorderLayout.CENTER);

        // Panel Inferior: Registro de Préstamos
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.X_AXIS));
        panelInferior.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                "Registrar Préstamo",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(70, 130, 180)
        ));
        panelInferior.setBackground(new Color(255, 255, 255));
        panelInferior.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Etiqueta Días de Préstamo
        JLabel diasLabel = new JLabel("Días de Préstamo:");
        diasLabel.setFont(new Font("Arial", Font.BOLD, 14));
        diasLabel.setForeground(new Color(70, 130, 180));
        diasLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        panelInferior.add(Box.createRigidArea(new Dimension(10, 0)));
        panelInferior.add(diasLabel);
        panelInferior.add(Box.createRigidArea(new Dimension(10, 0)));

        // ComboBox Días de Préstamo
        diasPrestamoComboBox = new JComboBox<>();
        diasPrestamoComboBox.setPreferredSize(new Dimension(100, 25));
        diasPrestamoComboBox.setMaximumSize(new Dimension(150, 25));
        panelInferior.add(diasPrestamoComboBox);
        panelInferior.add(Box.createRigidArea(new Dimension(20, 0)));

        // Botón Registrar Préstamo
        registrarButton = new JButton("Registrar Préstamo");
        registrarButton.setBackground(new Color(34, 139, 34));
        registrarButton.setForeground(Color.WHITE);
        registrarButton.setFocusPainted(false);
        registrarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registrarButton.setFont(new Font("Arial", Font.BOLD, 14));
        registrarButton.setPreferredSize(new Dimension(180, 30));
        panelInferior.add(registrarButton);
        panelInferior.add(Box.createRigidArea(new Dimension(10, 0)));

        add(panelInferior, BorderLayout.SOUTH);

        // Cargar tipos de documentos y días de préstamo según el rol
        cargarTablasDesdeTiposDocumentos();
        cargarDiasPrestamoPorRol(usuarioAutenticado != null ? usuarioAutenticado.getRol().toLowerCase() : "desconocido");

        // Eventos
        buscarButton.addActionListener(e -> buscarLibros());
        registrarButton.addActionListener(e -> registrarPrestamo(usuarioAutenticado));
    }

    // Obtiene el usuario autenticado basado en el correo electrónico.
    private Usuario obtenerUsuarioAutenticado() {
        try {
            return usuarioDAO.obtenerUsuarioPorEmail(emailUsuario);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener usuario autenticado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Carga los usuarios en el ComboBox para administradores.
    private void cargarUsuariosAdministradores() {
        if (!rolUsuario.equalsIgnoreCase("Administrador")) {
            return;
        }

        try {
            List<Usuario> usuarios = usuarioDAO.obtenerTodosLosUsuarios();
            usuarioComboBox.addItem("Seleccione un usuario");
            for (Usuario usuario : usuarios) {
                usuarioComboBox.addItem(usuario.getEmail());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Carga los días de préstamo permitidos según el rol del usuario.
    private void cargarDiasPrestamoPorRol(String rolUsuario) {
    try {
        int limiteDias = prestamoController.obtenerLimitePrestamosPorRol(rolUsuario);
        diasPrestamoComboBox.removeAllItems();
        for (int i = 1; i <= limiteDias; i++) {
            diasPrestamoComboBox.addItem(i);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar días de préstamo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    // Carga los tipos de documentos disponibles desde la base de datos.
    private void cargarTablasDesdeTiposDocumentos() {
        try {
            List<String> tiposDocumentos = documentoDAO.obtenerNombresTablas();
            for (String tipo : tiposDocumentos) {
                tiposDocumentosComboBox.addItem(tipo);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar tipos de documentos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Busca y carga los documentos disponibles del tipo seleccionado.
    private void buscarLibros() {
        String tipoDocumento = (String) tiposDocumentosComboBox.getSelectedItem();

        if (tipoDocumento == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un tipo de documento.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Obtener el orden de las columnas desde la base de datos
            List<String> columnasOrdenadas = obtenerOrdenColumnasDesdeBD(tipoDocumento);

            // Obtener los datos de la tabla seleccionada
            List<Map<String, Object>> documentos = documentoDAO.obtenerTodosLosDocumentos(tipoDocumento);

            // Obtener el modelo de la tabla y limpiarlo antes de insertar nuevos datos
            DefaultTableModel modeloTabla = (DefaultTableModel) tablaLibros.getModel();
            modeloTabla.setRowCount(0); // Limpiar filas
            modeloTabla.setColumnCount(0); // Limpiar columnas

            if (!documentos.isEmpty()) {
                // Añadir las columnas al modelo en el orden obtenido de la base de datos
                for (String columna : columnasOrdenadas) {
                    modeloTabla.addColumn(columna);
                }

                // Añadir las filas respetando el orden de las columnas
                for (Map<String, Object> doc : documentos) {
                    Object[] rowData = columnasOrdenadas.stream().map(doc::get).toArray();
                    modeloTabla.addRow(rowData);
                }

                // Ajustar el ancho de las columnas según el contenido
                ajustarAnchoColumnas(tablaLibros);
            } else {
                JOptionPane.showMessageDialog(this, "No hay documentos disponibles para el tipo seleccionado.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos de la tabla seleccionada: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Registra un préstamo para el usuario seleccionado (administrador) o para sí mismo (usuario regular).

    private void registrarPrestamo(Usuario usuarioAutenticado) {
        int filaSeleccionada = tablaLibros.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un documento para registrar el préstamo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tipoDocumento = (String) tiposDocumentosComboBox.getSelectedItem();
        if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un tipo de documento válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idUsuarioDestino = usuarioAutenticado.getId(); // Por defecto, asignar a sí mismo

        // Si el usuario es administrador, obtener el usuario seleccionado en el ComboBox
        if (rolUsuario.equalsIgnoreCase("Administrador")) {
            String usuarioSeleccionado = (String) usuarioComboBox.getSelectedItem();
            if (usuarioSeleccionado == null || usuarioSeleccionado.equals("Seleccione un usuario")) {
                JOptionPane.showMessageDialog(this, "Seleccione un usuario al cual asignar el préstamo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Usuario usuarioDestino = usuarioDAO.obtenerUsuarioPorEmail(usuarioSeleccionado);
                if (usuarioDestino == null) {
                    JOptionPane.showMessageDialog(this, "El usuario seleccionado no existe.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                idUsuarioDestino = usuarioDestino.getId();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al obtener información del usuario seleccionado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            // Validar si el usuario destino tiene mora pendiente
            if (tieneMoraPendiente(idUsuarioDestino)) {
                JOptionPane.showMessageDialog(this, "El usuario tiene mora pendiente y no puede realizar más préstamos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Obtén el nombre dinámico del ID
            String nombreColumnaID = obtenerNombreColumnaID(tipoDocumento);
            if (nombreColumnaID == null || nombreColumnaID.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontró un ID válido para el tipo de documento seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Obtén el valor del ID del documento
            String idDocumento = tablaLibros.getValueAt(filaSeleccionada, getColumnIndex(nombreColumnaID)).toString();
            String cantidadDisponibleStr = tablaLibros.getValueAt(filaSeleccionada, getColumnIndex("cantidad_disponible")).toString();
            int cantidadDisponible = Integer.parseInt(cantidadDisponibleStr);

            if (cantidadDisponible <= 0) {
                JOptionPane.showMessageDialog(this, "No hay suficientes copias disponibles para realizar el préstamo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int diasPrestamoSeleccionados = (int) diasPrestamoComboBox.getSelectedItem();

            // Validar límite de préstamos por rol
            int limitePrestamos = prestamoController.obtenerLimitePrestamosPorRol(rolUsuario.toLowerCase());
            boolean puedePrestar = prestamoController.validarLimitePrestamos(idUsuarioDestino, limitePrestamos);
            if (!puedePrestar) {
                JOptionPane.showMessageDialog(this, "El usuario ha alcanzado el límite de préstamos permitidos para su rol.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Obtener mora diaria
            double moraDiaria = prestamoController.obtenerMoraDiariaPorRol(rolUsuario.toLowerCase());

            // Registrar el préstamo
            Prestamo prestamo = new Prestamo();
            prestamo.setIdUsuario(idUsuarioDestino);
            prestamo.setIdDocumento(idDocumento);
            prestamo.setMoraDiaria(moraDiaria);
            prestamo.setDiasMora(diasPrestamoSeleccionados); // Usando diasMora para almacenar los días de préstamo

            prestamoController.registrarPrestamo(prestamo);

            // Actualizar cantidad disponible
            prestamoController.actualizarDisponibilidadDocumento(tipoDocumento, idDocumento, -1);

            JOptionPane.showMessageDialog(this, "Préstamo registrado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // Recargar la tabla para reflejar los cambios
            buscarLibros();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La cantidad disponible no es válida. Verifique los datos del documento.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar préstamo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Obtiene el índice de una columna en la tabla basada en su nombre.
    private int getColumnIndex(String columnName) {
        for (int i = 0; i < tablaLibros.getColumnCount(); i++) {
            if (tablaLibros.getColumnName(i).equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    // Ajusta el ancho de las columnas de una tabla según el contenido.
    private void ajustarAnchoColumnas(JTable table) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 15; // Valor por defecto
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 1, width);
            }
            if (width > 300)
                width = 300;
            table.getColumnModel().getColumn(column).setPreferredWidth(width);
        }
    }

    //Crea una etiqueta con estilo personalizado.
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180));
        return label;
    }

    //Obtiene el nombre de la columna ID de una tabla específica.
    private String obtenerNombreColumnaID(String tabla) throws SQLException {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME LIKE 'id_%'";
        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, tabla);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("COLUMN_NAME");
                }
            }
        }
        throw new SQLException("No se encontró ninguna columna que comience con 'id_' en la tabla: " + tabla);
    }

    // Obtiene el orden de las columnas desde la base de datos.
    private List<String> obtenerOrdenColumnasDesdeBD(String tabla) throws SQLException {
        List<String> columnasOrdenadas = new ArrayList<>();
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? ORDER BY ORDINAL_POSITION";

        try (Connection conexion = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, tabla);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    columnasOrdenadas.add(rs.getString("COLUMN_NAME"));
                }
            }
        }

        return columnasOrdenadas;
    }

    // Verifica si el usuario tiene mora pendiente.
    private boolean tieneMoraPendiente(String idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) AS moraPendiente " +
                     "FROM prestamos " +
                     "WHERE id_usuario = ? AND estado = 'Mora'";

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int moraPendiente = rs.getInt("moraPendiente");
                    return moraPendiente > 0; // Retorna true si hay mora pendiente
                }
            }
        }
        return false;
    }
}
