package com.biblioteca.Panel.Inventario;

import com.biblioteca.controller.InventarioController;
import com.biblioteca.utilidades.DateLabelFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.Properties;

public class RegistrarArticulo extends JPanel {
    private JComboBox<String> formulariosComboBox;
    private JPanel formularioPanel;
    private JButton cargarFormularioButton, registrarButton;
    private String tablaSeleccionada;
    private List<Component> camposDinamicos;
    private InventarioController inventarioController = new InventarioController();

    private final Color botonCargarFormulario = new Color(34, 139, 34);
    private final Color botonCargarFormularioHover = new Color(0, 100, 0);
    private final Color botonRegistrar = new Color(255, 69, 0);
    private final Color botonRegistrarHover = new Color(178, 34, 34);

    public RegistrarArticulo() {

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Registrar Información en Tablas",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel superior con el ComboBox para seleccionar tablas y botón cargar formulario
        JPanel superiorPanel = new JPanel(new GridBagLayout());
        superiorPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta para seleccionar tabla
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        superiorPanel.add(createStyledLabel("Seleccionar Tabla:"), gbc);

        // ComboBox para tablas
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        formulariosComboBox = createStyledComboBox();
        formulariosComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    String displayName = formatString((String) value);
                    label.setText(displayName);
                }
                return label;
            }
        });
        superiorPanel.add(formulariosComboBox, gbc);

        // Botón Cargar Formulario
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        superiorPanel.add(cargarFormularioButton = createStyledButton("Cargar Formulario", botonCargarFormulario, botonCargarFormularioHover), gbc);

        cargarFormularioButton.addActionListener(e -> cargarFormulario());

        add(superiorPanel, BorderLayout.NORTH);

        // Panel central para formularios dinámicos
        formularioPanel = new JPanel(new GridBagLayout());
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JScrollPane scrollPanel = new JScrollPane(formularioPanel);
        scrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Formulario Dinámico",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        add(scrollPanel, BorderLayout.CENTER);

        // Panel inferior para el botón de registrar
        JPanel inferiorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        inferiorPanel.add(registrarButton = createStyledButton("Registrar Datos", botonRegistrar, botonRegistrarHover));
        registrarButton.setEnabled(false); // Deshabilitado hasta que se cargue un formulario
        registrarButton.addActionListener(e -> registrarDatos());
        add(inferiorPanel, BorderLayout.SOUTH);

        // Inicializar camposDinamicos
        camposDinamicos = new ArrayList<>();

        // Cargar formularios existentes al inicializar
        cargarFormularios();
    }

    // Carga los formularios existentes desde la base de datos y los añade al ComboBox.
     
    private void cargarFormularios() {
        formulariosComboBox.removeAllItems();
        // Añadir el elemento predeterminado "Opciones"
        addDefaultItem(formulariosComboBox, "Opciones");

        try {
            List<String> formularios = inventarioController.obtenerFormularios();
            for (String formulario : formularios) {
                formulariosComboBox.addItem(formulario);
            }
            if (formularios.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron formularios registrados en 'tipos_documentos'.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar formularios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carga el formulario dinámico basado en la tabla seleccionada.
     
    private void cargarFormulario() {
        tablaSeleccionada = (String) formulariosComboBox.getSelectedItem();
        if (tablaSeleccionada == null || tablaSeleccionada.equals("Opciones")) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una opción válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        formularioPanel.removeAll();
        camposDinamicos = new ArrayList<>();

        try {
            List<Map<String, String>> columnas = inventarioController.obtenerColumnasTabla(tablaSeleccionada);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.3;

            for (Map<String, String> columnaInfo : columnas) {
                String nombreColumna = columnaInfo.get("Field");
                String tipo = columnaInfo.get("Type").toLowerCase();

                boolean esId = nombreColumna.equalsIgnoreCase("id_" + tablaSeleccionada.toLowerCase());

                String etiquetaTexto = esId ? "ID:" : formatString(nombreColumna) + ":";
                JLabel etiqueta = createStyledLabel(etiquetaTexto);
                formularioPanel.add(etiqueta, gbc);

                gbc.gridx = 1;
                gbc.weightx = 0.7;

                if (nombreColumna.equalsIgnoreCase("estado")) {
                    JComboBox<String> estadoComboBox = new JComboBox<>(new String[]{"Bueno", "Dañado", "En Reparación"});
                    estadoComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
                    estadoComboBox.setBackground(Color.WHITE);
                    formularioPanel.add(estadoComboBox, gbc);
                    camposDinamicos.add(estadoComboBox);
                } else if (esId) {
                    String prefijo = tablaSeleccionada.length() >= 3 ? tablaSeleccionada.substring(0, 3).toUpperCase() : tablaSeleccionada.toUpperCase();
                    String nuevoId = inventarioController.generarNuevoId(tablaSeleccionada, prefijo);
                    JTextField campoTexto = createStyledTextField();
                    campoTexto.setText(nuevoId);
                    campoTexto.setEditable(false);
                    formularioPanel.add(campoTexto, gbc);
                    camposDinamicos.add(campoTexto);
                } else if (tipo.contains("date") || tipo.contains("timestamp")) {
                    JPanel campoFecha = crearCampoFecha();
                    formularioPanel.add(campoFecha, gbc);
                } else {
                    JTextField campoTexto = createStyledTextField();
                    formularioPanel.add(campoTexto, gbc);
                    camposDinamicos.add(campoTexto);
                }

                gbc.gridx = 0;
                gbc.weightx = 0.3;
                gbc.gridy++;
            }

            formularioPanel.revalidate();
            formularioPanel.repaint();
            registrarButton.setEnabled(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar formulario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Crea un campo de fecha con calendario.

    private JPanel crearCampoFecha() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255));

        UtilDateModel model = new UtilDateModel();
        model.setSelected(false);

        Properties properties = new Properties();
        properties.put("text.today", "Hoy");
        properties.put("text.month", "Mes");
        properties.put("text.year", "Año");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        panel.add(datePicker, BorderLayout.CENTER);
        camposDinamicos.add(datePicker);

        return panel;
    }

    // Obtiene la fecha seleccionada del JDatePicker.

    private java.sql.Date obtenerFechaSeleccionada(JDatePickerImpl datePicker) {
        if (datePicker.getModel().getValue() != null) {
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            return new java.sql.Date(selectedDate.getTime());
        }
        return null;
    }

    // Registra los datos ingresados en el formulario en la tabla seleccionada.
     
    private void registrarDatos() {
        if (tablaSeleccionada == null || tablaSeleccionada.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una tabla válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar entradas antes de proceder
        if (!validarEntradas()) {
            return;
        }

        List<Object> valores = new ArrayList<>();
        List<String> columnas = new ArrayList<>();

        try {
            List<Map<String, String>> columnasInfo = inventarioController.obtenerColumnasTabla(tablaSeleccionada);
            for (Map<String, String> columnaInfo : columnasInfo) {
                columnas.add(columnaInfo.get("Field"));
            }

            for (Component campo : camposDinamicos) {
                if (campo instanceof JTextField) {
                    valores.add(((JTextField) campo).getText().trim());
                } else if (campo instanceof JComboBox) {
                    valores.add(((JComboBox<?>) campo).getSelectedItem().toString());
                } else if (campo instanceof JDatePickerImpl) {
                    java.sql.Date fecha = obtenerFechaSeleccionada((JDatePickerImpl) campo);
                    valores.add(fecha);
                }
            }

            inventarioController.registrarDatos(tablaSeleccionada, columnas, valores);
            JOptionPane.showMessageDialog(this, "Datos registrados exitosamente en " + tablaSeleccionada, "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // Limpiar campos después del registro
            limpiarCampos();

            // Actualizar el ID para el siguiente registro
            actualizarProximoId();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Valida las entradas del formulario antes de registrar los datos.

    private boolean validarEntradas() {
        for (Component campo : camposDinamicos) {
            if (campo instanceof JTextField) {
                JTextField textField = (JTextField) campo;
                if (textField.isEditable() && textField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } else if (campo instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>) campo;
                if (comboBox.getSelectedItem() == null || comboBox.getSelectedItem().toString().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } else if (campo instanceof JDatePickerImpl) {
                JDatePickerImpl datePicker = (JDatePickerImpl) campo;
                if (datePicker.getModel().getValue() == null) {
                    JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    // Limpia los campos de texto editables después de registrar los datos.
     
    private void limpiarCampos() {
        for (Component campo : camposDinamicos) {
            if (campo instanceof JTextField) {
                JTextField textField = (JTextField) campo;
                if (textField.isEditable()) {
                    textField.setText("");
                }
            } else if (campo instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>) campo;
                comboBox.setSelectedIndex(-1);
            } else if (campo instanceof JDatePickerImpl) {
                JDatePickerImpl datePicker = (JDatePickerImpl) campo;
                datePicker.getModel().setValue(null);
            }
        }
    }

    // Actualiza el próximo ID después de registrar un nuevo registro.
 
    private void actualizarProximoId() {
        for (Component campo : camposDinamicos) {
            if (campo instanceof JTextField) {
                JTextField textField = (JTextField) campo;
                if (!textField.isEditable()) { // Es el campo ID
                    try {
                        String prefijo = tablaSeleccionada.length() >= 3 ? tablaSeleccionada.substring(0, 3).toUpperCase() : tablaSeleccionada.toUpperCase();
                        String nuevoId = inventarioController.generarNuevoId(tablaSeleccionada, prefijo);
                        textField.setText(nuevoId);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error al actualizar el ID: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                }
            }
        }
    }

    // Crea un botón estilizado con colores personalizados y efectos hover.

    private JButton createStyledButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 40));
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

    //Crea una etiqueta estilizada con colores y fuentes personalizadas.

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180));
        return label;
    }

    //Crea un ComboBox estilizado con bordes y fuentes personalizadas.

    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
    }

    // Crea un campo de texto estilizado con bordes y fuentes personalizadas.
    
    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        textField.setBackground(Color.WHITE);
        return textField;
    }

    // Formatea una cadena de texto convirtiéndola a mayúsculas y reemplazando guiones bajos con espacios.

    private String formatString(String input) {
        return input.toUpperCase().replace("_", " ");
    }

    //Añade un elemento predeterminado al ComboBox.
   
    private void addDefaultItem(JComboBox<String> comboBox, String defaultItem) {
        if (comboBox.getItemCount() == 0 || !comboBox.getItemAt(0).equals(defaultItem)) {
            comboBox.insertItemAt(defaultItem, 0);
            comboBox.setSelectedIndex(0);
        }
    }
}
