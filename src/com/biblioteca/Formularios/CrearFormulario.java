package com.biblioteca.Formularios;

import com.biblioteca.controller.FormularioController;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.TitledBorder;

// Clase que representa un panel para crear formularios dinámicos con campos y tablas.
public class CrearFormulario extends JPanel {
    private JTextField nombreTablaField;
    private JTextField numeroColumnasField;
    private JPanel columnasPanel;
    private JButton generarColumnasButton, crearTablaButton;

    // Permite al usuario ingresar el nombre de la tabla y el número de columnas.
    private final Color botonCrearTabla = new Color(255, 69, 0); // Orange Red
    private final Color botonCrearTablaHover = new Color(178, 34, 34); // Firebrick
    private final Color botonGenerarCampos = new Color(34, 139, 34); // Forest Green
    private final Color botonGenerarCamposHover = new Color(0, 100, 0); // Dark Green

    // generar los campos dinámicos y crear la tabla en la base de datos.
    private List<JTextField> camposDinamicos = new ArrayList<>();
    private FormularioController formularioController = new FormularioController();

    // Constructor de la clase CrearFormulario. Configura el panel con un diseño de BorderLayout,
// establece un borde con título personalizado y define la apariencia del título y bordes.
    
    public CrearFormulario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Crear Nuevo Formulario",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel superior con las configuraciones
        JPanel configuracionPanel = new JPanel(new GridBagLayout());
        configuracionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre del formulario
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        configuracionPanel.add(createStyledLabel("Nombre del Formulario:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        configuracionPanel.add(nombreTablaField = createStyledTextField(), gbc);

        // Número de columnas
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        configuracionPanel.add(createStyledLabel("Número de Columnas:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.8;
        configuracionPanel.add(numeroColumnasField = createStyledTextField(), gbc);

        // Botón generar columnas
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        configuracionPanel.add(generarColumnasButton = createStyledButton("Generar Campos", botonGenerarCampos, botonGenerarCamposHover), gbc);

        generarColumnasButton.addActionListener(e -> generarCampos());

        add(configuracionPanel, BorderLayout.NORTH);

        // Panel central para columnas dinámicas
        columnasPanel = new JPanel();
        columnasPanel.setLayout(new BoxLayout(columnasPanel, BoxLayout.Y_AXIS));
        columnasPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JScrollPane scrollPanel = new JScrollPane(columnasPanel);
        scrollPanel.setPreferredSize(new Dimension(500, 300));
        scrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Columnas Personalizadas",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        add(scrollPanel, BorderLayout.CENTER);

        // Botón crear tabla
        crearTablaButton = createStyledButton("Crear Tabla", botonCrearTabla, botonCrearTablaHover);
        crearTablaButton.addActionListener(e -> crearTabla());
        crearTablaButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.add(crearTablaButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Método que genera dinámicamente los campos de columna basados en la cantidad ingresada.
    private void generarCampos() {
        columnasPanel.removeAll();
        camposDinamicos.clear();
        crearTablaButton.setEnabled(false);

        // Verifica que el nombre del formulario no esté vacío y que no exista en la base de datos.
        String nombreTabla = nombreTablaField.getText().trim();
        if (nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del formulario no puede estar vacío.");
            return;
        }

        try {
            if (formularioController.verificarNombreExistente(nombreTabla)) {
                JOptionPane.showMessageDialog(this, "El nombre del formulario ya existe en 'tipos_documentos'.");
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al verificar el nombre en 'tipos_documentos': " + e.getMessage());
            return;
        }

        try {
            int numeroColumnas = Integer.parseInt(numeroColumnasField.getText());
            if (numeroColumnas <= 0) {
                JOptionPane.showMessageDialog(this, "El número de columnas debe ser mayor a 0.");
                return;
            }

            for (int i = 0; i < numeroColumnas; i++) {
                JPanel columnaPanel = new JPanel(new BorderLayout(10, 10));
                columnaPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                columnaPanel.setBackground(Color.WHITE);

                JLabel columnaLabel = createStyledLabel("Nombre de la Columna " + (i + 1) + ":");
                columnaPanel.add(columnaLabel, BorderLayout.WEST);

                JTextField nombreColumnaField = createStyledTextField();
                columnaPanel.add(nombreColumnaField, BorderLayout.CENTER);

                camposDinamicos.add(nombreColumnaField);
                columnasPanel.add(columnaPanel);
                columnasPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            // Luego, genera los campos de texto para cada columna y habilita el botón para crear la tabla.
            crearTablaButton.setEnabled(true);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido para las columnas.");
        }

        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

    // Método para crear una nueva tabla en la base de datos con las columnas definidas por el usuario.
    private void crearTabla() {
        String nombreTabla = nombreTablaField.getText().trim();
        
        // Realiza varias validaciones: que el nombre del formulario y las columnas no estén vacíos,
        if (nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del formulario no puede estar vacío.");
            return;
        }
        
// que los nombres sean válidos y que no contengan caracteres no permitidos.
        if (!nombreTabla.matches("[a-zA-Z0-9_ ]+")) {
            JOptionPane.showMessageDialog(this, "El nombre del formulario debe contener solo letras, números, espacios o guiones bajos.",
                    "Error en el nombre", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Luego, llama al controlador para crear la tabla en la base de datos y limpia los campos tras el éxito.
        String nombreTablaDB = sanitizeName(nombreTabla);
        List<String> nombresColumnas = new ArrayList<>();
        for (JTextField campo : camposDinamicos) {
            String nombreColumna = campo.getText().trim();
            if (nombreColumna.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Los nombres de las columnas no pueden estar vacíos.");
                return;
            }
            if (!nombreColumna.matches("[\\p{L}\\p{N}_ áéíóúÁÉÍÓÚñÑüÜ!@#$%^&*()+-=/]+")) {
                JOptionPane.showMessageDialog(this, "El nombre de la columna '" + nombreColumna + "' contiene caracteres inválidos.");
                return;
            }
            nombresColumnas.add(nombreColumna);
        }

        try {
            formularioController.crearTabla(nombreTablaDB, nombresColumnas);
            JOptionPane.showMessageDialog(this, "Tabla registrada de forma exitosa.");

            // Limpiar campos después de la creación
            nombreTablaField.setText("");
            numeroColumnasField.setText("");
            columnasPanel.removeAll();
            camposDinamicos.clear();
            columnasPanel.revalidate();
            columnasPanel.repaint();
            crearTablaButton.setEnabled(false);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al crear la tabla: " + e.getMessage());
        }
    }

    // Método para sanitizar el nombre de la tabla, eliminando espacios en blanco adicionales 
// y reemplazando los espacios por guiones bajos ('_'). Esto asegura que el nombre sea adecuado 
// para ser utilizado en una base de datos.
    
    private String sanitizeName(String name) {
        return name.trim().replaceAll(" +", "_");
    }

    // Método para crear un botón estilizado con un texto, color de fondo por defecto y color de fondo al pasar el ratón (hover).
    private JButton createStyledButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14)); // Fuente en Arial, negrita, tamaño 14
        button.setBackground(defaultColor); // Establece el color de fondo por defecto
        button.setForeground(Color.WHITE); // Establece el color del texto a blanco
        button.setFocusPainted(false); // Desactiva el borde de enfoque (cuando se selecciona)
        button.setPreferredSize(new Dimension(160, 45));  // Establece el tamaño preferido del botón
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1), // Borde gris de 1px
                BorderFactory.createEmptyBorder(5, 5, 5, 5) // Relleno interno de 5px
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cambia el cursor al pasar sobre el botón
        
// Se establece un borde, un tamaño preferido, y se ajusta la fuente. También se agrega un efecto visual al pasar el ratón
// sobre el botón, cambiando su color de fondo entre el valor por defecto y el color de hover.
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor); // Cambia al color de hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(defaultColor); // Vuelve al color por defecto
            }
        });
        return button; // Devuelve el botón creado
    }

    // Método para crear una etiqueta (JLabel) estilizada con un texto.
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text); // Crea la etiqueta con el texto proporcionado
        label.setFont(new Font("Arial", Font.BOLD, 14)); // Establece la fuente en Arial, negrita, tamaño 14
        label.setForeground(new Color(25, 25, 112)); // Establece el color del texto a un tono azul oscuro (Midnight Blue)
        return label; // Devuelve la etiqueta creada
    }

    // Método para crear un campo de texto (JTextField) estilizado.
    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(); // Crea un nuevo campo de texto vacío
        textField.setFont(new Font("Arial", Font.PLAIN, 14)); // Establece la fuente en Arial, tamaño 14
        // Configura un borde compuesto alrededor del campo de texto
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1), // Línea azul de 1px de grosor
                BorderFactory.createEmptyBorder(5, 5, 5, 5) // Espacio interno alrededor del campo de texto
        ));
        textField.setBackground(Color.WHITE); // Establece el fondo del campo de texto a blanco
        return textField; // Devuelve el campo de texto estilizado
    }
}
