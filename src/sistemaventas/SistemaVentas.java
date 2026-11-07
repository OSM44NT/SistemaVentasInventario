package sistemaventas;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SistemaVentas {
    private final ArchivoService archivos = new ArchivoService();
    private EstadoSistema estado;

    public SistemaVentas() throws IOException {
        estado = archivos.cargar();
        validarEstado();
    }

    public List<Producto> getProductos() {
        return Collections.unmodifiableList(estado.getProductos());
    }

    public List<Cliente> getClientes() {
        return Collections.unmodifiableList(estado.getClientes());
    }

    public List<Empleado> getEmpleados() {
        return Collections.unmodifiableList(estado.getEmpleados());
    }

    public List<Bodega> getBodegas() {
        return Collections.unmodifiableList(estado.getBodegas());
    }

    public List<Existencia> getExistencias() {
        return Collections.unmodifiableList(estado.getExistencias());
    }

    public List<OrdenCompra> getOrdenes() {
        return Collections.unmodifiableList(estado.getOrdenes());
    }

    private void persistir() throws IOException {
        archivos.guardar(estado);
    }

    private void validarEstado() throws IOException {
        Set<String> codigos = new HashSet<>();
        for (Producto p : estado.getProductos()) {
            if (!codigos.add(p.getCodigo().toUpperCase())) {
                throw new IOException("Hay códigos de producto duplicados en los datos almacenados.");
            }
        }

        Set<String> dpisClientes = new HashSet<>();
        for (Cliente c : estado.getClientes()) {
            if (!dpisClientes.add(c.getDpi())) {
                throw new IOException("Hay DPI de cliente duplicados en los datos almacenados.");
            }
        }

        Set<String> idsEmp = new HashSet<>();
        Set<String> dpisEmp = new HashSet<>();
        for (Empleado e : estado.getEmpleados()) {
            if (!idsEmp.add(e.getId().toUpperCase())) {
                throw new IOException("Hay IDs de empleado duplicados en los datos almacenados.");
            }
            if (!dpisEmp.add(e.getDpi())) {
                throw new IOException("Hay DPI de empleado duplicados en los datos almacenados.");
            }
        }

        Set<String> idsBod = new HashSet<>();
        for (Bodega b : estado.getBodegas()) {
            if (!idsBod.add(b.getId().toUpperCase())) {
                throw new IOException("Hay IDs de bodega duplicados en los datos almacenados.");
            }
            if (buscarEmpleado(b.getIdEmpleado()) == null) {
                throw new IOException("La bodega " + b.getId() + " hace referencia a un empleado inexistente.");
            }
        }

        Set<String> clavesExistencia = new HashSet<>();
        for (Existencia x : estado.getExistencias()) {
            if (buscarProducto(x.getCodigoProducto()) == null || buscarBodega(x.getIdBodega()) == null) {
                throw new IOException("Hay existencias con producto o bodega inexistente en los datos.");
            }
            String clave = x.getCodigoProducto().toUpperCase() + "|" + x.getIdBodega().toUpperCase();
            if (!clavesExistencia.add(clave)) {
                throw new IOException("Hay existencias duplicadas para el producto " + x.getCodigoProducto()
                        + " en la bodega " + x.getIdBodega() + ".");
            }
        }

        for (OrdenCompra o : estado.getOrdenes()) {
            if (buscarCliente(o.getDpiCliente()) == null) {
                throw new IOException("La orden " + o.getId() + " hace referencia a un cliente inexistente.");
            }
            for (String clave : o.getProductos().keySet()) {
                String[] partes;
                try {
                    partes = separarClave(clave);
                } catch (RuntimeException ex) {
                    throw new IOException("Hay un detalle de orden con formato inválido.", ex);
                }
                if (buscarProducto(partes[0]) == null || buscarBodega(partes[1]) == null) {
                    throw new IOException("La orden " + o.getId() + " hace referencia a un producto o bodega inexistente.");
                }
            }
        }
    }

    public Producto buscarProducto(String codigo) {
        if (codigo == null) return null;
        String c = codigo.trim();
        if (c.isEmpty()) return null;
        for (Producto p : estado.getProductos()) {
            if (p.getCodigo().equalsIgnoreCase(c)) {
                return p;
            }
        }
        return null;
    }

    public Cliente buscarCliente(String dpi) {
        if (dpi == null) return null;
        String d = dpi.trim();
        if (d.isEmpty()) return null;
        for (Cliente c : estado.getClientes()) {
            if (c.getDpi().equals(d)) {
                return c;
            }
        }
        return null;
    }

    public Empleado buscarEmpleado(String id) {
        if (id == null) return null;
        String i = id.trim();
        if (i.isEmpty()) return null;
        for (Empleado e : estado.getEmpleados()) {
            if (e.getId().equalsIgnoreCase(i)) {
                return e;
            }
        }
        return null;
    }

    public Bodega buscarBodega(String id) {
        if (id == null) return null;
        String i = id.trim();
        if (i.isEmpty()) return null;
        for (Bodega b : estado.getBodegas()) {
            if (b.getId().equalsIgnoreCase(i)) {
                return b;
            }
        }
        return null;
    }

    public Existencia buscarExistencia(String codigo, String bodega) {
        if (codigo == null || bodega == null) return null;
        String c = codigo.trim();
        String b = bodega.trim();
        if (c.isEmpty() || b.isEmpty()) return null;
        for (Existencia x : estado.getExistencias()) {
            if (x.getCodigoProducto().equalsIgnoreCase(c) && x.getIdBodega().equalsIgnoreCase(b)) {
                return x;
            }
        }
        return null;
    }

    public OrdenCompra buscarOrden(String id) {
        if (id == null) return null;
        String i = id.trim();
        if (i.isEmpty()) return null;
        for (OrdenCompra o : estado.getOrdenes()) {
            if (o.getId().equalsIgnoreCase(i)) {
                return o;
            }
        }
        return null;
    }

    public int obtenerOcupacionBodega(String idBodega) {
        if (idBodega == null) return 0;
        String bId = idBodega.trim();
        if (bId.isEmpty()) return 0;
        int ocupacion = 0;
        for (Existencia ex : estado.getExistencias()) {
            if (ex.getIdBodega().equalsIgnoreCase(bId)) {
                ocupacion = Math.addExact(ocupacion, ex.getCantidad());
            }
        }
        return ocupacion;
    }

    public String claveDetalle(String codigo, String bodega) {
        if (codigo == null || bodega == null) {
            throw new IllegalArgumentException("Código de producto y bodega no pueden ser nulos.");
        }
        return codigo.trim().toUpperCase() + "@@" + bodega.trim().toUpperCase();
    }

    public String[] separarClave(String clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave del detalle no puede ser nula.");
        }
        int pos = clave.indexOf("@@");
        if (pos <= 0 || pos + 2 >= clave.length()) {
            throw new IllegalStateException("Detalle de orden con formato inválido: " + clave);
        }
        return new String[]{
                clave.substring(0, pos).trim().toUpperCase(),
                clave.substring(pos + 2).trim().toUpperCase()
        };
    }

    public void registrarProducto(Producto p) throws IOException {
        if (p == null) throw new IllegalArgumentException("El producto no puede ser nulo.");
        if (buscarProducto(p.getCodigo()) != null) {
            throw new IllegalArgumentException("El código de producto '" + p.getCodigo() + "' ya existe.");
        }
        estado.getProductos().add(p);
        try {
            persistir();
        } catch (IOException ex) {
            estado.getProductos().remove(p);
            throw ex;
        }
    }

    public void registrarCliente(Cliente c) throws IOException {
        if (c == null) throw new IllegalArgumentException("El cliente no puede ser nulo.");
        if (buscarCliente(c.getDpi()) != null) {
            throw new IllegalArgumentException("El DPI '" + c.getDpi() + "' ya pertenece a un cliente registrado.");
        }
        estado.getClientes().add(c);
        try {
            persistir();
        } catch (IOException ex) {
            estado.getClientes().remove(c);
            throw ex;
        }
    }

    public void registrarEmpleado(Empleado e) throws IOException {
        if (e == null) throw new IllegalArgumentException("El empleado no puede ser nulo.");
        if (buscarEmpleado(e.getId()) != null) {
            throw new IllegalArgumentException("El ID de empleado '" + e.getId() + "' ya existe.");
        }
        for (Empleado otro : estado.getEmpleados()) {
            if (otro.getDpi().equals(e.getDpi())) {
                throw new IllegalArgumentException("El DPI '" + e.getDpi() + "' ya pertenece al empleado " + otro.getNombre() + ".");
            }
        }
        estado.getEmpleados().add(e);
        try {
            persistir();
        } catch (IOException ex) {
            estado.getEmpleados().remove(e);
            throw ex;
        }
    }

    public void registrarBodega(Bodega b) throws IOException {
        if (b == null) throw new IllegalArgumentException("La bodega no puede ser nula.");
        if (buscarBodega(b.getId()) != null) {
            throw new IllegalArgumentException("El ID de bodega '" + b.getId() + "' ya existe.");
        }
        if (buscarEmpleado(b.getIdEmpleado()) == null) {
            throw new IllegalArgumentException("No se encontró el empleado responsable con ID '" + b.getIdEmpleado() + "'.");
        }
        estado.getBodegas().add(b);
        try {
            persistir();
        } catch (IOException ex) {
            estado.getBodegas().remove(b);
            throw ex;
        }
    }

    public void agregarStock(String codigo, String idBodega, int cantidad) throws IOException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor que cero.");
        }
        Producto p = buscarProducto(codigo);
        if (p == null) {
            throw new IllegalArgumentException("El producto indicado no existe.");
        }
        Bodega b = buscarBodega(idBodega);
        if (b == null) {
            throw new IllegalArgumentException("La bodega indicada no existe.");
        }

        int ocupacionActual = obtenerOcupacionBodega(b.getId());
        if (Math.addExact(ocupacionActual, cantidad) > b.getCapacidad()) {
            int espacioDisponible = Math.max(0, b.getCapacidad() - ocupacionActual);
            throw new IllegalArgumentException("La cantidad supera la capacidad disponible de la bodega ("
                    + espacioDisponible + " unidades de espacio restante).");
        }

        Existencia x = buscarExistencia(codigo, idBodega);
        boolean nueva = (x == null);
        int cantidadAnterior = nueva ? 0 : x.getCantidad();

        if (nueva) {
            x = new Existencia(p.getCodigo().toUpperCase(), b.getId().toUpperCase(), cantidad);
            estado.getExistencias().add(x);
        } else {
            x.agregar(cantidad);
        }

        try {
            persistir();
        } catch (IOException ex) {
            if (nueva) {
                estado.getExistencias().remove(x);
            } else {
                x.setCantidad(cantidadAnterior);
            }
            throw ex;
        }
    }

    public OrdenCompra crearOrden(String dpiCliente) throws IOException {
        Cliente c = buscarCliente(dpiCliente);
        if (c == null) {
            throw new IllegalArgumentException("No se encontró ningún cliente registrado con el DPI proporcionado.");
        }
        String id = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        OrdenCompra o = new OrdenCompra(id, c.getDpi());
        estado.getOrdenes().add(o);
        try {
            persistir();
        } catch (IOException ex) {
            estado.getOrdenes().remove(o);
            throw ex;
        }
        return o;
    }

    public void agregarProductoOrden(String idOrden, String codigo, String idBodega, int cantidad) throws IOException {
        OrdenCompra o = buscarOrden(idOrden);
        if (o == null) {
            throw new IllegalArgumentException("Orden no encontrada.");
        }
        if (!"PENDIENTE".equalsIgnoreCase(o.getEstado())) {
            throw new IllegalStateException("La orden ya fue procesada y no permite modificaciones.");
        }
        Producto p = buscarProducto(codigo);
        if (p == null) {
            throw new IllegalArgumentException("El producto '" + codigo + "' no existe en el catálogo.");
        }
        Bodega b = buscarBodega(idBodega);
        if (b == null) {
            throw new IllegalArgumentException("La bodega '" + idBodega + "' no existe.");
        }
        Existencia x = buscarExistencia(codigo, idBodega);
        if (x == null || cantidad <= 0 || x.getCantidad() < cantidad) {
            int disponible = (x == null) ? 0 : x.getCantidad();
            throw new IllegalArgumentException("Stock insuficiente en la bodega " + b.getId()
                    + ". Disponible: " + disponible + ", Solicitado: " + cantidad);
        }

        String clave = claveDetalle(p.getCodigo(), b.getId());
        int acumuladoEnOrden = o.getProductos().getOrDefault(clave, 0);
        if (Math.addExact(acumuladoEnOrden, cantidad) > x.getCantidad()) {
            throw new IllegalArgumentException("La cantidad acumulada en la orden ("
                    + (acumuladoEnOrden + cantidad) + ") superaría el stock disponible en bodega ("
                    + x.getCantidad() + ").");
        }

        o.agregarProducto(clave, cantidad, p.getPrecio());
        try {
            persistir();
        } catch (IOException ex) {
            o.quitarProducto(clave, cantidad, p.getPrecio());
            throw ex;
        }
    }

    public void completarOrden(String idOrden) throws IOException {
        OrdenCompra o = buscarOrden(idOrden);
        if (o == null) {
            throw new IllegalArgumentException("Orden no encontrada.");
        }
        if (!"PENDIENTE".equalsIgnoreCase(o.getEstado())) {
            throw new IllegalStateException("La orden ya fue procesada anteriormente.");
        }
        if (o.getProductos().isEmpty()) {
            throw new IllegalStateException("La orden debe incluir al menos un producto para ser completada.");
        }

        // Acumulación y mapeo estricto por existencia física para evitar sobregiro/overdraft
        Map<Existencia, Integer> demandasPorExistencia = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> item : o.getProductos().entrySet()) {
            String[] partes = separarClave(item.getKey());
            Existencia x = buscarExistencia(partes[0], partes[1]);
            if (x == null) {
                throw new IllegalArgumentException("No se encontró inventario para el producto "
                        + partes[0] + " en la bodega " + partes[1] + ".");
            }
            demandasPorExistencia.merge(x, item.getValue(), Math::addExact);
        }

        // Validación total previa contra el stock actual disponible
        for (Map.Entry<Existencia, Integer> entry : demandasPorExistencia.entrySet()) {
            Existencia x = entry.getKey();
            int requerido = entry.getValue();
            if (x.getCantidad() < requerido) {
                throw new IllegalArgumentException("Stock insuficiente para completar la venta. Producto: "
                        + x.getCodigoProducto() + " en Bodega: " + x.getIdBodega()
                        + " (Disponible: " + x.getCantidad() + ", Requerido: " + requerido + ").");
            }
        }

        // Aplicación segura de descuentos con respaldo en memoria para garantizar atomicidad ante fallo
        Map<Existencia, Integer> cantidadesAnteriores = new LinkedHashMap<>();
        for (Map.Entry<Existencia, Integer> entry : demandasPorExistencia.entrySet()) {
            Existencia x = entry.getKey();
            cantidadesAnteriores.put(x, x.getCantidad());
            x.descontar(entry.getValue());
        }

        o.completar();
        try {
            persistir();
        } catch (IOException ex) {
            // Revertir cambios en memoria
            for (Map.Entry<Existencia, Integer> entry : cantidadesAnteriores.entrySet()) {
                entry.getKey().setCantidad(entry.getValue());
            }
            o.restaurarPendiente();
            throw ex;
        }
    }

    public void cancelarOrden(String idOrden) throws IOException {
        OrdenCompra o = buscarOrden(idOrden);
        if (o == null) {
            throw new IllegalArgumentException("Orden no encontrada.");
        }
        o.cancelar();
        try {
            persistir();
        } catch (IOException ex) {
            o.restaurarPendiente();
            throw ex;
        }
    }

    public boolean eliminarOrdenSiVacia(String idOrden) throws IOException {
        OrdenCompra o = buscarOrden(idOrden);
        if (o != null && "PENDIENTE".equalsIgnoreCase(o.getEstado()) && o.getProductos().isEmpty()) {
            estado.getOrdenes().remove(o);
            persistir();
            return true;
        }
        return false;
    }
}
