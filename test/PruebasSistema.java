import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import sistemaventas.*;

/**
 * Suite completa de pruebas automatizadas para el Sistema de Ventas e Inventario.
 * Valida reglas de negocio, integridad de datos, manejo de inventario,
 * ciclo de vida de órdenes de compra y persistencia atómica.
 */
public class PruebasSistema {

    private static int pruebasExitosas = 0;
    private static int pruebasFallidas = 0;
    private static final List<String> detallesFallos = new ArrayList<>();

    // Variables para aislamiento de pruebas
    private static final Path CARPETA_DATOS = Paths.get("datos");
    private static final Path CARPETA_BACKUP_TEST = Paths.get("datos_test_backup_isolated");
    private static boolean habiaDatosPrevios = false;

    public static void main(String[] args) {
        System.out.println("==============================================================================");
        System.out.println("     INICIANDO SUITE DE PRUEBAS AUTOMATIZADAS - SISTEMA DE VENTAS            ");
        System.out.println("==============================================================================");
        System.out.println();

        try {
            prepararEntornoAislado();

            ejecutarPrueba("1. Validaciones y Reglas del Dominio", () -> testValidacionesDominio());
            ejecutarPrueba("2. Registro y Unicidad de Entidades", () -> testRegistroYUnicidad());
            ejecutarPrueba("3. Capacidad de Bodega y Control de Stock", () -> testCapacidadBodegaYStock());
            ejecutarPrueba("4. Ciclo de Órdenes y Descuento Exacto de Stock", () -> testCicloOrdenesYDescuentoStock());
            ejecutarPrueba("5. Cancelación de Órdenes (con productos y vacías)", () -> testCancelacionOrdenes());
            ejecutarPrueba("6. Eliminación Segura de Órdenes Vacías", () -> testEliminarOrdenVacia());
            ejecutarPrueba("7. Persistencia Atómica, Respaldo y Recuperación", () -> testPersistenciaYRecuperacion());

        } finally {
            restaurarEntornoOriginal();
        }

        System.out.println();
        System.out.println("==============================================================================");
        System.out.println("                        RESUMEN DE PRUEBAS AUTOMATIZADAS                      ");
        System.out.println("==============================================================================");
        System.out.println(" Pruebas exitosas : " + pruebasExitosas);
        System.out.println(" Pruebas fallidas : " + pruebasFallidas);
        System.out.println(" Total ejecutadas : " + (pruebasExitosas + pruebasFallidas));
        System.out.println(" Tasa de éxito    : " + String.format(Locale.US, "%.1f%%",
                (pruebasExitosas * 100.0) / Math.max(1, (pruebasExitosas + pruebasFallidas))));
        System.out.println("==============================================================================");

        if (pruebasFallidas > 0) {
            System.err.println("\n[DETALLE DE FALLOS]");
            for (String detalle : detallesFallos) {
                System.err.println(" - " + detalle);
            }
            System.exit(1);
        } else {
            System.out.println("\n>>> [RESULTADO FINAL] TODAS LAS PRUEBAS PASARON EXITOSAMENTE AL 100% <<<");
            System.exit(0);
        }
    }

