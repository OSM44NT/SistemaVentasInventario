package sistemaventas;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Aplicar Look & Feel y configuración visual antes de renderizar la UI
                EstiloUI.aplicar();
                SistemaVentas sistema = new SistemaVentas();
                new VentanaPrincipal(sistema).setVisible(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo inicializar la persistencia de datos del sistema:\n" + ex.getMessage(),
                        "Error Crítico de Datos", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Error inesperado al iniciar la aplicación:\n" + ex.getMessage(),
                        "Error Inesperado", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
