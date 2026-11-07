package sistemaventas;

import java.io.Serializable;

public class Empleado implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nombre;
    private String dpi;
    private String direccion;
    private String puesto;
    private double salario;

    public Empleado(String id, String nombre, String dpi, String direccion, String puesto, double salario) {
        setId(id);
        setNombre(nombre);
        setDpi(dpi);
        setDireccion(direccion);
        setPuesto(puesto);
        setSalario(salario);
    }

    public String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = Validaciones.texto(id, "ID empleado");
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        this.nombre = Validaciones.texto(nombre, "Nombre del empleado");
    }

    public String getDpi() {
        return dpi;
    }

    public final void setDpi(String dpi) {
        this.dpi = Validaciones.dpi(dpi, "DPI empleado");
    }

    public String getDireccion() {
        return direccion;
    }

    public final void setDireccion(String direccion) {
        this.direccion = Validaciones.texto(direccion, "Dirección del empleado");
    }

    public String getPuesto() {
        return puesto;
    }

    public final void setPuesto(String puesto) {
        this.puesto = Validaciones.texto(puesto, "Puesto del empleado");
    }

    public double getSalario() {
        return salario;
    }

    public final void setSalario(double salario) {
        if (!Double.isFinite(salario) || salario < 0.0) {
            throw new IllegalArgumentException("El salario no puede ser negativo ni infinito.");
        }
        this.salario = Math.round(salario * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return "Empleado[id=" + id + ", nombre=" + nombre + ", puesto=" + puesto + "]";
    }
}
