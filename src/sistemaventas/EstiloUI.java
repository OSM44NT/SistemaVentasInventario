package sistemaventas;

import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class EstiloUI {
    public static final Color BLANCO = Color.WHITE;
    public static final Color GRIS_FONDO = new Color(245, 245, 245);
    public static final Color GRIS_BORDE = new Color(220, 220, 220);
    public static final Color TEXTO = new Color(45, 45, 45);
    public static final Color GRIS_BOTON = new Color(235, 235, 235);

    public EstiloUI() {}

    public static void aplicar() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Mantener L&F por defecto si el del sistema no está disponible
        }

        FontUIResource fuenteNormal = new FontUIResource("Segoe UI", 0, 13);
        FontUIResource fuenteEncabezado = new FontUIResource("Segoe UI", 1, 13);

        UIManager.put("Panel.background", GRIS_FONDO);
        UIManager.put("OptionPane.background", BLANCO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Label.font", fuenteNormal);
        UIManager.put("Button.background", GRIS_BOTON);
        UIManager.put("Button.foreground", TEXTO);
        UIManager.put("Button.font", fuenteNormal);
        UIManager.put("TextField.background", BLANCO);
        UIManager.put("TextField.foreground", TEXTO);
        UIManager.put("TextField.font", fuenteNormal);
        UIManager.put("Table.background", BLANCO);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("Table.gridColor", GRIS_BORDE);
        UIManager.put("Table.font", fuenteNormal);
        UIManager.put("TableHeader.background", GRIS_BOTON);
        UIManager.put("TableHeader.foreground", TEXTO);
        UIManager.put("TableHeader.font", fuenteEncabezado);
        UIManager.put("ScrollPane.background", BLANCO);
        UIManager.put("TextArea.background", BLANCO);
    }
}