    // =========================================================================
    // 1. VALIDACIONES Y REGLAS DE MODELO
    // =========================================================================
    private static void testValidacionesDominio() {
        // Producto
        Producto prod = new Producto("P-001", "Laptop Gamer", "Core i7 16GB", "Dell", 8500.50);
        afirmar(prod.getCodigo().equals("P-001"), "Código de producto correcto");
        afirmar(prod.getPrecio() == 8500.50, "Precio de producto correcto");

        afirmarExcepcion(() -> new Producto("", "Nombre", "Desc", "Marca", 10.0),
                IllegalArgumentException.class, "Rechazo de código vacío");
        afirmarExcepcion(() -> new Producto("P-002", "Nombre", "Desc", "Marca", -5.0),
                IllegalArgumentException.class, "Rechazo de precio negativo");
        afirmarExcepcion(() -> new Producto("P-002", "Nombre", "Desc", "Marca", 0.0),
                IllegalArgumentException.class, "Rechazo de precio cero");

        // Cliente
        Cliente cli = new Cliente("1234567890123", "Carlos Gómez", "Zona 10", LocalDate.of(1990, 5, 15), "Regular");
        afirmar(cli.getDpi().equals("1234567890123"), "DPI cliente asignado");
        afirmar(cli.getTipo().equals("Regular"), "Tipo regular asignado");

        afirmarExcepcion(() -> new Cliente("123", "Carlos", "Zona 1", LocalDate.of(1990, 1, 1), "Regular"),
                IllegalArgumentException.class, "Rechazo de DPI inválido (distinto de 13 dígitos)");
        afirmarExcepcion(() -> new Cliente("1234567890123", "Carlos", "Zona 1", LocalDate.now().plusDays(1), "Regular"),
                IllegalArgumentException.class, "Rechazo de fecha de nacimiento en el futuro");
        afirmarExcepcion(() -> new Cliente("1234567890123", "Carlos", "Zona 1", LocalDate.of(1990, 1, 1), "Invalido"),
                IllegalArgumentException.class, "Rechazo de tipo de cliente no soportado");

        // Empleado
        Empleado emp = new Empleado("EMP-01", "María López", "9876543210123", "Zona 4", "Gerente", 12500.0);
        afirmar(emp.getId().equals("EMP-01"), "ID empleado correcto");
        afirmar(emp.getSalario() == 12500.0, "Salario correcto");

        afirmarExcepcion(() -> new Empleado("EMP-02", "Pedro", "111", "Zona 1", "Ventas", 5000.0),
                IllegalArgumentException.class, "Rechazo de DPI empleado inválido");
        afirmarExcepcion(() -> new Empleado("EMP-02", "Pedro", "9876543210123", "Zona 1", "Ventas", -100.0),
                IllegalArgumentException.class, "Rechazo de salario negativo");

        // Bodega
        Bodega bod = new Bodega("BOD-01", "EMP-01", 1000, "Parque Industrial");
        afirmar(bod.getId().equals("BOD-01"), "ID bodega correcto");
        afirmar(bod.getCapacidad() == 1000, "Capacidad correcta");

        afirmarExcepcion(() -> new Bodega("BOD-02", "EMP-01", 0, "Dir"),
                IllegalArgumentException.class, "Rechazo de bodega con capacidad cero");
        afirmarExcepcion(() -> new Bodega("BOD-02", "EMP-01", -50, "Dir"),
                IllegalArgumentException.class, "Rechazo de bodega con capacidad negativa");

        // Existencia
        Existencia ex = new Existencia("P-001", "BOD-01", 50);
        afirmar(ex.getCantidad() == 50, "Cantidad inicial de stock correcta");
        ex.agregar(25);
        afirmar(ex.getCantidad() == 75, "Cantidad tras agregar stock correcta");
        ex.descontar(15);
        afirmar(ex.getCantidad() == 60, "Cantidad tras descontar stock correcta");

        afirmarExcepcion(() -> ex.descontar(100),
                IllegalArgumentException.class, "Rechazo de descuento superior al stock actual");
    }

