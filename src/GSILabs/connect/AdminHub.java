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
    private String tagActual;
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
     * Permite recibir los parámetros tanto por argumentos de línea de comandos
     * (args[0]=host, args[1]=puerto, args[2]=tag) como de forma interactiva por consola.
     *
     * @param args Argumentos opcionales de línea de comandos.
     */
    public static void main(String[] args) {
        new AdminHub().ejecutar(args);
    }

    private void ejecutar(String[] args) {
        System.out.println("=== CLIENTE RMI: AdminHub ===");
        Scanner scanner = new Scanner(System.in);

        String host = null;
        int puerto = -1;
        String tag = null;

        if (args != null && args.length >= 1 && !args[0].trim().isEmpty()) {
            host = args[0].trim();
        }
        if (args != null && args.length >= 2 && !args[1].trim().isEmpty()) {
            try {
                int p = Integer.parseInt(args[1].trim());
                if (p >= 1 && p <= 65535) {
                    puerto = p;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (args != null && args.length >= 3 && !args[2].trim().isEmpty()) {
            tag = args[2].trim();
        }

        if (host == null) {
            host = pedirTextoConDefecto(scanner, "Dirección del servidor [localhost]: ", DEFAULT_HOST);
        } else {
            System.out.println("Dirección del servidor : " + host + " (por argumento)");
        }

        if (puerto == -1) {
            puerto = pedirPuerto(scanner, "Puerto del servidor [1099]: ", DEFAULT_PORT);
        } else {
            System.out.println("Puerto del servidor    : " + puerto + " (por argumento)");
        }

        if (tag == null) {
            tag = pedirTextoConDefecto(scanner, "Tag del objeto remoto [AdminGateway]: ", DEFAULT_TAG);
        } else {
            System.out.println("Tag del objeto remoto  : " + tag + " (por argumento)");
        }

        this.tagActual = tag;
        this.gateway = conectarConEspera(scanner, host, puerto);

        boolean continuar = true;
        while (continuar) {
            ejecutarBateriaPruebas(scanner, host, puerto);
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

    private AdminGateway conectarConEspera(Scanner scanner, String host, int puerto) {
        while (true) {
            try {
                Registry registry = LocateRegistry.getRegistry(host, puerto);
                Object obj = registry.lookup(tagActual);
                if (!(obj instanceof AdminGateway)) {
                    System.out.println("[ERROR] El objeto asociado al tag '" + tagActual
                            + "' no implementa AdminGateway (es de tipo "
                            + (obj != null ? obj.getClass().getName() : "null") + ").");
                    tagActual = pedirTextoConDefecto(scanner, "Introduce un tag válido [AdminGateway]: ", DEFAULT_TAG);
                    continue;
                }
                System.out.println("[CONECTADO] Enlace establecido con '" + tagActual + "' en " + host + ":" + puerto);
                return (AdminGateway) obj;
            } catch (NotBoundException e) {
                System.out.println("[ERROR] El tag '" + tagActual + "' no existe en el registro RMI de " + host + ":" + puerto + ".");
                tagActual = pedirTextoConDefecto(scanner, "Introduce un tag que esté publicado en el servidor [AdminGateway]: ", DEFAULT_TAG);
            } catch (RemoteException e) {
                System.out.println("[AVISO] Servidor RMI no disponible en "
                        + host + ":" + puerto + " (" + e.getMessage() + "). Esperando a que el servidor arranque...");
                esperarReintento();
            }
        }
    }

    private <T> T invocarConReconexion(Scanner scanner, String host, int puerto, OperacionRemota<T> op) {
        while (true) {
            if (gateway == null) {
                gateway = conectarConEspera(scanner, host, puerto);
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

    private void ejecutarBateriaPruebas(Scanner scanner, String host, int puerto) {
        numeroEjecucion++;
        System.out.println("\n--- Ejecución de funcionalidades de AdminGateway (#" + numeroEjecucion + ") ---");

        // 1. LocalFinder.getLocals y getLocal
        Local[] encontrados = invocarConReconexion(scanner, host, puerto, g -> g.getLocals("bar"));
        System.out.println("1) getLocals(\"bar\") -> " + encontrados.length + " local(es): "
                + Arrays.toString(encontrados));

        Local barTxoko = invocarConReconexion(scanner, host, puerto, g -> g.getLocal("  bar   txoko  "));
        Local barFitero = invocarConReconexion(scanner, host, puerto, g -> g.getLocal("  bar   fitero  "));
        Local inexistente = invocarConReconexion(scanner, host, puerto, g -> g.getLocal("LocalInexistenteXYZ"));
        System.out.println("   getLocal(\"  bar   fitero  \") -> "
                + (barFitero != null ? barFitero.getNombre() : "null")
                + " | getLocal(\"LocalInexistenteXYZ\") -> " + inexistente);

        // 2. AdminGateway.insertaReviewFalsa
        Local objetivoFalsa = barTxoko != null ? barTxoko : (encontrados.length > 0 ? encontrados[0] : null);
        if (objetivoFalsa != null) {
            Boolean falsaOk = invocarConReconexion(scanner, host, puerto, g -> g.insertaReviewFalsa(objetivoFalsa, 5));
            Boolean falsaInvalida = invocarConReconexion(scanner, host, puerto, g -> g.insertaReviewFalsa(objetivoFalsa, 99));
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
        Boolean elimContestada = invocarConReconexion(scanner, host, puerto, g -> g.eliminaReview(revConContestacion));
        Boolean elimNula = invocarConReconexion(scanner, host, puerto, g -> g.eliminaReview(null));
        System.out.println("3) eliminaReview(Pub Subsuelo, carlos) -> " + elimContestada
                + " | eliminaReview(null) -> " + elimNula);

        // 4. AdminGateway.eliminaReviewsDeUsuario
        Cliente usuarioNoExiste = new Cliente("fantasma_xyz", "pw", LocalDate.of(1992, 1, 1));
        Cliente diego = new Cliente("diego", "pw", LocalDate.of(1988, 1, 30));
        Cliente marta = new Cliente("marta", "pw", LocalDate.of(2000, 11, 5));

        Integer elimNoExiste = invocarConReconexion(scanner, host, puerto, g -> g.eliminaReviewsDeUsuario(usuarioNoExiste));
        Integer elimDiego = invocarConReconexion(scanner, host, puerto, g -> g.eliminaReviewsDeUsuario(diego));
        Integer elimMarta = invocarConReconexion(scanner, host, puerto, g -> g.eliminaReviewsDeUsuario(marta));
        System.out.println("4) eliminaReviewsDeUsuario -> inexistente=" + elimNoExiste
                + ", diego=" + elimDiego + ", marta=" + elimMarta);

        // 5. AdminGateway.eliminaReviewsDeLocal
        Boolean elimRevsLocal = invocarConReconexion(scanner, host, puerto, g -> g.eliminaReviewsDeLocal(barFitero));
        Boolean elimRevsNull = invocarConReconexion(scanner, host, puerto, g -> g.eliminaReviewsDeLocal(null));
        System.out.println("5) eliminaReviewsDeLocal(Bar Fitero) -> " + elimRevsLocal
                + " | null -> " + elimRevsNull);

        // 6. AdminGateway.eliminaLocal
        Boolean elimLocal = invocarConReconexion(scanner, host, puerto, g -> g.eliminaLocal(barFitero));
        Boolean elimLocalNull = invocarConReconexion(scanner, host, puerto, g -> g.eliminaLocal(null));
        System.out.println("6) eliminaLocal(Bar Fitero) -> " + elimLocal
                + " | null -> " + elimLocalNull);
    }
}
