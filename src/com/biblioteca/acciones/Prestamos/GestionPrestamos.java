package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class GestionPrestamos extends JPanel {

    private String emailUsuario; // Almacenar el correo del usuario autenticado
    private JTable tablaLibros;
    private JComboBox<String> tiposDocumentosComboBox;
    private JComboBox<Integer> diasPrestamoComboBox;
    private JButton buscarButton;
    private JButton registrarButton;
    private JLabel infoLabel; // Etiqueta para mostrar el correo del usuario
    private JLabel rolLabel;  // Etiqueta para mostrar el rol del usuario


    public GestionPrestamos(String emailUsuario) {
        this.emailUsuario = emailUsuario; // Asignar el correo recibido
        setLayout(new BorderLayout(15, 15)); // Margen entre componentes

        // Estilo general
        setBackground(new Color(240, 240, 240)); // Fondo claro

        // Contenedor para las etiquetas de información y el panel superior
        JPanel panelNorth = new JPanel();
        panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
        panelNorth.setBackground(new Color(240, 240, 240));

        // Obtener información del usuario autenticado
        String rolUsuario = obtenerRolUsuarioAutenticado();

        // Etiqueta superior: Información del Usuario
        infoLabel = createStyledLabel("Préstamos gestionados por: " + emailUsuario);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorth.add(infoLabel);

        // Etiqueta inferior: Rol del Usuario
        rolLabel = createStyledLabel("Rol del usuario: " + rolUsuario);
        rolLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorth.add(rolLabel);

        // Panel Superior: Búsqueda de Tipo de Documento
        JPanel panelSuperior = new JPanel(new GridLayout(1, 1, 10, 10)); // Grid para alineación
        panelSuperior.setBorder(new EmptyBorder(10, 10, 10, 10)); // Espacio alrededor
        panelSuperior.setBackground(new Color(255, 255, 255)); // Fondo blanco

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila1.setBackground(new Color(255, 255, 255));
        buscarButton = new JButton("Buscar Documentos");
        tiposDocumentosComboBox = new JComboBox<>();

        fila1.add(new JLabel("Tipo de Documento:"));
        fila1.add(tiposDocumentosComboBox);
        fila1.add(buscarButton);

        panelSuperior.add(fila1);
        panelNorth.add(panelSuperior);

        add(panelNorth, BorderLayout.NORTH);

        // Panel Central: Tabla de Documentos Disponibles
        String[] columnas = {"ID", "Título", "Autor", "Cantidad Disponible"};
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            // Hacer que las celdas no sean editables
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaLibros = new JTable(modeloTabla);

        tablaLibros.setFillsViewportHeight(true); // Rellenar todo el área visible
        tablaLibros.setRowHeight(30); // Altura de filas
        tablaLibros.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaLibros.getTableHeader().setBackground(new Color(200, 200, 200));
        tablaLibros.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(tablaLibros);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        add(scrollPane, BorderLayout.CENTER);

        // Panel Inferior: Registro de Préstamos
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelInferior.setBackground(new Color(255, 255, 255));

        diasPrestamoComboBox = new JComboBox<>();
        registrarButton = new JButton("Registrar Préstamo");

        registrarButton.setBackground(new Color(0, 123, 255));
        registrarButton.setForeground(Color.WHITE);
        registrarButton.setFocusPainted(false);
        registrarButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        panelInferior.add(new JLabel("Días de Préstamo:"));
        panelInferior.add(diasPrestamoComboBox);
        panelInferior.add(registrarButton);

        add(panelInferior, BorderLayout.SOUTH);

        // Cargar nombres de tablas desde tipos_documentos
        cargarTablasDesdeTiposDocumentos();
        cargarDiasPrestamoPorRol(); // Cargar días de préstamo según el rol

        // Eventos
        buscarButton.addActionListener(e -> buscarLibros());
        registrarButton.addActionListener(e -> registrarPrestamo());
    }

   private void cargarDiasPrestamoPorRol() {
    String rolUsuario = obtenerRolUsuarioAutenticado();
    if (rolUsuario == null || rolUsuario.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "No se pudo obtener el rol del usuario.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    rolUsuario = rolUsuario.toLowerCase();

    try (Connection conexion = ConexionBaseDatos.getConexion()) {
        String query = "SELECT valor FROM configuraciones WHERE clave = ?";
        PreparedStatement stmt = conexion.prepareStatement(query);
        stmt.setString(1, "limite_dias_" + rolUsuario);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int limiteDias = rs.getInt("valor");
            diasPrestamoComboBox.removeAllItems();
            for (int i = 1; i <= limiteDias; i++) {
                diasPrestamoComboBox.addItem(i);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró configuración de límite de días para el rol: " + rolUsuario, "Advertencia", JOptionPane.WARNING_MESSAGE);
            // Opcional: Agregar un valor predeterminado
            diasPrestamoComboBox.removeAllItems();
            diasPrestamoComboBox.addItem(7); // Valor por defecto
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar días de préstamo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    
    private String obtenerRolUsuarioAutenticado() {
        String rol = "Desconocido"; // Valor por defecto
        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            String rolQuery = "SELECT rol FROM usuarios WHERE email = ?";
            PreparedStatement rolStmt = conexion.prepareStatement(rolQuery);
            rolStmt.setString(1, emailUsuario);
            ResultSet rs = rolStmt.executeQuery();

            if (rs.next()) {
                rol = rs.getString("rol");
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el rol del usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener el rol del usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return rol;
    }

    /**
     * Carga los nombres de las tablas dinámicas desde la tabla tipos_documentos.
     */
    private void cargarTablasDesdeTiposDocumentos() {
        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            // Obtener los nombres de las tablas registradas en la tabla `tipos_documentos`
            String sql = "SELECT nombre FROM tipos_documentos";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tiposDocumentosComboBox.addItem(rs.getString("nombre"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar tipos de documentos: " + e.getMessage());
        }
    }

    /**
     * Busca y muestra los libros disponibles según el tipo de documento seleccionado.
     */
    private void buscarLibros() {
        String tipoDocumento = (String) tiposDocumentosComboBox.getSelectedItem();

        // Validar que se haya seleccionado una tabla
        if (tipoDocumento == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una tabla de tipo de documento.");
            return;
        }

        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            // Verificar que el correo exista en la tabla de usuarios
            String usuarioQuery = "SELECT id FROM usuarios WHERE email = ?";
            PreparedStatement usuarioStmt = conexion.prepareStatement(usuarioQuery);
            usuarioStmt.setString(1, emailUsuario);
            ResultSet usuarioRs = usuarioStmt.executeQuery();

            if (!usuarioRs.next()) {
                JOptionPane.showMessageDialog(this, "El usuario no está registrado.");
                return;
            }

            // Cargar todos los datos de la tabla seleccionada
            String sql = "SELECT * FROM " + tipoDocumento;
            PreparedStatement stmt = conexion.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Obtener el modelo de la tabla y limpiarlo antes de insertar nuevos datos
            DefaultTableModel modeloTabla = (DefaultTableModel) tablaLibros.getModel();
            modeloTabla.setRowCount(0);

            // Leer los metadatos para manejar dinámicamente las columnas de la tabla
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Establecer los nombres de las columnas en la tabla
            String[] columnas = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columnas[i - 1] = metaData.getColumnName(i);
            }
            modeloTabla.setColumnIdentifiers(columnas);

            // Agregar las filas desde el resultado
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                modeloTabla.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos de la tabla seleccionada: " + e.getMessage());
        }
    }

    /**
     * Obtiene el índice de una columna en la tabla basada en el nombre de la columna.
     *
     * @param columnName Nombre de la columna.
     * @return Índice de la columna o -1 si no se encuentra.
     */
    private int getColumnIndex(String columnName) {
        for (int i = 0; i < tablaLibros.getColumnCount(); i++) {
            if (tablaLibros.getColumnName(i).equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1; // Retorna -1 si la columna no se encuentra
    }

    
   private void registrarPrestamo() {
    int filaSeleccionada = tablaLibros.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Seleccione un documento para registrar el préstamo.");
        return;
    }

    String tipoDocumento = (String) tiposDocumentosComboBox.getSelectedItem();
    if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Seleccione un tipo de documento válido.");
        return;
    }

    // Obtener el índice de la columna "Cantidad Disponible" o "cantidad_disponible"
    int cantidadDisponibleIndex = getColumnIndex("Cantidad Disponible");
    if (cantidadDisponibleIndex == -1) {
        cantidadDisponibleIndex = getColumnIndex("cantidad_disponible"); // Intentar con minúsculas
        if (cantidadDisponibleIndex == -1) {
            JOptionPane.showMessageDialog(this, "La columna 'Cantidad Disponible' no se encontró en la tabla.");
            return;
        }
    }

    try {
        // Obtener el correo del usuario autenticado
        String correo = emailUsuario;
        if (correo == null || correo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se pudo obtener la información del usuario de la sesión.");
            return;
        }

        // Obtener valores de la fila seleccionada
        String idDocumento = tablaLibros.getValueAt(filaSeleccionada, 0).toString(); // Asegúrate que "ID" está en la columna 0
        String cantidadDisponibleStr = tablaLibros.getValueAt(filaSeleccionada, cantidadDisponibleIndex).toString();
        int cantidadDisponible = Integer.parseInt(cantidadDisponibleStr);

        if (cantidadDisponible <= 0) {
            JOptionPane.showMessageDialog(this, "No hay suficientes copias disponibles para realizar el préstamo.");
            return;
        }

        int diasPrestamo = (int) diasPrestamoComboBox.getSelectedItem();

        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            // Validar correo del usuario y obtener su ID y rol
            String usuarioQuery = "SELECT id, rol FROM usuarios WHERE email = ?";
            PreparedStatement usuarioStmt = conexion.prepareStatement(usuarioQuery);
            usuarioStmt.setString(1, correo);
            ResultSet usuarioRs = usuarioStmt.executeQuery();

            if (!usuarioRs.next()) {
                JOptionPane.showMessageDialog(this, "El correo ingresado no pertenece a un usuario registrado.");
                return;
            }

            String idUsuario = usuarioRs.getString("id");
            String rolUsuario = usuarioRs.getString("rol").toLowerCase();

            // Validar límite de préstamos por rol
            if (!validarLimitePrestamos(idUsuario, conexion)) {
                JOptionPane.showMessageDialog(this, "El usuario ha alcanzado el límite de préstamos permitidos para su rol.");
                return;
            }

            // Obtener la mora diaria para el rol
            double moraDiaria = obtenerMoraDiariaPorRol(idUsuario, conexion);

            // Insertar el préstamo en la tabla `prestamos` con mora_diaria
            String prestamoQuery = "INSERT INTO prestamos (id_usuario, id_documento, mora_diaria, fecha_prestamo, fecha_devolucion, estado, fecha_devolucion_programada) " +
                    "VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY), 'Pendiente', DATE_ADD(CURDATE(), INTERVAL ? DAY))";
            PreparedStatement prestamoStmt = conexion.prepareStatement(prestamoQuery);
            prestamoStmt.setString(1, idUsuario);
            prestamoStmt.setString(2, idDocumento);
            prestamoStmt.setDouble(3, moraDiaria);
            prestamoStmt.setInt(4, diasPrestamo);
            prestamoStmt.setInt(5, diasPrestamo);
            prestamoStmt.executeUpdate();

            // Actualizar la cantidad disponible en la tabla dinámica
            String actualizarDisponibilidadQuery = "UPDATE " + tipoDocumento + " SET cantidad_disponible = cantidad_disponible - 1 WHERE id_libros = ?";
            PreparedStatement actualizarStmt = conexion.prepareStatement(actualizarDisponibilidadQuery);
            actualizarStmt.setString(1, idDocumento);
            actualizarStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Préstamo registrado exitosamente.");

            // Recargar la tabla para reflejar los cambios
            buscarLibros();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar préstamo: " + e.getMessage());
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "La cantidad disponible no es válida. Verifique los datos del documento.");
    }
}


   private boolean validarLimitePrestamos(String idUsuario, Connection conexion) throws SQLException {
    // Obtener el rol del usuario
    String rolQuery = "SELECT rol FROM usuarios WHERE id = ?";
    PreparedStatement rolStmt = conexion.prepareStatement(rolQuery);
    rolStmt.setString(1, idUsuario);
    ResultSet rolRs = rolStmt.executeQuery();

    if (rolRs.next()) {
        String rol = rolRs.getString("rol").toLowerCase();

        // Obtener el límite de préstamos para el rol del usuario
        String limiteQuery = "SELECT valor FROM configuraciones WHERE clave = ?";
        PreparedStatement limiteStmt = conexion.prepareStatement(limiteQuery);
        limiteStmt.setString(1, "limite_prestamos_" + rol);

        ResultSet limiteRs = limiteStmt.executeQuery();
        if (limiteRs.next()) {
            int limitePrestamos = limiteRs.getInt("valor");

            // Contar la cantidad de préstamos activos del usuario
            String prestamosQuery = "SELECT COUNT(*) AS prestamos_activos FROM prestamos WHERE id_usuario = ? AND estado = 'Pendiente'";
            PreparedStatement prestamosStmt = conexion.prepareStatement(prestamosQuery);
            prestamosStmt.setString(1, idUsuario);

            ResultSet prestamosRs = prestamosStmt.executeQuery();
            if (prestamosRs.next()) {
                int prestamosActivos = prestamosRs.getInt("prestamos_activos");
                return prestamosActivos < limitePrestamos; // Validar si está dentro del límite
            }
        }
    }
    return false; // Por defecto, denegar si algo falla
}

    private double obtenerMoraDiariaPorRol(String idUsuario, Connection conexion) throws SQLException {
        // Obtener el rol del usuario
        String rolQuery = "SELECT rol FROM usuarios WHERE id = ?";
        PreparedStatement rolStmt = conexion.prepareStatement(rolQuery);
        rolStmt.setString(1, idUsuario);
        ResultSet rolRs = rolStmt.executeQuery();

        if (rolRs.next()) {
            String rol = rolRs.getString("rol").toLowerCase();

            // Obtener la mora diaria asociada al rol
            String moraQuery = "SELECT valor FROM configuraciones WHERE clave = ?";
            PreparedStatement moraStmt = conexion.prepareStatement(moraQuery);
            moraStmt.setString(1, "mora_" + rol);

            ResultSet moraRs = moraStmt.executeQuery();
            if (moraRs.next()) {
                return moraRs.getDouble("valor"); // Retornar la mora configurada
            }
        }
        return 1.50; // Valor por defecto si no se encuentra
    }

    /**
     * Método auxiliar para crear una etiqueta estilizada.
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
}
