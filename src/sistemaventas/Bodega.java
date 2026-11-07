package sistemaventas;

import java.io.Serializable;

public class Bodega implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String idEmpleado;
    private int capacidad;
    private String direccion;

    public Bodega(String id, String idEmpleado, int capacidad, String direccion) {
        setId(id);
        setIdEmpleado(idEmpleado);
        setCapacidad(capacidad);
        setDireccion(direccion);
    }

    public String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = Validaciones.texto(id, "ID bodega");
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public final void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = Validaciones.texto(idEmpleado, "Empleado responsable");
    }

    public int getCapacidad() {
        return capacidad;
    }

    public final void setCapacidad(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad de la bodega debe ser mayor que cero.");
        }
        this.capacidad = capacidad;
    }

    public String getDireccion() {
        return direccion;
    }

    public final void setDireccion(String direccion) {
        this.direccion = Validaciones.texto(direccion, "Dirección de bodega");
    }

    @Override
    public String toString() {
        return "Bodega[id=" + id + ", idEmpleado=" + idEmpleado + ", capacidad=" + capacidad + "]";
    }
}