    // =========================================================================
    // 2. REGISTRO Y UNICIDAD DE ENTIDADES
    // =========================================================================
    private static void testRegistroYUnicidad() throws Exception {
        limpiarCarpetaDatos();
        SistemaVentas sistema = new SistemaVentas();

        // 1. Registrar empleado primero (requerido para bodegas)
        Empleado emp1 = new Empleado("EMP-A", "Laura Pérez", "1000000000001", "Zona 1", "Encargada", 7000.0);
        sistema.registrarEmpleado(emp1);
        afirmar(sistema.getEmpleados().size() == 1, "Empleado 1 registrado");

        // Rechazo de ID empleado duplicado (mismo ID o case-insensitive)
        afirmarExcepcion(() -> sistema.registrarEmpleado(new Empleado("emp-a", "Otro", "1000000000002", "Zona 2", "Aux", 4000.0)),
                IllegalArgumentException.class, "Rechazo de ID empleado duplicado");

        // Rechazo de DPI empleado duplicado
        afirmarExcepcion(() -> sistema.registrarEmpleado(new Empleado("EMP-B", "Otro", "1000000000001", "Zona 2", "Aux", 4000.0)),
                IllegalArgumentException.class, "Rechazo de DPI empleado duplicado");

        // 2. Registrar bodega
        Bodega bod1 = new Bodega("BOD-CENTRAL", "EMP-A", 500, "Centro Logístico");
        sistema.registrarBodega(bod1);
        afirmar(sistema.getBodegas().size() == 1, "Bodega registrada");

        // Rechazo de ID bodega duplicado
        afirmarExcepcion(() -> sistema.registrarBodega(new Bodega("bod-central", "EMP-A", 300, "Otra")),
                IllegalArgumentException.class, "Rechazo de ID bodega duplicado");

        // Rechazo de bodega con empleado responsable inexistente
        afirmarExcepcion(() -> sistema.registrarBodega(new Bodega("BOD-SUR", "EMP-FANTASMA", 200, "Sur")),
                IllegalArgumentException.class, "Rechazo de bodega con empleado inexistente");

        // 3. Registrar producto
        Producto prod1 = new Producto("PROD-X", "Mouse Óptico", "Inalámbrico USB", "Logitech", 150.0);
        sistema.registrarProducto(prod1);
        afirmar(sistema.getProductos().size() == 1, "Producto registrado");

        // Rechazo de código de producto duplicado
        afirmarExcepcion(() -> sistema.registrarProducto(new Producto("prod-x", "Otro Mouse", "Cable", "Genius", 80.0)),
                IllegalArgumentException.class, "Rechazo de código de producto duplicado");

        // 4. Registrar cliente
        Cliente cli1 = new Cliente("2000000000001", "Ana Morales", "Zona 15", LocalDate.of(1995, 8, 20), "VIP");
        sistema.registrarCliente(cli1);
        afirmar(sistema.getClientes().size() == 1, "Cliente registrado");

        // Rechazo de DPI cliente duplicado
        afirmarExcepcion(() -> sistema.registrarCliente(new Cliente("2000000000001", "Ana Duplicada", "Zona 1", LocalDate.of(1990, 1, 1), "Regular")),
                IllegalArgumentException.class, "Rechazo de DPI de cliente duplicado");
    }

    // =========================================================================
    // 3. CAPACIDAD DE BODEGA Y CONTROL DE STOCK
    // =========================================================================
    private static void testCapacidadBodegaYStock() throws Exception {
        limpiarCarpetaDatos();
        SistemaVentas sistema = new SistemaVentas();

        Empleado emp = new Empleado("E1", "Juan", "1111111111111", "Dir", "Bodeguero", 4000.0);
        sistema.registrarEmpleado(emp);

        // Bodega con capacidad máxima de 100 unidades
        Bodega bodega = new Bodega("BOD-100", "E1", 100, "Zona Industrial");
        sistema.registrarBodega(bodega);

        Producto p1 = new Producto("ART-1", "Teclado", "Mecánico", "HyperX", 350.0);
        Producto p2 = new Producto("ART-2", "Monitor", "24 pulg IPS", "Samsung", 1200.0);
        sistema.registrarProducto(p1);
        sistema.registrarProducto(p2);

        // 1. Agregar stock válido (60 unidades de ART-1)
        sistema.agregarStock("ART-1", "BOD-100", 60);
        afirmar(sistema.obtenerOcupacionBodega("BOD-100") == 60, "Ocupación de bodega 60 unidades");

        // 2. Agregar stock que no sobrepasa (30 unidades de ART-2) -> Total 90 / 100
        sistema.agregarStock("ART-2", "BOD-100", 30);
        afirmar(sistema.obtenerOcupacionBodega("BOD-100") == 90, "Ocupación acumulada 90 unidades");

        // 3. Intentar agregar stock que excede la capacidad disponible (20 unidades -> 90 + 20 = 110 > 100)
        afirmarExcepcion(() -> sistema.agregarStock("ART-1", "BOD-100", 20),
                IllegalArgumentException.class, "Rechazo de stock que supera la capacidad de bodega");

        // Verificar que la ocupación se mantuvo intacta en 90
        afirmar(sistema.obtenerOcupacionBodega("BOD-100") == 90, "Ocupación se mantiene en 90 tras rechazo");

        // 4. Agregar exactamente el espacio restante (10 unidades) -> Total 100 / 100
        sistema.agregarStock("ART-1", "BOD-100", 10);
        afirmar(sistema.obtenerOcupacionBodega("BOD-100") == 100, "Bodega al 100% de capacidad");

        // 5. Intentar agregar 1 unidad más en bodega llena -> Rechazo
        afirmarExcepcion(() -> sistema.agregarStock("ART-2", "BOD-100", 1),
                IllegalArgumentException.class, "Rechazo de stock en bodega completamente llena");
    }

