package sistemaventas;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrdenCompra implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String dpiCliente;
    private String estado;
    private LocalDate fecha;
    private double total;
    private LinkedHashMap<String, Integer> productos;

    public OrdenCompra(String id, String dpiCliente) {
        this.id = Validaciones.texto(id, "ID orden");
        this.dpiCliente = Validaciones.dpi(dpiCliente, "DPI cliente");
        this.fecha = LocalDate.now();
        this.estado = "PENDIENTE";
        this.total = 0.0;
        this.productos = new LinkedHashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getDpiCliente() {
        return dpiCliente;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getTotal() {
        return total;
    }

    public Map<String, Integer> getProductos() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(productos));
    }

    public void agregarProducto(String clave, int cantidad, double precio) {
        if (!"PENDIENTE".equalsIgnoreCase(estado)) {
            throw new IllegalStateException("La orden ya fue procesada y no puede modificarse.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (!Double.isFinite(precio) || precio < 0.0) {
            throw new IllegalArgumentException("El precio debe ser un número válido no negativo.");
        }
        String claveValida = Validaciones.texto(clave, "Detalle de producto");
        productos.merge(claveValida, cantidad, Math::addExact);
        total = Math.round((total + precio * cantidad) * 100.0) / 100.0;
    }

    public void quitarProducto(String clave, int cantidad, double precio) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave del detalle no puede ser nula.");
        }
        Integer actual = productos.get(clave);
        if (actual == null || cantidad <= 0 || cantidad > actual) {
            throw new IllegalArgumentException("No se puede revertir o quitar el detalle de la orden.");
        }
        int nuevo = actual - cantidad;
        if (nuevo == 0) {
            productos.remove(clave);
        } else {
            productos.put(clave, nuevo);
        }
        total = Math.round((total - precio * cantidad) * 100.0) / 100.0;
        if (Math.abs(total) < 0.005) {
            total = 0.0;
        }
    }

    public void restaurarPendiente() {
        this.estado = "PENDIENTE";
    }

    public void completar() {
        if (!"PENDIENTE".equalsIgnoreCase(estado)) {
            throw new IllegalStateException("La orden ya fue procesada.");
        }
        if (productos.isEmpty()) {
            throw new IllegalStateException("La orden debe incluir al menos un producto.");
        }
        this.estado = "COMPLETADA";
    }

    public void cancelar() {
        if (!"PENDIENTE".equalsIgnoreCase(estado)) {
            throw new IllegalStateException("La orden ya fue procesada.");
        }
        // Corrección: NO arrojar excepción si la orden está vacía.
        this.estado = "CANCELADA";
    }

    @Override
    public String toString() {
        return "OrdenCompra[id=" + id + ", cliente=" + dpiCliente + ", estado=" + estado + ", total=" + total + "]";
    }
}
