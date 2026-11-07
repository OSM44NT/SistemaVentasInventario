package sistemaventas;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class VentanaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient SistemaVentas sistema;
    private final JTextArea salida = new JTextArea();

    public VentanaPrincipal(SistemaVentas sistema) {
        this.sistema = sistema;
        setTitle("Sistema Integral de Ventas e Inventario");
        setSize(980, 640);
        setMinimumSize(new java.awt.Dimension(850, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Productos", panel(
                boton("Registrar producto", this::registrarProducto),
                boton("Consultar catálogo", this::mostrarProductos)
        ));
        tabs.addTab("Clientes", panel(
                boton("Registrar cliente", this::registrarCliente),
                boton("Consultar clientes", this::mostrarClientes)
        ));
        tabs.addTab("Empleados", panel(
                boton("Registrar empleado", this::registrarEmpleado),
                boton("Consultar empleados", this::mostrarEmpleados)
        ));
        tabs.addTab("Bodegas", panel(
                boton("Registrar bodega", this::registrarBodega),
                boton("Consultar bodegas", this::mostrarBodegas)
        ));
        tabs.addTab("Inventario", panel(
                boton("Agregar stock", this::agregarStock),
                boton("Consultar existencias", this::mostrarInventario)
        ));
        tabs.addTab("Ventas", panel(
                boton("Nueva orden", this::crearOrden),
                boton("Completar orden", () -> cambiarEstadoOrden(true)),
                boton("Cancelar orden", () -> cambiarEstadoOrden(false)),
                boton("Consultar órdenes", this::mostrarOrdenes)
        ));

        salida.setEditable(false);
        salida.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        salida.setMargin(new java.awt.Insets(10, 10, 10, 10));

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 6));
        JButton btnLimpiar = new JButton("Limpiar consola");
        btnLimpiar.addActionListener(e -> salida.setText(""));
        panelInferior.add(btnLimpiar);

        add(tabs, BorderLayout.NORTH);
        add(new JScrollPane(salida), BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        salida.setText("========================================================================================================\n"
                + " SISTEMA DE VENTAS E INVENTARIO - LISTO PARA OPERAR\n"
                + "========================================================================================================\n"
                + " Registre productos, clientes, empleados y bodegas antes de gestionar stock y ventas.\n"
                + " Utilice las pestañas superiores para navegar entre los módulos.\n");
    }

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    private JButton boton(String texto, Accion accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> {
            try {
                accion.ejecutar();
            } catch (Exception ex) {
                String msg = ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error inesperado.";
                mensaje("Error: " + msg);
            }
        });
        return b;
    }

    private JPanel panel(JButton... botones) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        for (JButton b : botones) {
            p.add(b);
        }
        return p;
    }

    private String texto(String mensaje) {
        String v = JOptionPane.showInputDialog(this, mensaje);
        if (v == null) return null;
        if (v.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo solicitado no puede quedar vacío.");
        }
        return v.trim();
    }

    private Double dinero(String mensaje, boolean permitirCero) {
        String s = texto(mensaje);
        if (s == null) return null;
        try {
            double n = Double.parseDouble(s.replace(",", "."));
            if (!Double.isFinite(n) || n < 0.0 || (!permitirCero && n == 0.0)) {
                throw new NumberFormatException();
            }
            return n;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingresa un importe numérico válido (mayor que 0).");
        }
    }

    private int entero(String mensaje) {
        String s = texto(mensaje);
        if (s == null) return -1;
        try {
            int n = Integer.parseInt(s);
            if (n <= 0) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingresa un número entero positivo mayor que cero.");
        }
    }

    private void mensaje(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private void registrarProducto() throws IOException {
        String codigo = texto("Código del producto (ej. PROD-01):");
        if (codigo == null) return;
        String nombre = texto("Nombre comercial del producto:");
        if (nombre == null) return;
        String desc = texto("Descripción detallada:");
        if (desc == null) return;
        String marca = texto("Marca:");
        if (marca == null) return;
        Double precio = dinero("Precio unitario en Quetzales (Q):", false);
        if (precio == null) return;

        sistema.registrarProducto(new Producto(codigo, nombre, desc, marca, precio));
        mensaje("Producto '" + codigo + "' registrado exitosamente.");
        mostrarProductos();
    }

    private void registrarCliente() throws IOException {
        String dpi = texto("DPI del cliente (13 dígitos numéricos):");
        if (dpi == null) return;
        String nombre = texto("Nombre completo:");
        if (nombre == null) return;
        String direccion = texto("Dirección de residencia:");
        if (direccion == null) return;
        String fecha = texto("Fecha de nacimiento (AAAA-MM-DD):");
        if (fecha == null) return;
        LocalDate nacimiento;
        try {
            nacimiento = LocalDate.parse(fecha);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Utilice el formato AAAA-MM-DD.");
        }
        String[] opciones = {"Regular", "VIP"};
        int t = JOptionPane.showOptionDialog(this, "Seleccione la categoría del cliente:", "Tipo de Cliente",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (t < 0) return;

        sistema.registrarCliente(new Cliente(dpi, nombre, direccion, nacimiento, opciones[t]));
        mensaje("Cliente con DPI " + dpi + " registrado exitosamente.");
        mostrarClientes();
    }

    private void registrarEmpleado() throws IOException {
        String id = texto("ID de empleado (ej. EMP-01):");
        if (id == null) return;
        String nombre = texto("Nombre completo:");
        if (nombre == null) return;
        String dpi = texto("DPI (13 dígitos numéricos):");
        if (dpi == null) return;
        String direccion = texto("Dirección:");
        if (direccion == null) return;
        String puesto = texto("Puesto o cargo:");
        if (puesto == null) return;
        Double salario = dinero("Salario mensual en Quetzales (Q):", true);
        if (salario == null) return;

        sistema.registrarEmpleado(new Empleado(id, nombre, dpi, direccion, puesto, salario));
        mensaje("Empleado '" + id + "' registrado exitosamente.");
        mostrarEmpleados();
    }

    private void registrarBodega() throws IOException {
        String id = texto("ID de la bodega (ej. BOD-01):");
        if (id == null) return;
        String emp = texto("ID del empleado responsable:");
        if (emp == null) return;
        int capacidad = entero("Capacidad máxima de almacenamiento (unidades):");
        if (capacidad < 0) return;
        String direccion = texto("Ubicación o dirección física:");
        if (direccion == null) return;

        sistema.registrarBodega(new Bodega(id, emp, capacidad, direccion));
        mensaje("Bodega '" + id + "' registrada exitosamente.");
        mostrarBodegas();
    }

    private void agregarStock() throws IOException {
        String codigo = texto("Código del producto a abastecer:");
        if (codigo == null) return;
        String bodega = texto("ID de bodega de destino:");
        if (bodega == null) return;
        int cantidad = entero("Cantidad de unidades a ingresar:");
        if (cantidad < 0) return;

        sistema.agregarStock(codigo, bodega, cantidad);
        mensaje("Stock actualizado con éxito en la bodega '" + bodega + "'.");
        mostrarInventario();
    }

    private void crearOrden() throws IOException {
        String dpi = texto("DPI del cliente para la orden:");
        if (dpi == null) return;

        OrdenCompra orden = sistema.crearOrden(dpi);

        while (true) {
            String codigo = JOptionPane.showInputDialog(this,
                    "Orden: " + orden.getId() + "\n"
                            + "Total acumulado: Q" + String.format(Locale.US, "%.2f", orden.getTotal()) + "\n\n"
                            + "Código del producto (Presione Cancelar para finalizar):");
            if (codigo == null) {
                break;
            }
            codigo = codigo.trim();
            if (codigo.isEmpty()) {
                continue;
            }

            String bodega = texto("ID de la bodega desde donde se despacha el producto:");
            if (bodega == null) {
                break;
            }

            int cantidad = entero("Cantidad de unidades a vender:");
            if (cantidad < 0) {
                break;
            }

            // Captura de errores sin cerrar el flujo de agregar productos
            try {
                sistema.agregarProductoOrden(orden.getId(), codigo, bodega, cantidad);
                mensaje("Producto agregado a la orden.\nSubtotal actual de la orden: Q"
                        + String.format(Locale.US, "%.2f", orden.getTotal()));
                // Actualizar referencia de la orden
                String idActual = orden.getId();
                orden = sistema.getOrdenes().stream()
                        .filter(o -> o.getId().equalsIgnoreCase(idActual))
                        .findFirst()
                        .orElse(orden);
            } catch (Exception ex) {
                mensaje("No se pudo agregar el producto:\n" + ex.getMessage()
                        + "\n\nPuede continuar agregando otros productos o corregir los datos.");
            }
        }

        // Eliminación de órdenes huérfanas vacías
        if (orden.getProductos().isEmpty()) {
            sistema.eliminarOrdenSiVacia(orden.getId());
            mensaje("Creación de orden finalizada sin productos. La orden vacía fue descartada.");
        } else {
            mensaje("Orden registrada exitosamente:\nID: " + orden.getId()
                    + "\nTotal final: Q" + String.format(Locale.US, "%.2f", orden.getTotal())
                    + "\nÍtems agregados: " + orden.getProductos().size()
                    + "\nEstado: PENDIENTE (Utilice 'Completar orden' para procesar la entrega).");
            mostrarOrdenes();
        }
    }

    private String seleccionarOrdenPendiente() {
        List<OrdenCompra> pendientes = sistema.getOrdenes().stream()
                .filter(o -> "PENDIENTE".equalsIgnoreCase(o.getEstado()))
                .collect(Collectors.toList());

        if (pendientes.isEmpty()) {
            mensaje("No hay órdenes pendientes en este momento.");
            return null;
        }

        String[] opciones = pendientes.stream()
                .map(o -> o.getId() + " | Cliente: " + o.getDpiCliente()
                        + " | Q" + String.format(Locale.US, "%.2f", o.getTotal())
                        + " (" + o.getProductos().size() + " ítems)")
                .toArray(String[]::new);

        String seleccion = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione la orden pendiente a procesar:",
                "Gestión de Órdenes Pendientes",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (seleccion == null) return null;
        return seleccion.split(" \\| ")[0].trim();
    }

    private void cambiarEstadoOrden(boolean completar) throws IOException {
        String id = seleccionarOrdenPendiente();
        if (id == null) return;

        if (completar) {
            sistema.completarOrden(id);
            mensaje("Venta completada con éxito.\nEl stock fue descontado y la orden '" + id + "' quedó COMPLETADA.");
        } else {
            sistema.cancelarOrden(id);
            mensaje("La orden '" + id + "' fue cancelada.\nNo se realizaron modificaciones sobre el stock.");
        }
        mostrarOrdenes();
    }

    private void mostrarProductos() {
        StringBuilder s = new StringBuilder();
        s.append("========================================================================================================\n");
        s.append(" CATÁLOGO DE PRODUCTOS REGISTRADOS\n");
        s.append("========================================================================================================\n");
        s.append(String.format("%-14s | %-28s | %-18s | %-14s | %s%n",
                "CÓDIGO", "NOMBRE", "MARCA", "PRECIO UNIT.", "DESCRIPCIÓN"));
        s.append("--------------------------------------------------------------------------------------------------------\n");

        List<Producto> lista = sistema.getProductos();
        if (lista.isEmpty()) {
            s.append("  (No hay productos registrados en el sistema)\n");
        } else {
            for (Producto p : lista) {
                s.append(String.format(Locale.US, "%-14s | %-28s | %-18s | Q %11.2f | %s%n",
                        p.getCodigo(),
                        truncar(p.getNombre(), 28),
                        truncar(p.getMarca(), 18),
                        p.getPrecio(),
                        p.getDescripcion()));
            }
            s.append("--------------------------------------------------------------------------------------------------------\n");
            s.append("Total de productos registrados: ").append(lista.size()).append("\n");
        }
        salida.setText(s.toString());
    }

    private void mostrarClientes() {
        StringBuilder s = new StringBuilder();
        s.append("========================================================================================================\n");
        s.append(" LISTADO GENERAL DE CLIENTES\n");
        s.append("========================================================================================================\n");
        s.append(String.format("%-16s | %-28s | %-10s | %-12s | %s%n",
                "DPI", "NOMBRE COMPLETO", "TIPO", "FECHA NAC.", "DIRECCIÓN"));
        s.append("--------------------------------------------------------------------------------------------------------\n");

        List<Cliente> lista = sistema.getClientes();
        if (lista.isEmpty()) {
            s.append("  (No hay clientes registrados en el sistema)\n");
        } else {
            for (Cliente c : lista) {
                s.append(String.format("%-16s | %-28s | %-10s | %-12s | %s%n",
                        c.getDpi(),
                        truncar(c.getNombre(), 28),
                        c.getTipo(),
                        c.getFechaNacimiento().toString(),
                        c.getDireccion()));
            }
            s.append("--------------------------------------------------------------------------------------------------------\n");
            s.append("Total de clientes registrados: ").append(lista.size()).append("\n");
        }
        salida.setText(s.toString());
    }

    private void mostrarEmpleados() {
        StringBuilder s = new StringBuilder();
        s.append("========================================================================================================\n");
        s.append(" LISTADO DE EMPLEADOS REGISTRADOS\n");
        s.append("========================================================================================================\n");
        s.append(String.format("%-12s | %-28s | %-16s | %-18s | %-14s%n",
                "ID EMPLEADO", "NOMBRE COMPLETO", "DPI", "PUESTO", "SALARIO MENSUAL"));
        s.append("--------------------------------------------------------------------------------------------------------\n");

        List<Empleado> lista = sistema.getEmpleados();
        if (lista.isEmpty()) {
            s.append("  (No hay empleados registrados en el sistema)\n");
        } else {
            for (Empleado e : lista) {
                s.append(String.format(Locale.US, "%-12s | %-28s | %-16s | %-18s | Q %11.2f%n",
                        e.getId(),
                        truncar(e.getNombre(), 28),
                        e.getDpi(),
                        truncar(e.getPuesto(), 18),
                        e.getSalario()));
            }
            s.append("--------------------------------------------------------------------------------------------------------\n");
            s.append("Total de empleados registrados: ").append(lista.size()).append("\n");
        }
        salida.setText(s.toString());
    }

    private void mostrarBodegas() {
        StringBuilder s = new StringBuilder();
        s.append("========================================================================================================\n");
        s.append(" CONTROL DE BODEGAS Y CAPACIDAD DE ALMACENAMIENTO\n");
        s.append("========================================================================================================\n");
        s.append(String.format("%-12s | %-32s | %-12s | %-12s | %-10s | %s%n",
                "ID BODEGA", "RESPONSABLE", "CAPACIDAD", "OCUPACIÓN", "% OCUPADO", "UBICACIÓN"));
        s.append("--------------------------------------------------------------------------------------------------------\n");

        List<Bodega> lista = sistema.getBodegas();
        if (lista.isEmpty()) {
            s.append("  (No hay bodegas registradas en el sistema)\n");
        } else {
            for (Bodega b : lista) {
                Empleado emp = sistema.buscarEmpleado(b.getIdEmpleado());
                String responsable = emp != null ? b.getIdEmpleado() + " (" + emp.getNombre() + ")" : b.getIdEmpleado();
                int ocupacion = sistema.obtenerOcupacionBodega(b.getId());
                double porcentaje = (b.getCapacidad() > 0) ? (ocupacion * 100.0) / b.getCapacidad() : 0.0;

                s.append(String.format(Locale.US, "%-12s | %-32s | %8d u. | %8d u. | %7.1f%% | %s%n",
                        b.getId(),
                        truncar(responsable, 32),
                        b.getCapacidad(),
                        ocupacion,
                        porcentaje,
                        b.getDireccion()));
            }
            s.append("--------------------------------------------------------------------------------------------------------\n");
            s.append("Total de bodegas: ").append(lista.size()).append("\n");
        }
        salida.setText(s.toString());
    }

    private void mostrarInventario() {
        StringBuilder s = new StringBuilder();
        s.append("========================================================================================================\n");
        s.append(" CONTROL DE EXISTENCIAS EN INVENTARIO\n");
        s.append("========================================================================================================\n");
        s.append(String.format("%-14s | %-28s | %-12s | %-12s | %-14s | %-14s%n",
                "CÓDIGO", "PRODUCTO", "ID BODEGA", "STOCK DISP.", "PRECIO UNIT.", "VALOR TOTAL"));
        s.append("--------------------------------------------------------------------------------------------------------\n");

        List<Existencia> lista = sistema.getExistencias();
        if (lista.isEmpty()) {
            s.append("  (No hay existencias registradas en el inventario)\n");
        } else {
            int unidadesTotales = 0;
            double valuacionTotal = 0.0;

            for (Existencia x : lista) {
                Producto p = sistema.buscarProducto(x.getCodigoProducto());
                String nombreProd = p != null ? p.getNombre() : "(Desconocido)";
                double precio = p != null ? p.getPrecio() : 0.0;
                double valorFila = Math.round(precio * x.getCantidad() * 100.0) / 100.0;

                unidadesTotales = Math.addExact(unidadesTotales, x.getCantidad());
                valuacionTotal += valorFila;

                s.append(String.format(Locale.US, "%-14s | %-28s | %-12s | %8d u. | Q %11.2f | Q %11.2f%n",
                        x.getCodigoProducto(),
                        truncar(nombreProd, 28),
                        x.getIdBodega(),
                        x.getCantidad(),
                        precio,
                        valorFila));
            }
            s.append("--------------------------------------------------------------------------------------------------------\n");
            s.append(String.format(Locale.US, "TOTAL UNIDADES EN STOCK: %d u.  |  VALORACIÓN TOTAL DEL INVENTARIO: Q %.2f%n",
                    unidadesTotales, valuacionTotal));
        }
        salida.setText(s.toString());
    }

    private void mostrarOrdenes() {
        StringBuilder s = new StringBuilder();
        s.append("========================================================================================================\n");
        s.append(" HISTORIAL COMPLETO DE ÓRDENES DE VENTA\n");
        s.append("========================================================================================================\n");

        List<OrdenCompra> lista = sistema.getOrdenes();
        if (lista.isEmpty()) {
            s.append("  (No hay órdenes registradas en el sistema)\n");
        } else {
            for (OrdenCompra o : lista) {
                Cliente c = sistema.buscarCliente(o.getDpiCliente());
                String nombreCliente = c != null ? c.getNombre() : "(Cliente no registrado)";

                s.append(String.format("ORDEN: %-14s | FECHA: %-10s | ESTADO: %-11s | CLIENTE: %s (%s)%n",
                        o.getId(),
                        o.getFecha().toString(),
                        o.getEstado(),
                        o.getDpiCliente(),
                        nombreCliente));

                Map<String, Integer> prods = o.getProductos();
                if (prods.isEmpty()) {
                    s.append("   └─ (Sin productos registrados)\n");
                } else {
                    s.append("   DETALLE DE PRODUCTOS:\n");
                    for (Map.Entry<String, Integer> item : prods.entrySet()) {
                        String[] partes = sistema.separarClave(item.getKey());
                        Producto p = sistema.buscarProducto(partes[0]);
                        String nombre = p != null ? p.getNombre() : partes[0];
                        double precio = p != null ? p.getPrecio() : 0.0;
                        int cant = item.getValue();
                        double subtotal = Math.round(precio * cant * 100.0) / 100.0;

                        s.append(String.format(Locale.US,
                                "     • [%s] %-24s | Bodega: %-8s | Cant: %4d u. x Q %9.2f = Q %10.2f%n",
                                partes[0],
                                truncar(nombre, 24),
                                partes[1],
                                cant,
                                precio,
                                subtotal));
                    }
                }
                s.append(String.format(Locale.US, "   TOTAL ORDEN: Q %.2f%n", o.getTotal()));
                s.append("--------------------------------------------------------------------------------------------------------\n");
            }
            s.append("Total de órdenes registradas: ").append(lista.size()).append("\n");
        }
        salida.setText(s.toString());
    }

    private String truncar(String texto, int longitudMaxima) {
        if (texto == null) return "";
        if (texto.length() <= longitudMaxima) return texto;
        return texto.substring(0, longitudMaxima - 3) + "...";
    }
}