    // =========================================================================
    // 4. CICLO DE ÓRDENES Y DESCUENTO EXACTO DE STOCK
    // =========================================================================
    private static void testCicloOrdenesYDescuentoStock() throws Exception {
        limpiarCarpetaDatos();
        SistemaVentas sistema = new SistemaVentas();

        Empleado emp = new Empleado("EMP-V", "Valeria", "3333333333333", "Dir", "Ventas", 5000.0);
        sistema.registrarEmpleado(emp);

        Bodega bPrincipal = new Bodega("B-PRINCIPAL", "EMP-V", 1000, "Zona 12");
        Bodega bSecundaria = new Bodega("B-SECUNDARIA", "EMP-V", 500, "Zona 9");
        sistema.registrarBodega(bPrincipal);
        sistema.registrarBodega(bSecundaria);

        Producto monitor = new Producto("MON-01", "Monitor 27", "4K 144Hz", "LG", 2500.0);
        Producto teclado = new Producto("TEC-01", "Teclado", "RGB", "Razer", 400.0);
        sistema.registrarProducto(monitor);
        sistema.registrarProducto(teclado);

        Cliente cli = new Cliente("4444444444444", "Rodrigo Méndez", "Carretera a El Salvador", LocalDate.of(1988, 11, 3), "VIP");
        sistema.registrarCliente(cli);

        // Cargar stock
        sistema.agregarStock("MON-01", "B-PRINCIPAL", 10); // 10 monitores en B-PRINCIPAL
        sistema.agregarStock("TEC-01", "B-SECUNDARIA", 15); // 15 teclados en B-SECUNDARIA

        // 1. Crear Orden
        OrdenCompra orden = sistema.crearOrden("4444444444444");
        afirmar(orden != null, "Orden creada");
        afirmar(orden.getEstado().equals("PENDIENTE"), "Orden inicia en estado PENDIENTE");
        afirmar(orden.getTotal() == 0.0, "Total de orden inicia en 0");

        // 2. Agregar productos desde distintas bodegas
        sistema.agregarProductoOrden(orden.getId(), "MON-01", "B-PRINCIPAL", 3); // 3 * 2500 = 7500
        sistema.agregarProductoOrden(orden.getId(), "TEC-01", "B-SECUNDARIA", 5); // 5 * 400 = 2000

        // Verificar total acumulado: 7500 + 2000 = 9500
        afirmar(orden.getTotal() == 9500.0, "Total calculado con exactitud (Q9,500.00)");

        // 3. Verificar que durante el estado PENDIENTE el stock físico todavía NO se ha descontado
        afirmar(sistema.buscarExistencia("MON-01", "B-PRINCIPAL").getCantidad() == 10, "Stock físico de monitor sin descontar en PENDIENTE");
        afirmar(sistema.buscarExistencia("TEC-01", "B-SECUNDARIA").getCantidad() == 15, "Stock físico de teclado sin descontar en PENDIENTE");

        // 4. Intentar agregar más stock del disponible en bodega (quedan 10 - 3 = 7 disponibles para MON-01)
        afirmarExcepcion(() -> sistema.agregarProductoOrden(orden.getId(), "MON-01", "B-PRINCIPAL", 8),
                IllegalArgumentException.class, "Rechazo por exceder stock disponible en bodega");

        // 5. Completar orden
        sistema.completarOrden(orden.getId());
        afirmar(orden.getEstado().equals("COMPLETADA"), "Orden transicionó a COMPLETADA");

        // 6. Verificar descuento exacto de stock en bodegas
        Existencia exMon = sistema.buscarExistencia("MON-01", "B-PRINCIPAL");
        Existencia exTec = sistema.buscarExistencia("TEC-01", "B-SECUNDARIA");
        afirmar(exMon.getCantidad() == 7, "Stock de monitor descontado exactamente a 7 (10 - 3)");
        afirmar(exTec.getCantidad() == 10, "Stock de teclado descontado exactamente a 10 (15 - 5)");

        // 7. Verificar que no se puede volver a completar ni cancelar una orden completada
        afirmarExcepcion(() -> sistema.completarOrden(orden.getId()),
                IllegalStateException.class, "No se puede volver a completar orden ya procesada");
        afirmarExcepcion(() -> sistema.cancelarOrden(orden.getId()),
                IllegalStateException.class, "No se puede cancelar orden completada");
    }

