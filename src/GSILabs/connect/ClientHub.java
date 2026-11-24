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
    private String tagActual;
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
     * Permite recibir los parámetros tanto por argumentos de línea de comandos
     * (args[0]=host, args[1]=puerto, args[2]=tag) como de forma interactiva por consola.
     *
     * @param args Argumentos opcionales de línea de comandos.
     */
    public static void main(String[] args) {
        new ClientHub().ejecutar(args);
    }

    private void ejecutar(String[] args) {
        System.out.println("=== CLIENTE RMI: ClientHub ===");
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
            tag = pedirTextoConDefecto(scanner, "Tag del objeto remoto [ClientGateway]: ", DEFAULT_TAG);
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

    private ClientGateway conectarConEspera(Scanner scanner, String host, int puerto) {
        while (true) {
            try {
                Registry registry = LocateRegistry.getRegistry(host, puerto);
                Object obj = registry.lookup(tagActual);
                if (!(obj instanceof ClientGateway)) {
                    System.out.println("[ERROR] El objeto asociado al tag '" + tagActual
                            + "' no implementa ClientGateway (es de tipo "
                            + (obj != null ? obj.getClass().getName() : "null") + ").");
                    tagActual = pedirTextoConDefecto(scanner, "Introduce un tag válido [ClientGateway]: ", DEFAULT_TAG);
                    continue;
                }
                System.out.println("[CONECTADO] Enlace establecido con '" + tagActual + "' en " + host + ":" + puerto);
                return (ClientGateway) obj;
            } catch (NotBoundException e) {
                System.out.println("[ERROR] El tag '" + tagActual + "' no existe en el registro RMI de " + host + ":" + puerto + ".");
                tagActual = pedirTextoConDefecto(scanner, "Introduce un tag que esté publicado en el servidor [ClientGateway]: ", DEFAULT_TAG);
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
        System.out.println("\n--- Ejecución de funcionalidades de ClientGateway (#" + numeroEjecucion + ") ---");

        // 1. LocalFinder.getLocals
        Local[] encontrados = invocarConReconexion(scanner, host, puerto, g -> g.getLocals("bar"));
        if (encontrados.length == 0) {
            encontrados = invocarConReconexion(scanner, host, puerto, g -> g.getLocals("a"));
        }
        System.out.println("1) getLocals(\"bar\") -> " + encontrados.length + " local(es): "
                + Arrays.toString(encontrados));

        Local[] sinResultados = invocarConReconexion(scanner, host, puerto, g -> g.getLocals("LocalInexistenteXYZ"));
        System.out.println("   getLocals(\"LocalInexistenteXYZ\") -> longitud " + sinResultados.length);

        // 2. LocalFinder.getLocal
        Local localExacto = invocarConReconexion(scanner, host, puerto, g -> g.getLocal("  bar   txoko  "));
        System.out.println("2) getLocal(\"  bar   txoko  \") -> "
                + (localExacto != null ? localExacto.getNombre() : "null"));

        Local localInexistente = invocarConReconexion(scanner, host, puerto, g -> g.getLocal("NoExisteXYZ"));
        System.out.println("   getLocal(\"NoExisteXYZ\") -> " + localInexistente);

        // 3. ClientGateway.mejorBar
        Bar mejorBarPamplona = invocarConReconexion(scanner, host, puerto, g -> g.mejorBar("Pamplona"));
        System.out.println("3) mejorBar(\"Pamplona\") -> "
                + (mejorBarPamplona != null ? mejorBarPamplona.getNombre() : "null"));

        Bar mejorBarVacio = invocarConReconexion(scanner, host, puerto, g -> g.mejorBar("CiudadSinBaresXYZ"));
        System.out.println("   mejorBar(\"CiudadSinBaresXYZ\") -> " + mejorBarVacio);

        // 4. ClientGateway.mejoresRestaurantes
        Restaurante[] top5 = invocarConReconexion(scanner, host, puerto, g -> g.mejoresRestaurantes("Pamplona", 5));
        System.out.println("4) mejoresRestaurantes(\"Pamplona\", 5) -> " + formatearRestaurantes(top5));

        Restaurante[] top2 = invocarConReconexion(scanner, host, puerto, g -> g.mejoresRestaurantes("Pamplona", 2));
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

            boolean insertada = invocarConReconexion(scanner, host, puerto, g -> g.insertaReview(reviewNueva));
            boolean duplicada = invocarConReconexion(scanner, host, puerto, g -> g.insertaReview(reviewNueva));
            System.out.println("5) insertaReview(ana, " + localObjetivo.getNombre() + ", " + fechaVisita + ") -> "
                    + insertada + " | reintento duplicado -> " + duplicada);

            Cliente inexistente = new Cliente("no_existe_999", "pw", LocalDate.of(1995, 1, 1));
            Review reviewInvalida = new Review(inexistente, localObjetivo, 4, "Inválida", fechaVisita, LocalDate.now());
            boolean rechazada = invocarConReconexion(scanner, host, puerto, g -> g.insertaReview(reviewInvalida));
            System.out.println("   insertaReview(cliente inexistente) -> " + rechazada);

            boolean quitada = invocarConReconexion(scanner, host, puerto, g -> g.quitaReview(reviewNueva));
            boolean quitadaOtraVez = invocarConReconexion(scanner, host, puerto, g -> g.quitaReview(reviewNueva));
            System.out.println("6) quitaReview(nueva) -> " + quitada + " | segundo intento -> " + quitadaOtraVez);

            Bar gaucho = new Bar("Bar Gaucho", new Direccion("Pamplona", "Navarra", "Calle Espoz y Mina", 7), "Famoso");
            Review conContestacion = new Review(
                    ana, gaucho, 5, "Con contestación",
                    LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 11));
            boolean quitadaConResp = invocarConReconexion(scanner, host, puerto, g -> g.quitaReview(conContestacion));
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
