package com.biblioteca.Formularios;

import com.biblioteca.controller.FormularioController;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.border.TitledBorder;

/**
 * Panel para editar un formulario existente.
 */
public class EditarFormulario extends JPanel {
    private JComboBox<String> tablasComboBox;
    private JPanel columnasPanel;
    private JButton cargarTablaButton, actualizarTablaButton, agregarColumnaButton, cancelarButton;
    private boolean usarExistente = false;

    private final Color botonCargarTabla = new Color(34, 139, 34); // Forest Green
    private final Color botonCargarTablaHover = new Color(0, 100, 0); // Dark Green
    private final Color botonActualizarTabla = new Color(255, 69, 0); // Orange Red
    private final Color botonActualizarTablaHover = new Color(178, 34, 34); // Firebrick
    private final Color botonAgregarColumna = new Color(70, 130, 180); // Steel Blue
    private final Color botonAgregarColumnaHover = new Color(30, 144, 255); // Dodger Blue
    private final Color botonCancelar = new Color(220, 20, 60); // Crimson
    private final Color botonCancelarHover = new Color(178, 34, 34); // Firebrick

    private final Set<String> columnasExcluidasGenerales = new HashSet<>(Arrays.asList(
            "fecha_registro",
            "ubicacion_fisica",
            "cantidad_total",
            "cantidad_disponible",
            "estado",
            "palabras_clave"
    ));

    private FormularioController formularioController = new FormularioController();

    private List<JTextField> camposDinamicos = new ArrayList<>();
    private List<JTextField> nuevasColumnas = new ArrayList<>();

    // Constructor de la clase EditarFormulario, que inicializa la interfaz gráfica para editar formularios existentes.
    
