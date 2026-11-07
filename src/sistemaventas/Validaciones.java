package sistemaventas;

public final class Validaciones {
    private Validaciones() {}

    public static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    public static String dpi(String valor, String campo) {
        String v = texto(valor, campo);
        if (!v.matches("\\d{13}")) {
            throw new IllegalArgumentException("El " + campo + " debe contener exactamente 13 dígitos numéricos.");
        }
        return v;
    }
}
