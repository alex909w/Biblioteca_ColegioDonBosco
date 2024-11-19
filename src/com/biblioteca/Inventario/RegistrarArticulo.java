package biblioteca.interfaces.Inventario;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrarArticulo extends JPanel {
    private JTextField nombreArticuloField, categoriaField, cantidadField, ubicacionField;

    public RegistrarArticulo() {
        setLayout(new GridLayout(5, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Artículo"));

        add(new JLabel("Nombre del Artículo:"));
        nombreArticuloField = new JTextField();
        add(nombreArticuloField);

        add(new JLabel("Categoría:"));
        categoriaField = new JTextField();
        add(categoriaField);

        add(new JLabel("Cantidad Total:"));
        cantidadField = new JTextField();
        add(cantidadField);

        add(new JLabel("Ubicación Física:"));
        ubicacionField = new JTextField();
        add(ubicacionField);

        JButton registrarButton = new JButton("Registrar");
        registrarButton.addActionListener(e -> registrarArticulo());
        add(registrarButton);
    }

    private void registrarArticulo() {
        String nombreArticulo = nombreArticuloField.getText();
        String categoria = categoriaField.getText();
        String cantidad = cantidadField.getText();
        String ubicacion = ubicacionField.getText();

        if (nombreArticulo.isEmpty() || categoria.isEmpty() || cantidad.isEmpty() || ubicacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "INSERT INTO inventario (nombre, categoria, cantidad_total, ubicacion_fisica) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombreArticulo);
            stmt.setString(2, categoria);
            stmt.setInt(3, Integer.parseInt(cantidad));
            stmt.setString(4, ubicacion);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Artículo registrado exitosamente.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar artículo: " + ex.getMessage());
        }
    }
}
