package sistemaventas;

import java.io.Serializable;

public class Existencia implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigoProducto;
    private String idBodega;
    private int cantidad;

    public Existencia(String codigoProducto, String idBodega, int cantidad) {
        setCodigoProducto(codigoProducto);
        setIdBodega(idBodega);
        setCantidad(cantidad);
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public final void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = Validaciones.texto(codigoProducto, "Código producto");
    }

    public String getIdBodega() {
        return idBodega;
    }

    public final void setIdBodega(String idBodega) {
        this.idBodega = Validaciones.texto(idBodega, "ID bodega");
    }

    public int getCantidad() {
        return cantidad;
    }

    public final void setCantidad(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("El stock de existencias no puede ser negativo.");
        }
        this.cantidad = cantidad;
    }

    public void agregar(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor que cero.");
        }
        this.cantidad = Math.addExact(this.cantidad, cantidad);
    }

    public void descontar(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor que cero.");
        }
        if (cantidad > this.cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para descontar la cantidad requerida.");
        }
        this.cantidad -= cantidad;
    }

    @Override
    public String toString() {
        return "Existencia[producto=" + codigoProducto + ", bodega=" + idBodega + ", stock=" + cantidad + "]";
    }
}
