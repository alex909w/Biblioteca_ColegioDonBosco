package biblioteca.interfaces.Inventario;


import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditarArticulo extends JPanel {
    private JTextField idArticuloField, nombreArticuloField, categoriaField, cantidadField, ubicacionField;
    private JButton buscarButton, actualizarButton;

    public EditarArticulo() {
        setLayout(new GridLayout(6, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Editar Artículo del Inventario"));

        add(new JLabel("ID del Artículo:"));
        idArticuloField = new JTextField();
        add(idArticuloField);

        buscarButton = new JButton("Buscar");
        buscarButton.addActionListener(e -> buscarArticulo());
        add(buscarButton);

        add(new JLabel("Nombre del Artículo:"));
        nombreArticuloField = new JTextField();
        nombreArticuloField.setEnabled(false);
        add(nombreArticuloField);

        add(new JLabel("Categoría:"));
        categoriaField = new JTextField();
        categoriaField.setEnabled(false);
        add(categoriaField);

        add(new JLabel("Cantidad Total:"));
        cantidadField = new JTextField();
        cantidadField.setEnabled(false);
        add(cantidadField);

        add(new JLabel("Ubicación Física:"));
        ubicacionField = new JTextField();
        ubicacionField.setEnabled(false);
        add(ubicacionField);

        actualizarButton = new JButton("Actualizar");
        actualizarButton.setEnabled(false);
        actualizarButton.addActionListener(e -> actualizarArticulo());
        add(actualizarButton);
    }

    private void buscarArticulo() {
        String idArticulo = idArticuloField.getText();
        if (idArticulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese el ID del artículo.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT * FROM inventario WHERE id_articulo = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idArticulo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nombreArticuloField.setText(rs.getString("nombre"));
                categoriaField.setText(rs.getString("categoria"));
                cantidadField.setText(String.valueOf(rs.getInt("cantidad_total")));
                ubicacionField.setText(rs.getString("ubicacion_fisica"));

                nombreArticuloField.setEnabled(true);
                categoriaField.setEnabled(true);
                cantidadField.setEnabled(true);
                ubicacionField.setEnabled(true);
                actualizarButton.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Artículo no encontrado.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar artículo: " + ex.getMessage());
        }
    }

    private void actualizarArticulo() {
        String idArticulo = idArticuloField.getText();
        String nombreArticulo = nombreArticuloField.getText();
        String categoria = categoriaField.getText();
        String cantidad = cantidadField.getText();
        String ubicacion = ubicacionField.getText();

        if (nombreArticulo.isEmpty() || categoria.isEmpty() || cantidad.isEmpty() || ubicacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "UPDATE inventario SET nombre = ?, categoria = ?, cantidad_total = ?, ubicacion_fisica = ? WHERE id_articulo = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombreArticulo);
            stmt.setString(2, categoria);
            stmt.setInt(3, Integer.parseInt(cantidad));
            stmt.setString(4, ubicacion);
            stmt.setString(5, idArticulo);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Artículo actualizado exitosamente.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar artículo: " + ex.getMessage());
        }
    }
}
