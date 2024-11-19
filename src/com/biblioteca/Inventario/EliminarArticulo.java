package biblioteca.interfaces.Inventario;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EliminarArticulo extends JPanel {
    private JTextField idArticuloField;

    public EliminarArticulo() {
        setLayout(new GridLayout(3, 1, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Eliminar Artículo del Inventario"));

        add(new JLabel("ID del Artículo:"));
        idArticuloField = new JTextField();
        add(idArticuloField);

        JButton eliminarButton = new JButton("Eliminar");
        eliminarButton.addActionListener(e -> eliminarArticulo());
        add(eliminarButton);
    }

    private void eliminarArticulo() {
        String idArticulo = idArticuloField.getText();
        if (idArticulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese el ID del artículo.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar este artículo?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = ConexionBaseDatos.getConexion()) {
                String sql = "DELETE FROM inventario WHERE id_articulo = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, idArticulo);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Artículo eliminado exitosamente.");
                } else {
                    JOptionPane.showMessageDialog(this, "Artículo no encontrado.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar artículo: " + ex.getMessage());
            }
        }
    }
}