    // =========================================================================
    // 5. CANCELACIÓN DE ÓRDENES (CON PRODUCTOS Y VACÍAS)
    // =========================================================================
    private static void testCancelacionOrdenes() throws Exception {
        limpiarCarpetaDatos();
        SistemaVentas sistema = new SistemaVentas();

        Empleado emp = new Empleado("E-CAN", "Esteban", "5555555555555", "Dir", "Cajero", 4500.0);
        sistema.registrarEmpleado(emp);
        Bodega bodega = new Bodega("B-CAN", "E-CAN", 500, "Zona 1");
        sistema.registrarBodega(bodega);
        Producto prod = new Producto("PROD-CAN", "Cámara Web", "Full HD", "Logitech", 450.0);
        sistema.registrarProducto(prod);
        Cliente cli = new Cliente("6666666666666", "Elena Ríos", "Zona 14", LocalDate.of(1992, 4, 10), "Regular");
        sistema.registrarCliente(cli);

        sistema.agregarStock("PROD-CAN", "B-CAN", 20);

        // CASO A: Cancelar orden con productos -> Verificar que el stock queda 100% INTACTO
        OrdenCompra ordenConProductos = sistema.crearOrden("6666666666666");
        sistema.agregarProductoOrden(ordenConProductos.getId(), "PROD-CAN", "B-CAN", 8);
        afirmar(ordenConProductos.getTotal() == (8 * 450.0), "Total de orden calculado");

        sistema.cancelarOrden(ordenConProductos.getId());
        afirmar(ordenConProductos.getEstado().equals("CANCELADA"), "Orden con productos queda en CANCELADA");

        // Stock debe mantenerse exactamente en 20
        Existencia ex = sistema.buscarExistencia("PROD-CAN", "B-CAN");
        afirmar(ex.getCantidad() == 20, "Stock de bodega se mantiene INTACTO en 20 tras cancelación");

        // CASO B: Cancelar orden VACÍA (sin productos) -> NO debe lanzar excepción (corrige bug histórico)
        OrdenCompra ordenVacia = sistema.crearOrden("6666666666666");
        afirmar(ordenVacia.getProductos().isEmpty(), "Orden creada sin productos");

        // Cancelar orden vacía directamente
        sistema.cancelarOrden(ordenVacia.getId());
        afirmar(ordenVacia.getEstado().equals("CANCELADA"), "Orden vacía cancelada exitosamente sin excepción");

        // Intentar agregar productos a orden cancelada debe ser rechazado
        afirmarExcepcion(() -> sistema.agregarProductoOrden(ordenVacia.getId(), "PROD-CAN", "B-CAN", 1),
                IllegalStateException.class, "Rechazo de agregar producto a orden cancelada");
    }

