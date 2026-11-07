package sistemaventas;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class ArchivoService {
    private static final int MAX_REINTENTOS = 5;
    private static final long PAUSA_BASE_MS = 50L;

    private final Path carpeta;
    private final Path archivo;
    private final Path temporal;
    private final Path respaldo;

    public ArchivoService() {
        this.carpeta = Paths.get("datos");
        this.archivo = carpeta.resolve("sistema.dat");
        this.temporal = carpeta.resolve("sistema.tmp");
        this.respaldo = carpeta.resolve("sistema.bak");
    }

    public EstadoSistema cargar() throws IOException {
        Files.createDirectories(carpeta);

        if (Files.exists(archivo)) {
            try {
                return leerObjeto(archivo);
            } catch (Exception ex) {
                if (Files.exists(respaldo)) {
                    try {
                        EstadoSistema recuperado = leerObjeto(respaldo);
                        guardar(recuperado);
                        return recuperado;
                    } catch (Exception ex2) {
                        throw new IOException("El archivo principal y el respaldo están dañados.", ex2);
                    }
                }
                throw new IOException("No se pudo leer el archivo de datos principal.", ex);
            }
        }

        if (Files.exists(respaldo)) {
            try {
                EstadoSistema recuperado = leerObjeto(respaldo);
                guardar(recuperado);
                return recuperado;
            } catch (Exception ex) {
                throw new IOException("El archivo de respaldo está dañado.", ex);
            }
        }

        return new EstadoSistema();
    }

    private EstadoSistema leerObjeto(Path ruta) throws IOException, ClassNotFoundException {
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(ruta))) {
            Object objeto = entrada.readObject();
            if (!(objeto instanceof EstadoSistema)) {
                throw new IOException("El archivo no contiene un formato de EstadoSistema válido.");
            }
            return (EstadoSistema) objeto;
        }
    }

    public void guardar(EstadoSistema estado) throws IOException {
        Files.createDirectories(carpeta);

        // 1. Escritura en archivo temporal
        try (ObjectOutputStream salida = new ObjectOutputStream(Files.newOutputStream(
                temporal, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            salida.writeObject(estado);
            salida.flush();
        }

        // 2. Verificación rigurosa de integridad del archivo temporal
        try {
            leerObjeto(temporal);
        } catch (ClassNotFoundException | IOException ex) {
            Files.deleteIfExists(temporal);
            throw new IOException("Fallo en la verificación de integridad del archivo temporal generado.", ex);
        }

        // 3. Crear copia de seguridad automática del archivo actual si existe
        if (Files.exists(archivo)) {
            copiarConReintentos(archivo, respaldo);
        }

        // 4. Reemplazo atómico con soporte de reintentos para bloqueos en Windows
        moverConReintentos(temporal, archivo);
    }

    private void copiarConReintentos(Path origen, Path destino) throws IOException {
        IOException ultimoError = null;
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (IOException ex) {
                ultimoError = ex;
                if (intento < MAX_REINTENTOS) {
                    dormir(intento);
                }
            }
        }
        throw new IOException("No se pudo copiar el archivo de respaldo tras múltiples intentos.", ultimoError);
    }

    private void moverConReintentos(Path origen, Path destino) throws IOException {
        IOException ultimoError = null;
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                try {
                    Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    return;
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            } catch (IOException ex) {
                ultimoError = ex;
                if (intento < MAX_REINTENTOS) {
                    dormir(intento);
                }
            }
        }
        throw new IOException("No se pudo completar el movimiento atómico del archivo tras múltiples intentos.", ultimoError);
    }

    private void dormir(int intento) throws IOException {
        try {
            Thread.sleep(PAUSA_BASE_MS * intento);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Operación de archivo interrumpida.", ie);
        }
    }
}
