package sistemaventas;

import java.io.Serializable;
import java.time.LocalDate;

public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dpi;
    private String nombre;
    private String direccion;
    private LocalDate fechaNacimiento;
    private String tipo;

    public Cliente(String dpi, String nombre, String direccion, LocalDate fechaNacimiento, String tipo) {
        setDpi(dpi);
        setNombre(nombre);
        setDireccion(direccion);
        setFechaNacimiento(fechaNacimiento);
        setTipo(tipo);
    }

    public String getDpi() {
        return dpi;
    }

    public final void setDpi(String dpi) {
        this.dpi = Validaciones.dpi(dpi, "DPI cliente");
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        this.nombre = Validaciones.texto(nombre, "Nombre del cliente");
    }

    public String getDireccion() {
        return direccion;
    }

    public final void setDireccion(String direccion) {
        this.direccion = Validaciones.texto(direccion, "Dirección del cliente");
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public final void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula ni estar en el futuro.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTipo() {
        return tipo;
    }

    public final void setTipo(String tipo) {
        String valor = Validaciones.texto(tipo, "Tipo de cliente").toUpperCase();
        if (!valor.equals("REGULAR") && !valor.equals("VIP")) {
            throw new IllegalArgumentException("El tipo de cliente debe ser Regular o VIP.");
        }
        this.tipo = valor.equals("VIP") ? "VIP" : "Regular";
    }

    @Override
    public String toString() {
        return "Cliente[dpi=" + dpi + ", nombre=" + nombre + ", tipo=" + tipo + "]";
    }
}