    // =========================================================================
    // 6. ELIMINACIÓN SEGURA DE ÓRDENES VACÍAS
    // =========================================================================
    private static void testEliminarOrdenVacia() throws Exception {
        limpiarCarpetaDatos();
        SistemaVentas sistema = new SistemaVentas();

        Empleado emp = new Empleado("E-ELIM", "David", "7777777777777", "Dir", "Operador", 3800.0);
        sistema.registrarEmpleado(emp);
        Cliente cli = new Cliente("8888888888888", "Silvia Paz", "Zona 11", LocalDate.of(1998, 9, 30), "Regular");
        sistema.registrarCliente(cli);

        OrdenCompra ordenVacia = sistema.crearOrden("8888888888888");
        afirmar(sistema.getOrdenes().size() == 1, "Hay 1 orden registrada");

        boolean eliminada = sistema.eliminarOrdenSiVacia(ordenVacia.getId());
        afirmar(eliminada, "Método eliminarOrdenSiVacia devuelve true para orden vacía");
        afirmar(sistema.getOrdenes().isEmpty(), "La orden vacía fue eliminada del sistema sin dejar residuos");

        // Intentar eliminar orden inexistente devuelve false
        boolean noExiste = sistema.eliminarOrdenSiVacia("ORD-INEXISTENTE");
        afirmar(!noExiste, "Eliminar orden inexistente devuelve false");
    }

    // =========================================================================
    // 7. PERSISTENCIA ATÓMICA, RESPALDO Y RECUPERACIÓN
    // =========================================================================
    private static void testPersistenciaYRecuperacion() throws Exception {
        limpiarCarpetaDatos();

        // 1. Instanciar sistema, poblar con datos y guardar
        SistemaVentas sistema1 = new SistemaVentas();

        Empleado emp = new Empleado("EMP-PERS", "Fernando", "9999999999999", "Zona 7", "Supervisor", 8200.0);
        sistema1.registrarEmpleado(emp);

        Bodega bodega = new Bodega("BOD-PERS", "EMP-PERS", 800, "Parque Industrial Norte");
        sistema1.registrarBodega(bodega);

        Producto prod = new Producto("LAP-PRO", "Laptop Core i9", "32GB RAM SSD 1TB", "Lenovo", 14500.0);
        sistema1.registrarProducto(prod);

        Cliente cli = new Cliente("1010101010101", "Lucía Castillo", "Zona 16", LocalDate.of(1985, 3, 22), "VIP");
        sistema1.registrarCliente(cli);

        sistema1.agregarStock("LAP-PRO", "BOD-PERS", 50);

        OrdenCompra orden = sistema1.crearOrden("1010101010101");
        sistema1.agregarProductoOrden(orden.getId(), "LAP-PRO", "BOD-PERS", 2);
        sistema1.completarOrden(orden.getId());

        // Verificar existencia física en disco
        Path pathPrincipal = CARPETA_DATOS.resolve("sistema.dat");
        afirmar(Files.exists(pathPrincipal), "El archivo principal 'sistema.dat' existe físicamente en disco");
        afirmar(Files.size(pathPrincipal) > 0, "El archivo 'sistema.dat' tiene contenido mayor a 0 bytes");

        // 2. Crear una nueva instancia de SistemaVentas y verificar que todo se recarga 100%
        SistemaVentas sistemaRecargado = new SistemaVentas();
        afirmar(sistemaRecargado.getEmpleados().size() == 1, "Empleado persistido y recargado");
        afirmar(sistemaRecargado.getBodegas().size() == 1, "Bodega persistida y recargada");
        afirmar(sistemaRecargado.getProductos().size() == 1, "Producto persistido y recargado");
        afirmar(sistemaRecargado.getClientes().size() == 1, "Cliente persistido y recargado");
        afirmar(sistemaRecargado.getOrdenes().size() == 1, "Orden persistida y recargada");

        // Verificar datos específicos
        Producto prodRec = sistemaRecargado.buscarProducto("LAP-PRO");
        afirmar(prodRec != null && prodRec.getPrecio() == 14500.0, "Precio del producto recargado intacto");

        Existencia exRec = sistemaRecargado.buscarExistencia("LAP-PRO", "BOD-PERS");
        afirmar(exRec != null && exRec.getCantidad() == 48, "Stock tras venta recargado con exactitud (50 - 2 = 48)");

        OrdenCompra ordRec = sistemaRecargado.buscarOrden(orden.getId());
        afirmar(ordRec != null && ordRec.getEstado().equals("COMPLETADA"), "Estado de orden recargado como COMPLETADA");

        // 3. Probar recuperación automática desde archivo de respaldo (sistema.bak)
        Path pathBak = CARPETA_DATOS.resolve("sistema.bak");
        afirmar(Files.exists(pathBak), "El archivo de respaldo 'sistema.bak' existe físicamente");

        // Corromper el archivo principal intencionalmente con bytes basura
        Files.write(pathPrincipal, new byte[]{0x00, 0x12, 0x34, 0x56, 0x78});

        // Al crear un nuevo sistema, ArchivoService detecta el daño y recupera desde sistema.bak
        SistemaVentas sistemaRecuperadoDeBackup = new SistemaVentas();
        afirmar(sistemaRecuperadoDeBackup.getProductos().size() == 1, "Recuperación automática exitosa desde 'sistema.bak'");
        afirmar(sistemaRecuperadoDeBackup.buscarProducto("LAP-PRO") != null, "Catálogo íntegro tras recuperación de respaldo");
    }

