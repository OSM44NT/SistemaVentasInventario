# Sistema de Ventas e Inventario

Proyecto de aplicacion de escritorio desarrollado en Java aplicando Programacion Orientada a Objetos (POO), interfaz grafica con Java Swing, persistencia de datos en archivos y control de versiones con Git.

---

## Indice
1. Descripcion del Proyecto
2. Alcance del Sistema y Modelos de Datos
3. Estructura del Proyecto
4. Requisitos del Sistema
5. Compilacion y Ejecucion en Windows
6. Ejecucion en Linux y macOS
7. Apertura en Entornos de Desarrollo (IDEs)
8. Pruebas del Sistema
9. Documentacion Incluida

---

## Descripcion del Proyecto

El sistema fue desarrollado para gestionar de forma centralizada las operaciones comerciales de una empresa que administra productos, clientes, empleados, bodegas, inventario y ordenes de compra.

Caracteristicas principales:
- Catalogo de productos con precios unitarios en Quetzales (Q).
- Registro de clientes clasificados por categoria (Regular o VIP) con validacion de DPI de 13 digitos.
- Registro de empleados con asignacion a bodegas fisicas como responsables.
- Administracion de bodegas con control de capacidad maxima volumetrica.
- Control de inventario multideposito con validacion para no superar la capacidad de almacenamiento.
- Gestion de ordenes de compra con venta de productos desde bodegas especificas y actualizacion automatica de existencias al completar la venta.
- Persistencia de datos en disco mediante serializacion de objetos binarios en la carpeta datos/, con generacion automatica de respaldos (.bak) para prevenir perdidas de informacion.

---

## Alcance del Sistema y Modelos de Datos

- **Producto:** Codigo, nombre, descripcion, marca y precio unitario.
- **Cliente:** DPI (13 digitos), nombre completo, direccion, fecha de nacimiento y tipo de cliente (Regular o VIP).
- **Empleado:** ID de empleado, nombre, DPI (13 digitos), direccion, puesto y salario en Quetzales.
- **Bodega:** ID de bodega, empleado responsable asignado, capacidad total en unidades y direccion fisica.
- **Inventario (Existencias):** Codigo de producto, bodega de almacenamiento y cantidad en stock.
- **Orden de Compra:** ID de orden, DPI del cliente, lista de productos con cantidades y bodegas de despacho, fecha, total acumulado y estado (Pendiente, Completada, Cancelada). Al completarse una orden, el sistema descuenta de forma automatica e inmediata el stock de las bodegas correspondientes.

---

## Estructura del Proyecto

```text
SistemaVentasInventario/
├── .gitattributes          # Normalizacion de saltos de linea
├── .gitignore              # Archivos y carpetas excluidos del control de versiones
├── README.md               # Informacion general del proyecto
├── build.bat               # Compilacion para Windows
├── run.bat                 # Ejecucion para Windows
├── test.bat                # Pruebas automatizadas para Windows
├── build.sh                # Compilacion para Linux y macOS
├── run.sh                  # Ejecucion para Linux y macOS
├── test.sh                 # Pruebas para Linux y macOS
├── docs/                   # Documentacion del proyecto
│   ├── MANUAL_TECNICO.md   # Manual tecnico de arquitectura y diagrama de clases UML
│   └── MANUAL_USUARIO.md   # Manual de usuario y guia paso a paso de uso
├── src/                    # Codigo fuente Java
│   └── sistemaventas/
│       ├── ArchivoService.java     # Persistencia y lectura de archivos en disco
│       ├── Bodega.java             # Clase de bodega y control de capacidad
│       ├── Cliente.java            # Clase de cliente y validacion de datos
│       ├── Empleado.java           # Clase de empleado y salario
│       ├── EstadoSistema.java      # Contenedor del estado de la aplicacion
│       ├── EstiloUI.java           # Configuracion visual de la interfaz grafica
│       ├── Existencia.java         # Control de existencias por producto y bodega
│       ├── Main.java               # Clase principal de inicio
│       ├── OrdenCompra.java        # Gestion del ciclo de vida de las ordenes
│       ├── Producto.java           # Clase de producto y validacion de precios
│       ├── SistemaVentas.java      # Fachada central y logica de negocio
│       ├── Validaciones.java       # Validaciones generales del sistema
│       └── VentanaPrincipal.java   # Interfaz grafica de usuario
└── test/
    └── PruebasSistema.java         # Bateria de pruebas unitarias y de integracion
```

---

## Requisitos del Sistema

- Java Development Kit (JDK) version 17 o 21 (OpenJDK o Oracle JDK).
- Sistema operativo Windows 10/11, macOS o Linux.
- Terminal o consola con soporte para comandos de Java (javac y java).

---

## Compilacion y Ejecucion en Windows

### Opcion 1: Ejecucion directa
Hacer doble clic en el archivo `run.bat`. El script se encargara de verificar el entorno, compilar los archivos si es la primera vez e iniciar la interfaz grafica.

### Opcion 2: Desde la consola (CMD)
Abrir una ventana de comandos en la carpeta del proyecto y ejecutar:
```cmd
build.bat
run.bat
```

---

## Pruebas del Sistema

El proyecto cuenta con una clase de pruebas automatizadas que valida:
1. Validaciones y reglas de negocio de los modelos.
2. Registro y unicidad de identificadores (codigos, DPIs, IDs).
3. Control de capacidad maxima de bodegas y verificacion de stock.
4. Descuento exacto de inventario al completar ordenes de compra.
5. Cancelacion de ordenes sin alterar el stock.
6. Eliminacion de borradores de ordenes vacias.
7. Persistencia de datos en disco y recuperacion de respaldo.

---

## Documentacion Incluida

- **Manual Tecnico (`docs/MANUAL_TECNICO.md`):** Contiene la descripcion de la arquitectura, patrones utilizados, diagrama de clases UML y detalles de la persistencia de datos.
- **Manual de Usuario (`docs/MANUAL_USUARIO.md`):** Explica detalladamente como utilizar cada uno de los modulos de la aplicacion con datos de ejemplo y solucion de dudas comunes.