    public EditarFormulario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Editar Formulario Existente",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel superior con configuraciones
        JPanel configuracionPanel = new JPanel(new GridBagLayout());
        configuracionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Selección de Tabla
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        configuracionPanel.add(createStyledLabel("Seleccione una Tabla:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        configuracionPanel.add(tablasComboBox = createStyledComboBox(), gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        configuracionPanel.add(cargarTablaButton = createStyledButton("Cargar Tabla", botonCargarTabla, botonCargarTablaHover), gbc);

        cargarTablaButton.addActionListener(e -> cargarColumnas());

        add(configuracionPanel, BorderLayout.NORTH);

        // Panel central para columnas dinámicas
        columnasPanel = new JPanel();
        columnasPanel.setLayout(new BoxLayout(columnasPanel, BoxLayout.Y_AXIS));
        columnasPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JScrollPane scrollPanel = new JScrollPane(columnasPanel);
        scrollPanel.setPreferredSize(new Dimension(600, 400));
        scrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Columnas Personalizadas",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        add(scrollPanel, BorderLayout.CENTER);

        // Panel inferior para los botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        // Botón "Actualizar Tabla"
        actualizarTablaButton = createStyledButton("Actualizar Tabla", botonActualizarTabla, botonActualizarTablaHover);
        actualizarTablaButton.addActionListener(e -> actualizarTabla());
        buttonPanel.add(actualizarTablaButton);

        // Botón "Agregar Nueva Columna"
        agregarColumnaButton = createStyledButton("Agregar Nueva Columna", botonAgregarColumna, botonAgregarColumnaHover);
        agregarColumnaButton.addActionListener(e -> agregarNuevaColumna());
        buttonPanel.add(agregarColumnaButton);

        // Botón "Cancelar"
        cancelarButton = createStyledButton("Cancelar", botonCancelar, botonCancelarHover);
        cancelarButton.addActionListener(e -> cancelarAccion());
        buttonPanel.add(cancelarButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Método que carga las columnas de la tabla seleccionada y permite editarlas
    private void cargarColumnas() {
        
         // Limpia el panel de columnas y las listas de campos dinámicos
        columnasPanel.removeAll();
        camposDinamicos.clear();
        nuevasColumnas.clear();
        usarExistente = false;
        
   // Obtiene el nombre de la tabla seleccionada del ComboBox
   
        String nombreTabla = (String) tablasComboBox.getSelectedItem();
        // Verifica si se ha seleccionado una tabla válida
        if (nombreTabla == null || nombreTabla.equals("Opciones")) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
// Verifica si el nombre de la tabla contiene caracteres inválidos

        if (!nombreTabla.matches("[a-zA-Z0-9_]+")) {
            JOptionPane.showMessageDialog(this, "El nombre de la tabla contiene caracteres inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

         // Crea el identificador de la columna "id" de la tabla, basado en el nombre de la tabla
        String idColumna = "id_" + nombreTabla.toLowerCase();

        try {
             // Obtiene las columnas de la tabla seleccionada desde el controlador
            List<String> columnas = formularioController.obtenerColumnas(nombreTabla);
            
             // Itera sobre las columnas obtenidas para crear un panel para cada una
            for (String campo : columnas) {
                String tipo = obtenerTipoColumna(nombreTabla, campo);    // Obtiene el tipo de cada columna
                JPanel columnaPanel = new JPanel(new GridBagLayout());  // Crea un panel para cada columna con un diseño de cuadrícula
                columnaPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                columnaPanel.setBackground(Color.WHITE);

                 // Configura las restricciones de la cuadrícula para el panel de la columna
                GridBagConstraints gbcCol = new GridBagConstraints();
                gbcCol.insets = new Insets(5, 5, 5, 5);
                gbcCol.fill = GridBagConstraints.HORIZONTAL;

                // Etiqueta del campo
                gbcCol.gridx = 0;
                gbcCol.gridy = 0;
                gbcCol.weightx = 0.4;
                gbcCol.anchor = GridBagConstraints.WEST;
                String etiquetaTexto = formatString(campo);
                columnaPanel.add(createStyledLabel("Columna: " + etiquetaTexto), gbcCol);

                // Campo para nuevo nombre
                gbcCol.gridx = 1;
                gbcCol.gridy = 0;
                gbcCol.weightx = 0.6;
                JTextField nuevoNombreField = createStyledTextField();
                nuevoNombreField.setToolTipText(campo); // Guardar el nombre original
                columnaPanel.add(nuevoNombreField, gbcCol);

                camposDinamicos.add(nuevoNombreField);
                columnasPanel.add(columnaPanel);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar columnas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();    // Maneja cualquier error de base de datos durante la carga de las columnas
        }

        // Refresca la interfaz gráfica para mostrar las nuevas columnas cargadas
        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

    private String obtenerTipoColumna(String nombreTabla, String columna) throws SQLException {
    String tipo = "VARCHAR(255)"; // Valor por defecto
    String sql = "DESCRIBE `" + nombreTabla + "`"; // Consulta para obtener la descripción de la tabla

    // Verifica conexión
    try (Connection conn = formularioController.formularioDAO.getConexion()) {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("La conexión a la base de datos no está disponible.");
        }

        // Verifica si la tabla existe
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rsMeta = metaData.getTables(null, null, nombreTabla, null)) {
            if (!rsMeta.next()) {
                throw new SQLException("La tabla especificada no existe: " + nombreTabla);
            }
        }

        // Ejecuta la consulta DESCRIBE
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Filtra la columna deseada
            while (rs.next()) {
                if (rs.getString("Field").equalsIgnoreCase(columna)) {
                    tipo = rs.getString("Type");
                    break;
                }
            }
        }
    }

    return tipo; // Retorna el tipo de la columna o el valor predeterminado
}

    private void actualizarTabla() {
        String nombreTabla = (String) tablasComboBox.getSelectedItem(); // Obtener el nombre de la tabla seleccionada del ComboBox
        
        // Verificar que el nombre de la tabla no sea nulo, vacío o "Opciones"
        if (nombreTabla == null || nombreTabla.isEmpty() || nombreTabla.equals("Opciones")) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

          // Recorrer todos los campos dinámicos (nuevos nombres de columna)
        try {
            for (JTextField nombreField : camposDinamicos) {
                String nuevoNombre = nombreField.getText().trim(); // Obtener el nuevo nombre de la columna
                String nombreActual = nombreField.getToolTipText(); // Obtener el nombre original de la columna (guardado en el tooltip)

                // Si el nuevo nombre no está vacío y es diferente al actual

                if (!nuevoNombre.isEmpty() && !nuevoNombre.equalsIgnoreCase(nombreActual)) {
                    
                    // Verificar que el nuevo nombre de la columna no contenga caracteres inválidos
                    if (!nuevoNombre.matches("[\\p{L}\\p{N}_ áéíóúÁÉÍÓÚñÑüÜ!@#$%^&*()+-=/]+")) {
                        JOptionPane.showMessageDialog(this, "El nombre de la columna '" + nuevoNombre + "' contiene caracteres inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue; // Salta al siguiente nombre de columna si el nombre no es válido
                    }

                    String nuevoNombreDB = sanitizeName(nuevoNombre); // Sanitizar el nombre de la columna

                    // Verificar que el nuevo nombre no sea el nombre de la columna ID
                    String idColumna = "id_" + nombreTabla.toLowerCase();
                    if (nuevoNombreDB.equalsIgnoreCase(idColumna)) {
                        JOptionPane.showMessageDialog(this, "No puedes renombrar otra columna al nombre de la columna ID ('" + idColumna + "').", "Error", JOptionPane.ERROR_MESSAGE);
                        continue; // Salta al siguiente campo si se intenta renombrar una columna como ID
                    }

                    String tipoDato = nuevoNombre.toLowerCase().contains("fecha") ? "DATE" : "VARCHAR(255)";   // Determinar el tipo de dato para la columna
                    formularioController.actualizarNombreColumna(nombreTabla, nombreActual, nuevoNombreDB, tipoDato);  // Llamar al controlador para actualizar el nombre de la columna en la base de datos
                }
            }

             // Recorrer todos los campos para nuevas columnas y agregar las nuevas columnas
            for (JTextField nuevaColumnaField : nuevasColumnas) {
                String nuevoNombre = nuevaColumnaField.getText().trim(); // Obtener el nombre de la nueva columna
                
             // Si el nombre de la nueva columna no está vacío
                if (!nuevoNombre.isEmpty()) {
                    if (!nuevoNombre.matches("[\\p{L}\\p{N}_ áéíóúÁÉÍÓÚñÑüÜ!@#$%^&*()+-=/]+")) {
                        JOptionPane.showMessageDialog(this, "El nombre de la nueva columna '" + nuevoNombre + "' contiene caracteres inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;  // Salta a la siguiente nueva columna si el nombre es inválido
                    }

                    String nuevoNombreDB = sanitizeName(nuevoNombre); // Sanitizar el nombre de la nueva columna
                    String idColumna = "id_" + nombreTabla.toLowerCase();  // Verificar que el nombre de la nueva columna no sea el mismo que el nombre de la columna ID
                    if (nuevoNombreDB.equalsIgnoreCase(idColumna)) {
                        JOptionPane.showMessageDialog(this, "No puedes agregar una columna con el nombre de la columna ID ('" + idColumna + "').", "Error", JOptionPane.ERROR_MESSAGE);
                        continue; // Salta a la siguiente nueva columna si el nombre es el mismo que el ID
                    }

                     // Determinar el tipo de dato para la nueva columna
                    String tipoDato = nuevoNombre.toLowerCase().contains("fecha") ? "DATE" : "VARCHAR(255)";
                    formularioController.agregarNuevaColumna(nombreTabla, nuevoNombreDB, tipoDato);  // Llamar al controlador para agregar la nueva columna a la base de datos
                }
            }

             // Mostrar mensaje de éxito al actualizar la tabla
            JOptionPane.showMessageDialog(this, "Tabla actualizada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarColumnas();  // Recargar las columnas de la tabla para mostrar los cambios

            // Capturar cualquier error relacionado con la base de datos y mostrar un mensaje de error
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar la tabla: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarNuevaColumna() {
        
          // Crear un panel para la nueva columna utilizando un layout de GridBagLayout
        JPanel nuevaColumnaPanel = new JPanel(new GridBagLayout());
        nuevaColumnaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)  // Espaciado interno del borde
        ));
        nuevaColumnaPanel.setBackground(Color.WHITE);   // Establecer el color de fondo del panel

        // Configurar las restricciones para la disposición de los componentes dentro del panel
        GridBagConstraints gbcNueva = new GridBagConstraints();
        gbcNueva.insets = new Insets(5, 5, 5, 5); // Espaciado entre los componentes
        gbcNueva.fill = GridBagConstraints.HORIZONTAL; // Hacer que los componentes se estiren horizontalmente

        // Etiqueta para nueva columna
        gbcNueva.gridx = 0;
        gbcNueva.gridy = 0;
        gbcNueva.weightx = 0.4;
        gbcNueva.anchor = GridBagConstraints.WEST;
        nuevaColumnaPanel.add(createStyledLabel("Nueva Columna:"), gbcNueva);

        // Campo para nombre de nueva columna
        gbcNueva.gridx = 1;
        gbcNueva.gridy = 0;
        gbcNueva.weightx = 0.6;
        JTextField nuevaColumnaField = createStyledTextField();
        nuevaColumnaPanel.add(nuevaColumnaField, gbcNueva);

        nuevasColumnas.add(nuevaColumnaField);
        columnasPanel.add(nuevaColumnaPanel);
        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

    private void cancelarAccion() {
        columnasPanel.removeAll();
        columnasPanel.revalidate();
        columnasPanel.repaint();

         // Restablecer el índice del JComboBox (tablasComboBox) al primer elemento (que es "Opciones" o vacío)
        tablasComboBox.setSelectedIndex(0);

         // Mostrar un mensaje informando que la actualización ha sido cancelada
        JOptionPane.showMessageDialog(this, "Actualización Cancelada.", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    
    // Elimina espacios al principio y al final del nombre (trim) y reemplaza los espacios múltiples por guiones bajos.
    private String sanitizeName(String name) {
        return name.trim().replaceAll(" +", "_");
    }

    // Crea el botón con el texto especificado
    private JButton createStyledButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text); 
        button.setFont(new Font("Arial", Font.BOLD, 14)); // Configura la fuente del botón a Arial, negrita, tamaño 14
        button.setBackground(defaultColor); // Establece el color de fondo del botón con el color predeterminado
        button.setForeground(Color.WHITE);  // Establece el color del texto del botón a blanco
        button.setFocusPainted(false); // Desactiva la pintura del foco, de modo que no se vean los bordes cuando el botón está enfocado
        button.setPreferredSize(new Dimension(160, 40));  // Establece el tamaño preferido del botón (ancho 160px y alto 40px)
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); // Crea un borde gris delgado alrededor del botón
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cambia el cursor a una mano cuando el ratón pasa por encima del botón

        // Agrega un MouseListener para manejar los eventos cuando el ratón entra y sale del área del botón
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(defaultColor);
            }
        });
        return button; // Devuelve el botón configurado
    }

    // Crea la etiqueta con el texto especificado
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label; // Devuelve la etiqueta configurada
    }

    // Crea el JComboBox vacío
    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox; // Devuelve el JComboBox configurado

    }

    // Crea una nueva instancia de JTextField (campo de texto)
    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        textField.setBackground(Color.WHITE);
        return textField; // Devuelve el campo de texto con el estilo configurado
    }

    private String formatString(String input) {
        return input.toUpperCase().replace("_", " ");
    }

    @Override
    public void addNotify() {
        super.addNotify(); // Llamada al método addNotify() de la clase padre
        cargarTablasExistentes(); // Llamada a la función personalizada para cargar las tablas
    }

    
    private void cargarTablasExistentes() {
        tablasComboBox.removeAllItems(); // Elimina todos los ítems previamente cargados en el JComboBox.
        tablasComboBox.addItem("Opciones"); // Agrega un ítem inicial llamado "Opciones".
        try {
            List<String> tablas = formularioController.obtenerTablas(); // Obtiene la lista de tablas desde el controlador.
            for (String tabla : tablas) { // Recorre la lista de tablas.
                tablasComboBox.addItem(tabla);  // Agrega cada nombre de tabla al JComboBox.
            }
            if (tablas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron tablas registradas en 'tipos_documentos'.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas desde 'tipos_documentos': " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