    // =========================================================================
    // UTILIDADES DE LA SUITE DE PRUEBAS
    // =========================================================================
    private static void ejecutarPrueba(String nombre, AccionPrueba accion) {
        System.out.printf("[PRUEBA] %-60s ", nombre);
        try {
            accion.ejecutar();
            pruebasExitosas++;
            System.out.println("[\u001B[32mPASÓ\u001B[0m]");
        } catch (Throwable ex) {
            pruebasFallidas++;
            System.out.println("[\u001B[31mFALLÓ\u001B[0m]");
            detallesFallos.add(nombre + ": " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    private static void afirmar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError("Fallo de aserción: " + mensaje);
        }
    }

    private static void afirmarExcepcion(AccionPrueba accion, Class<? extends Throwable> tipoEsperado, String mensaje) {
        try {
            accion.ejecutar();
            throw new AssertionError("Se esperaba excepción " + tipoEsperado.getSimpleName() + " pero no ocurrió: " + mensaje);
        } catch (Throwable ex) {
            if (!tipoEsperado.isInstance(ex)) {
                throw new AssertionError("Se esperaba " + tipoEsperado.getSimpleName() + " pero ocurrió "
                        + ex.getClass().getSimpleName() + ": " + ex.getMessage() + " (" + mensaje + ")");
            }
        }
    }

    private static void prepararEntornoAislado() {
        try {
            if (Files.exists(CARPETA_DATOS)) {
                habiaDatosPrevios = true;
                if (Files.exists(CARPETA_BACKUP_TEST)) {
                    eliminarDirectorioRecursivo(CARPETA_BACKUP_TEST);
                }
                copiarDirectorioRecursivo(CARPETA_DATOS, CARPETA_BACKUP_TEST);
            }
        } catch (IOException e) {
            System.err.println("[AVISO] No se pudo crear copia de respaldo temporal de datos: " + e.getMessage());
        }
    }

    private static void restaurarEntornoOriginal() {
        try {
            eliminarDirectorioRecursivo(CARPETA_DATOS);
            if (habiaDatosPrevios && Files.exists(CARPETA_BACKUP_TEST)) {
                copiarDirectorioRecursivo(CARPETA_BACKUP_TEST, CARPETA_DATOS);
                eliminarDirectorioRecursivo(CARPETA_BACKUP_TEST);
            }
        } catch (IOException e) {
            System.err.println("[AVISO] Error al restaurar carpeta original de datos: " + e.getMessage());
        }
    }

    private static void limpiarCarpetaDatos() throws IOException {
        if (Files.exists(CARPETA_DATOS)) {
            eliminarDirectorioRecursivo(CARPETA_DATOS);
        }
        Files.createDirectories(CARPETA_DATOS);
    }

    private static void eliminarDirectorioRecursivo(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {}
                });
    }

    private static void copiarDirectorioRecursivo(Path origen, Path destino) throws IOException {
        Files.walk(origen).forEach(fuente -> {
            try {
                Path dest = destino.resolve(origen.relativize(fuente));
                if (Files.isDirectory(fuente)) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(fuente, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    @FunctionalInterface
    private interface AccionPrueba {
        void ejecutar() throws Throwable;
    }
}
