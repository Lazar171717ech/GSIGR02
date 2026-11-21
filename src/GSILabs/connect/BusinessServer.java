package GSILabs.connect;

import GSILabs.BModel.Bar;
import GSILabs.BModel.Cliente;
import GSILabs.BModel.Contestacion;
import GSILabs.BModel.Direccion;
import GSILabs.BModel.Dueño;
import GSILabs.BModel.Pub;
import GSILabs.BModel.Restaurante;
import GSILabs.BModel.Review;
import GSILabs.BSystem.BusinessSystem;
import GSILabs.BSystem.PublicBusinessSystem;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Servidor RMI principal del sistema de información. Crea e inicializa
 * programáticamente una instancia de {@link PublicBusinessSystem}, la exporta
 * mediante {@link UnicastRemoteObject#exportObject(Remote, int)} y la publica
 * en un registro RMI bajo los tags {@code "ClientGateway"} y {@code "AdminGateway"}.
 *
 * <p>Permite indicar opcionalmente un puerto alternativo como primer argumento de
 * línea de comandos y una IP/hostname para {@code java.rmi.server.hostname} como
 * segundo argumento (útil para pruebas entre máquinas distintas en Linux).
 *
 * @author Joel Vílchez, Urko Inés, Aimar Ijurko, Iker Fernández, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class BusinessServer {

    /** Puerto RMI por defecto según el guión de la Práctica 04. */
    public static final int DEFAULT_PORT = 1099;

    /** Identificador en el registro RMI para clientes finales. */
    public static final String CLIENT_GATEWAY_TAG = "ClientGateway";

    /** Identificador en el registro RMI para administradores. */
    public static final String ADMIN_GATEWAY_TAG = "AdminGateway";

    // Referencias fuertes estáticas para impedir que el recolector de basura desexporte el stub o el registro
    private static PublicBusinessSystem sistemaPublico;
    private static Registry registro;
    private static Remote stub;

    /**
     * Constructor por defecto.
     */
    public BusinessServer() {
    }

    /**
     * Construye y puebla programáticamente un {@link BusinessSystem} con datos de
     * prueba variados (clientes, dueños, bares, restaurantes, pub, reviews con y sin
     * contestación) sin depender de ficheros externos ni del directorio de trabajo.
     *
     * @return Instancia poblada de {@link BusinessSystem}.
     */
    public static BusinessSystem crearSistemaPoblado() {
        BusinessSystem bs = new BusinessSystem();

        // Clientes
        Cliente ana = new Cliente("ana", "ana1234", LocalDate.of(1995, 3, 15));
        Cliente carlos = new Cliente("carlos", "carlos1234", LocalDate.of(1990, 7, 22));
        Cliente marta = new Cliente("marta", "marta1234", LocalDate.of(2000, 11, 5));
        Cliente diego = new Cliente("diego", "diego1234", LocalDate.of(1988, 1, 30));
        bs.nuevoUsuario(ana);
        bs.nuevoUsuario(carlos);
        bs.nuevoUsuario(marta);
        bs.nuevoUsuario(diego);

        // Dueños
        Dueño duenoTxoko = new Dueño("dueno_txoko", "pass1", LocalDate.of(1978, 4, 10));
        Dueño duenoEuropa = new Dueño("dueno_europa", "pass2", LocalDate.of(1975, 9, 12));
        Dueño duenoSubsuelo = new Dueño("dueno_subsuelo", "pass3", LocalDate.of(1982, 6, 18));
        bs.nuevoUsuario(duenoTxoko);
        bs.nuevoUsuario(duenoEuropa);
        bs.nuevoUsuario(duenoSubsuelo);

        // Locales: bares, restaurantes y pub
        Bar barTxoko = new Bar(
                "Bar Txoko",
                new Direccion("Pamplona", "Navarra", "Plaza del Castillo", 20),
                "Pintxos tradicionales y terraza");
        barTxoko.agregarEspecialidad("Tortilla");
        barTxoko.agregarEspecialidad("Croquetas");

        Bar barGaucho = new Bar(
                "Bar Gaucho",
                new Direccion("Pamplona", "Navarra", "Calle Espoz y Mina", 7),
                "Famoso por sus pintxos de foie y trufa");
        barGaucho.agregarEspecialidad("Foie");
        barGaucho.agregarEspecialidad("Erizo");

        Bar barFitero = new Bar(
                "Bar Fitero",
                new Direccion("Pamplona", "Navarra", "Calle Estafeta", 58),
                "Bar clásico del casco viejo");
        barFitero.agregarEspecialidad("Frito de pimiento");

        Bar barLaCepa = new Bar(
                "Bar La Cepa",
                new Direccion("Tudela", "Navarra", "Calle Herrerías", 14),
                "Vinos y tapas en Tudela");
        barLaCepa.agregarEspecialidad("Alcachofas");

        Restaurante restEuropa = new Restaurante(
                "Restaurante Europa",
                new Direccion("Pamplona", "Navarra", "Calle Espoz y Mina", 11),
                "Cocina navarra de autor",
                75.0f, 60, 8);

        Restaurante restRodero = new Restaurante(
                "Restaurante Rodero",
                new Direccion("Pamplona", "Navarra", "Calle Emilio Arrieta", 3),
                "Alta cocina contemporánea",
                85.0f, 50, 6);

        Restaurante restBaserri = new Restaurante(
                "Restaurante Baserri",
                new Direccion("Pamplona", "Navarra", "Calle San Nicolás", 32),
                "Cocina tradicional y brasa",
                40.0f, 80, 10);

        Pub pubSubsuelo = new Pub(
                "Pub Subsuelo",
                new Direccion("Pamplona", "Navarra", "Plaza del Castillo", 44),
                "Música en directo y copas",
                LocalTime.of(22, 0),
                LocalTime.of(4, 30));

        bs.nuevoLocal(barTxoko);
        bs.nuevoLocal(barGaucho);
        bs.nuevoLocal(barFitero);
        bs.nuevoLocal(barLaCepa);
        bs.nuevoLocal(restEuropa);
        bs.nuevoLocal(restRodero);
        bs.nuevoLocal(restBaserri);
        bs.nuevoLocal(pubSubsuelo);

        bs.asociarLocal(barTxoko, duenoTxoko);
        bs.asociarLocal(barGaucho, duenoTxoko);
        bs.asociarLocal(barFitero, duenoTxoko);
        bs.asociarLocal(barLaCepa, duenoTxoko);
        bs.asociarLocal(restEuropa, duenoEuropa);
        bs.asociarLocal(restRodero, duenoEuropa);
        bs.asociarLocal(restBaserri, duenoEuropa);
        bs.asociarLocal(pubSubsuelo, duenoSubsuelo);

        // Reviews (algunas con contestación y otras sin ella)
        Review revGauchoAna = new Review(
                ana, barGaucho, 5, "Excelente foie a la plancha",
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 11));
        bs.nuevaReview(revGauchoAna);
        bs.nuevaContestacion(new Contestacion(
                duenoTxoko, revGauchoAna, "Muchas gracias por tu visita, Ana",
                LocalDate.of(2026, 10, 12)), revGauchoAna);

        bs.nuevaReview(new Review(
                carlos, barGaucho, 5, "Imprescindible en Pamplona",
                LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 12)));

        bs.nuevaReview(new Review(
                ana, barTxoko, 4, "Muy buenos pintxos en la plaza",
                LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 16)));
        bs.nuevaReview(new Review(
                marta, barTxoko, 3, "Ambiente animado aunque algo lleno",
                LocalDate.of(2026, 10, 16), LocalDate.of(2026, 10, 16)));

        bs.nuevaReview(new Review(
                carlos, barFitero, 2, "Servicio lento en hora punta",
                LocalDate.of(2026, 10, 18), LocalDate.of(2026, 10, 18)));

        bs.nuevaReview(new Review(
                marta, barLaCepa, 4, "Verduras muy buenas",
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 20)));

        Review revRoderoAna = new Review(
                ana, restRodero, 5, "Menú degustación impecable",
                LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 6));
        bs.nuevaReview(revRoderoAna);
        bs.nuevaContestacion(new Contestacion(
                duenoEuropa, revRoderoAna, "Encantados de recibirte de nuevo",
                LocalDate.of(2026, 10, 7)), revRoderoAna);

        bs.nuevaReview(new Review(
                carlos, restEuropa, 4, "Muy buena relación calidad-precio",
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 9)));

        bs.nuevaReview(new Review(
                marta, restBaserri, 3, "Correcto para cenar en grupo",
                LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 10)));

        Review revPubCarlos = new Review(
                carlos, pubSubsuelo, 4, "Buen sonido en los conciertos",
                LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 23));
        bs.nuevaReview(revPubCarlos);
        bs.nuevaContestacion(new Contestacion(
                duenoSubsuelo, revPubCarlos, "Gracias por apoyar la música en directo",
                LocalDate.of(2026, 10, 24)), revPubCarlos);

        return bs;
    }

    /**
     * Arranca el servidor RMI siguiendo los cuatro pasos indicados en el guión:
     * instanciación, exportación con {@code UnicastRemoteObject.exportObject},
     * creación del registro RMI y enlace bajo {@code ClientGateway} y {@code AdminGateway}.
     *
     * @param args Opcionalmente {@code args[0]} indica el puerto (por defecto 1099)
     *             y {@code args[1]} la IP/hostname para {@code java.rmi.server.hostname}.
     */
    public static void main(String[] args) {
        int puerto = DEFAULT_PORT;
        if (args.length >= 1 && args[0] != null && !args[0].trim().isEmpty()) {
            try {
                puerto = Integer.parseInt(args[0].trim());
                if (puerto < 1 || puerto > 65535) {
                    System.err.println("Puerto fuera de rango (1-65535): " + puerto);
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido '" + args[0] + "'. Uso: BusinessServer [puerto] [hostname]");
                System.exit(1);
            }
        }

        if (args.length >= 2 && args[1] != null && !args[1].trim().isEmpty()) {
            String hostActual = System.getProperty("java.rmi.server.hostname");
            if (hostActual == null || hostActual.trim().isEmpty()) {
                System.setProperty("java.rmi.server.hostname", args[1].trim());
            }
        }

        try {
            // 1. Crear instancia del objeto a publicar
            BusinessSystem bs = crearSistemaPoblado();
            sistemaPublico = new PublicBusinessSystem(bs);

            // 2. Generar stub del objeto usando UnicastRemoteObject.exportObject
            stub = UnicastRemoteObject.exportObject(sistemaPublico, 0);

            // 3. Crear registro RMI en el puerto indicado (1099 por defecto)
            registro = LocateRegistry.createRegistry(puerto);

            // 4. Asociar el stub en el registro bajo ambos identificadores
            registro.rebind(CLIENT_GATEWAY_TAG, stub);
            registro.rebind(ADMIN_GATEWAY_TAG, stub);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (sistemaPublico != null) {
                        UnicastRemoteObject.unexportObject(sistemaPublico, true);
                    }
                } catch (NoSuchObjectException ignored) {
                }
            }));

            String rmiHostname = System.getProperty("java.rmi.server.hostname");
            System.out.println("=== SERVIDOR RMI GSIGR02 INICIADO ===");
            System.out.println("Puerto RMI Registry : " + puerto);
            System.out.println("Hostname anunciado  : "
                    + (rmiHostname != null && !rmiHostname.trim().isEmpty()
                    ? rmiHostname
                    : "(por defecto de JVM — en Linux entre máquinas distintas usa -Djava.rmi.server.hostname=<IP>)"));
            System.out.println("Tags publicados     : " + CLIENT_GATEWAY_TAG + ", " + ADMIN_GATEWAY_TAG);
            System.out.println("Locales cargados    : " + bs.obtenerTodosLosLocales().length
                    + " (Bar Txoko, Bar Gaucho, Bar Fitero, Bar La Cepa, Restaurante Europa/Rodero/Baserri, Pub Subsuelo)");
            System.out.println("Clientes de ejemplo : ana, carlos, marta, diego");
            System.out.println("Servidor en espera de peticiones (pulsa Ctrl+C para detener)...");
        } catch (RemoteException e) {
            System.err.println("No se pudo iniciar el servidor RMI en el puerto " + puerto + ": " + e.getMessage());
            System.err.println("Comprueba si el puerto ya está ocupado o prueba con un puerto alternativo (p. ej. 1100).");
            try {
                if (sistemaPublico != null) {
                    UnicastRemoteObject.unexportObject(sistemaPublico, true);
                }
            } catch (NoSuchObjectException ignored) {
            }
            System.exit(1);
        }
    }
}
