package sistemaventas;

import java.io.Serializable;

public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigo;
    private String nombre;
    private String descripcion;
    private String marca;
    private double precio;

    public Producto(String codigo, String nombre, String descripcion, String marca, double precio) {
        setCodigo(codigo);
        setNombre(nombre);
        setDescripcion(descripcion);
        setMarca(marca);
        setPrecio(precio);
    }

    public String getCodigo() {
        return codigo;
    }

    public final void setCodigo(String codigo) {
        this.codigo = Validaciones.texto(codigo, "Código de producto");
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        this.nombre = Validaciones.texto(nombre, "Nombre del producto");
    }

    public String getDescripcion() {
        return descripcion;
    }

    public final void setDescripcion(String descripcion) {
        this.descripcion = Validaciones.texto(descripcion, "Descripción del producto");
    }

    public String getMarca() {
        return marca;
    }

    public final void setMarca(String marca) {
        this.marca = Validaciones.texto(marca, "Marca");
    }

    public double getPrecio() {
        return precio;
    }

    public final void setPrecio(double precio) {
        if (!Double.isFinite(precio) || precio <= 0.0) {
            throw new IllegalArgumentException("El precio del producto debe ser mayor que cero.");
        }
        this.precio = Math.round(precio * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return "Producto[codigo=" + codigo + ", nombre=" + nombre + ", precio=" + precio + "]";
    }
}
