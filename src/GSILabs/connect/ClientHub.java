package GSILabs.connect;

import GSILabs.BModel.Bar;
import GSILabs.BModel.Cliente;
import GSILabs.BModel.Direccion;
import GSILabs.BModel.Local;
import GSILabs.BModel.Restaurante;
import GSILabs.BModel.Review;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Cliente ejecutable para el perfil de cliente final que se conecta por RMI al
 * objeto remoto publicado bajo {@link ClientGateway} (y {@link LocalFinder}).
 *
 * <p>Solicita por consola dirección del servidor, puerto y <em>tag</em> remoto,
 * ejecuta una batería predefinida de comprobaciones sobre todos los métodos de
 * la interfaz y gestiona caídas o indisponibilidad del servidor permaneciendo a la
 * espera y renovando automáticamente el <em>stub</em> al reconectar.
 *
 * @author Joel Vílchez, Urko Inés, Aimar Ijurko, Iker Fernández, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class ClientHub {

    private static final int DEFAULT_PORT = 1099;
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_TAG = "ClientGateway";
    private static final long RETRY_DELAY_MS = 3000L;

    private ClientGateway gateway;
    private int numeroEjecucion = 0;

    /**
     * Constructor por defecto.
     */
    public ClientHub() {
    }

    @FunctionalInterface
    private interface OperacionRemota<T> {
        T ejecutar(ClientGateway stub) throws RemoteException;
    }

    /**
     * Punto de entrada del cliente {@code ClientHub}.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        new ClientHub().ejecutar();
    }

    private void ejecutar() {
        System.out.println("=== CLIENTE RMI: ClientHub ===");
        Scanner scanner = new Scanner(System.in);

        String host = pedirTextoConDefecto(scanner, "Dirección del servidor [localhost]: ", DEFAULT_HOST);
        int puerto = pedirPuerto(scanner, "Puerto del servidor [1099]: ", DEFAULT_PORT);
        String tag = pedirTextoConDefecto(scanner, "Tag del objeto remoto [ClientGateway]: ", DEFAULT_TAG);

        this.gateway = conectarConEspera(host, puerto, tag);

        boolean continuar = true;
        while (continuar) {
            ejecutarBateriaPruebas(host, puerto, tag);
            continuar = preguntarSiRepetir(scanner);
        }

        System.out.println("ClientHub finalizado.");
    }

    private static String pedirTextoConDefecto(Scanner scanner, String mensaje, String defecto) {
        System.out.print(mensaje);
        if (!scanner.hasNextLine()) {
            return defecto;
        }
        String entrada = scanner.nextLine().trim();
        return entrada.isEmpty() ? defecto : entrada;
    }

    private static int pedirPuerto(Scanner scanner, String mensaje, int defecto) {
        while (true) {
            System.out.print(mensaje);
            if (!scanner.hasNextLine()) {
                return defecto;
            }
            String entrada = scanner.nextLine().trim();
            if (entrada.isEmpty()) {
                return defecto;
            }
            try {
                int puerto = Integer.parseInt(entrada);
                if (puerto >= 1 && puerto <= 65535) {
                    return puerto;
                }
                System.out.println("El puerto debe estar entre 1 y 65535.");
            } catch (NumberFormatException e) {
                System.out.println("Puerto no válido. Introduce un número entero o pulsa Enter para " + defecto + ".");
            }
        }
    }

    private static boolean preguntarSiRepetir(Scanner scanner) {
        System.out.print("\nPulsa ENTER para volver a ejecutar las comprobaciones o escribe 'salir' para terminar: ");
        if (!scanner.hasNextLine()) {
            return false;
        }
        String respuesta = scanner.nextLine().trim();
        return !"salir".equalsIgnoreCase(respuesta);
    }

    private ClientGateway conectarConEspera(String host, int puerto, String tag) {
        while (true) {
            try {
                Registry registry = LocateRegistry.getRegistry(host, puerto);
                Object obj = registry.lookup(tag);
                if (!(obj instanceof ClientGateway)) {
                    throw new ClassCastException("El objeto asociado a '" + tag + "' no implementa ClientGateway.");
                }
                System.out.println("[CONECTADO] Enlace establecido con '" + tag + "' en " + host + ":" + puerto);
                return (ClientGateway) obj;
            } catch (RemoteException | NotBoundException | ClassCastException e) {
                System.out.println("[AVISO] Servidor '" + tag + "' no disponible en "
                        + host + ":" + puerto + " (" + e.getMessage() + "). Reintentando en 3 s...");
                esperarReintento();
            }
        }
    }

    private <T> T invocarConReconexion(String host, int puerto, String tag, OperacionRemota<T> op) {
        while (true) {
            if (gateway == null) {
                gateway = conectarConEspera(host, puerto, tag);
            }
            try {
                return op.ejecutar(gateway);
            } catch (RemoteException e) {
                System.out.println("[AVISO] Se perdió la conexión con el servidor RMI ("
                        + e.getMessage() + "). Permaneciendo a la espera para reconectar...");
                gateway = null;
                esperarReintento();
            }
        }
    }

    private static void esperarReintento() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void ejecutarBateriaPruebas(String host, int puerto, String tag) {
        numeroEjecucion++;
        System.out.println("\n--- Ejecución de funcionalidades de ClientGateway (#" + numeroEjecucion + ") ---");

        // 1. LocalFinder.getLocals
        Local[] encontrados = invocarConReconexion(host, puerto, tag, g -> g.getLocals("bar"));
        if (encontrados.length == 0) {
            encontrados = invocarConReconexion(host, puerto, tag, g -> g.getLocals("a"));
        }
        System.out.println("1) getLocals(\"bar\") -> " + encontrados.length + " local(es): "
                + Arrays.toString(encontrados));

        Local[] sinResultados = invocarConReconexion(host, puerto, tag, g -> g.getLocals("LocalInexistenteXYZ"));
        System.out.println("   getLocals(\"LocalInexistenteXYZ\") -> longitud " + sinResultados.length);

        // 2. LocalFinder.getLocal
        Local localExacto = invocarConReconexion(host, puerto, tag, g -> g.getLocal("  bar   txoko  "));
        System.out.println("2) getLocal(\"  bar   txoko  \") -> "
                + (localExacto != null ? localExacto.getNombre() : "null"));

        Local localInexistente = invocarConReconexion(host, puerto, tag, g -> g.getLocal("NoExisteXYZ"));
        System.out.println("   getLocal(\"NoExisteXYZ\") -> " + localInexistente);

        // 3. ClientGateway.mejorBar
        Bar mejorBarPamplona = invocarConReconexion(host, puerto, tag, g -> g.mejorBar("Pamplona"));
        System.out.println("3) mejorBar(\"Pamplona\") -> "
                + (mejorBarPamplona != null ? mejorBarPamplona.getNombre() : "null"));

        Bar mejorBarVacio = invocarConReconexion(host, puerto, tag, g -> g.mejorBar("CiudadSinBaresXYZ"));
        System.out.println("   mejorBar(\"CiudadSinBaresXYZ\") -> " + mejorBarVacio);

        // 4. ClientGateway.mejoresRestaurantes
        Restaurante[] top5 = invocarConReconexion(host, puerto, tag, g -> g.mejoresRestaurantes("Pamplona", 5));
        System.out.println("4) mejoresRestaurantes(\"Pamplona\", 5) -> " + formatearRestaurantes(top5));

        Restaurante[] top2 = invocarConReconexion(host, puerto, tag, g -> g.mejoresRestaurantes("Pamplona", 2));
        System.out.println("   mejoresRestaurantes(\"Pamplona\", 2) -> " + formatearRestaurantes(top2));

        // 5 y 6. ClientGateway.insertaReview y quitaReview
        Local localObjetivo = localExacto != null
                ? localExacto
                : (encontrados.length > 0 ? encontrados[0] : null);

        if (localObjetivo != null) {
            Cliente ana = new Cliente("ana", "claveLocal", LocalDate.of(1995, 3, 15));
            LocalDate fechaVisita = LocalDate.of(2026, 11, 1).plusDays(numeroEjecucion - 1L);
            Review reviewNueva = new Review(
                    ana, localObjetivo, 5,
                    "Prueba de insertaReview vía RMI (#" + numeroEjecucion + ")",
                    fechaVisita, LocalDate.now());

            boolean insertada = invocarConReconexion(host, puerto, tag, g -> g.insertaReview(reviewNueva));
            boolean duplicada = invocarConReconexion(host, puerto, tag, g -> g.insertaReview(reviewNueva));
            System.out.println("5) insertaReview(ana, " + localObjetivo.getNombre() + ", " + fechaVisita + ") -> "
                    + insertada + " | reintento duplicado -> " + duplicada);

            Cliente inexistente = new Cliente("no_existe_999", "pw", LocalDate.of(1995, 1, 1));
            Review reviewInvalida = new Review(inexistente, localObjetivo, 4, "Inválida", fechaVisita, LocalDate.now());
            boolean rechazada = invocarConReconexion(host, puerto, tag, g -> g.insertaReview(reviewInvalida));
            System.out.println("   insertaReview(cliente inexistente) -> " + rechazada);

            boolean quitada = invocarConReconexion(host, puerto, tag, g -> g.quitaReview(reviewNueva));
            boolean quitadaOtraVez = invocarConReconexion(host, puerto, tag, g -> g.quitaReview(reviewNueva));
            System.out.println("6) quitaReview(nueva) -> " + quitada + " | segundo intento -> " + quitadaOtraVez);

            Bar gaucho = new Bar("Bar Gaucho", new Direccion("Pamplona", "Navarra", "Calle Espoz y Mina", 7), "Famoso");
            Review conContestacion = new Review(
                    ana, gaucho, 5, "Con contestación",
                    LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 11));
            boolean quitadaConResp = invocarConReconexion(host, puerto, tag, g -> g.quitaReview(conContestacion));
            System.out.println("   quitaReview(review con contestación en Bar Gaucho) -> " + quitadaConResp);
        } else {
            System.out.println("5-6) No se encontraron locales en el servidor para probar insertaReview/quitaReview.");
        }
    }

    private static String formatearRestaurantes(Restaurante[] lista) {
        if (lista == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(lista[i] != null ? lista[i].getNombre() : "null");
        }
        return sb.append("]").toString();
    }
}
