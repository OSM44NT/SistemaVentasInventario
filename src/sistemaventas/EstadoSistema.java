package sistemaventas;

import java.io.Serializable;
import java.util.ArrayList;

public class EstadoSistema implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ArrayList<Producto> productos = new ArrayList<>();
    private final ArrayList<Cliente> clientes = new ArrayList<>();
    private final ArrayList<Empleado> empleados = new ArrayList<>();
    private final ArrayList<Bodega> bodegas = new ArrayList<>();
    private final ArrayList<Existencia> existencias = new ArrayList<>();
    private final ArrayList<OrdenCompra> ordenes = new ArrayList<>();

    public ArrayList<Producto> getProductos() {
        return productos;
    }

    public ArrayList<Cliente> getClientes() {
        return clientes;
    }

    public ArrayList<Empleado> getEmpleados() {
        return empleados;
    }

    public ArrayList<Bodega> getBodegas() {
        return bodegas;
    }

    public ArrayList<Existencia> getExistencias() {
        return existencias;
    }

    public ArrayList<OrdenCompra> getOrdenes() {
        return ordenes;
    }
}
