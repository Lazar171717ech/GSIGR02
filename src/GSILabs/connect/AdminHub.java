package GSILabs.connect;

import GSILabs.BModel.Cliente;
import GSILabs.BModel.Direccion;
import GSILabs.BModel.Local;
import GSILabs.BModel.Pub;
import GSILabs.BModel.Review;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Cliente ejecutable para el perfil de administración que se conecta por RMI al
 * objeto remoto publicado bajo {@link AdminGateway} (y {@link LocalFinder}).
 *
 * <p>Solicita por consola dirección del servidor, puerto y <em>tag</em> remoto,
 * ejecuta una batería predefinida de comprobaciones sobre todos los métodos de
 * administración y gestiona caídas o indisponibilidad del servidor permaneciendo a
 * la espera y renovando automáticamente el <em>stub</em> al reconectar.
 *
 * @author Joel Vílchez, Urko Inés, Aimar Ijurko, Iker Fernández, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class AdminHub {

    private static final int DEFAULT_PORT = 1099;
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_TAG = "AdminGateway";
    private static final long RETRY_DELAY_MS = 3000L;

    private AdminGateway gateway;
    private int numeroEjecucion = 0;

    /**
     * Constructor por defecto.
     */
    public AdminHub() {
    }

    @FunctionalInterface
    private interface OperacionRemota<T> {
        T ejecutar(AdminGateway stub) throws RemoteException;
    }

    /**
     * Punto de entrada del cliente {@code AdminHub}.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        new AdminHub().ejecutar();
    }

    private void ejecutar() {
        System.out.println("=== CLIENTE RMI: AdminHub ===");
        Scanner scanner = new Scanner(System.in);

        String host = pedirTextoConDefecto(scanner, "Dirección del servidor [localhost]: ", DEFAULT_HOST);
        int puerto = pedirPuerto(scanner, "Puerto del servidor [1099]: ", DEFAULT_PORT);
        String tag = pedirTextoConDefecto(scanner, "Tag del objeto remoto [AdminGateway]: ", DEFAULT_TAG);

        this.gateway = conectarConEspera(host, puerto, tag);

        boolean continuar = true;
        while (continuar) {
            ejecutarBateriaPruebas(host, puerto, tag);
            continuar = preguntarSiRepetir(scanner);
        }

        System.out.println("AdminHub finalizado.");
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

    private AdminGateway conectarConEspera(String host, int puerto, String tag) {
        while (true) {
            try {
                Registry registry = LocateRegistry.getRegistry(host, puerto);
                Object obj = registry.lookup(tag);
                if (!(obj instanceof AdminGateway)) {
                    throw new ClassCastException("El objeto asociado a '" + tag + "' no implementa AdminGateway.");
                }
                System.out.println("[CONECTADO] Enlace establecido con '" + tag + "' en " + host + ":" + puerto);
                return (AdminGateway) obj;
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
        System.out.println("\n--- Ejecución de funcionalidades de AdminGateway (#" + numeroEjecucion + ") ---");

        // 1. LocalFinder.getLocals y getLocal
        Local[] encontrados = invocarConReconexion(host, puerto, tag, g -> g.getLocals("bar"));
        System.out.println("1) getLocals(\"bar\") -> " + encontrados.length + " local(es): "
                + Arrays.toString(encontrados));

        Local barTxoko = invocarConReconexion(host, puerto, tag, g -> g.getLocal("  bar   txoko  "));
        Local barFitero = invocarConReconexion(host, puerto, tag, g -> g.getLocal("  bar   fitero  "));
        Local inexistente = invocarConReconexion(host, puerto, tag, g -> g.getLocal("LocalInexistenteXYZ"));
        System.out.println("   getLocal(\"  bar   fitero  \") -> "
                + (barFitero != null ? barFitero.getNombre() : "null")
                + " | getLocal(\"LocalInexistenteXYZ\") -> " + inexistente);

        // 2. AdminGateway.insertaReviewFalsa
        Local objetivoFalsa = barTxoko != null ? barTxoko : (encontrados.length > 0 ? encontrados[0] : null);
        if (objetivoFalsa != null) {
            Boolean falsaOk = invocarConReconexion(host, puerto, tag, g -> g.insertaReviewFalsa(objetivoFalsa, 5));
            Boolean falsaInvalida = invocarConReconexion(host, puerto, tag, g -> g.insertaReviewFalsa(objetivoFalsa, 99));
            System.out.println("2) insertaReviewFalsa(" + objetivoFalsa.getNombre() + ", 5) -> " + falsaOk
                    + " | puntuación 99 -> " + falsaInvalida);
        } else {
            System.out.println("2) Sin locales disponibles para insertaReviewFalsa.");
        }

        // 3. AdminGateway.eliminaReview (con contestación)
        Cliente carlos = new Cliente("carlos", "pw", LocalDate.of(1990, 7, 22));
        Pub pubSubsuelo = new Pub(
                "Pub Subsuelo",
                new Direccion("Pamplona", "Navarra", "Plaza del Castillo", 44),
                "Música en directo",
                LocalTime.of(22, 0),
                LocalTime.of(4, 30));
        Review revConContestacion = new Review(
                carlos, pubSubsuelo, 4, "Review de Pub Subsuelo",
                LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 23));
        Boolean elimContestada = invocarConReconexion(host, puerto, tag, g -> g.eliminaReview(revConContestacion));
        Boolean elimNula = invocarConReconexion(host, puerto, tag, g -> g.eliminaReview(null));
        System.out.println("3) eliminaReview(Pub Subsuelo, carlos) -> " + elimContestada
                + " | eliminaReview(null) -> " + elimNula);

        // 4. AdminGateway.eliminaReviewsDeUsuario
        Cliente usuarioNoExiste = new Cliente("fantasma_xyz", "pw", LocalDate.of(1992, 1, 1));
        Cliente diego = new Cliente("diego", "pw", LocalDate.of(1988, 1, 30));
        Cliente marta = new Cliente("marta", "pw", LocalDate.of(2000, 11, 5));

        Integer elimNoExiste = invocarConReconexion(host, puerto, tag, g -> g.eliminaReviewsDeUsuario(usuarioNoExiste));
        Integer elimDiego = invocarConReconexion(host, puerto, tag, g -> g.eliminaReviewsDeUsuario(diego));
        Integer elimMarta = invocarConReconexion(host, puerto, tag, g -> g.eliminaReviewsDeUsuario(marta));
        System.out.println("4) eliminaReviewsDeUsuario -> inexistente=" + elimNoExiste
                + ", diego=" + elimDiego + ", marta=" + elimMarta);

        // 5. AdminGateway.eliminaReviewsDeLocal
        Boolean elimRevsLocal = invocarConReconexion(host, puerto, tag, g -> g.eliminaReviewsDeLocal(barFitero));
        Boolean elimRevsNull = invocarConReconexion(host, puerto, tag, g -> g.eliminaReviewsDeLocal(null));
        System.out.println("5) eliminaReviewsDeLocal(Bar Fitero) -> " + elimRevsLocal
                + " | null -> " + elimRevsNull);

        // 6. AdminGateway.eliminaLocal
        Boolean elimLocal = invocarConReconexion(host, puerto, tag, g -> g.eliminaLocal(barFitero));
        Boolean elimLocalNull = invocarConReconexion(host, puerto, tag, g -> g.eliminaLocal(null));
        System.out.println("6) eliminaLocal(Bar Fitero) -> " + elimLocal
                + " | null -> " + elimLocalNull);
    }
}
