package Pruebas;

import GSILabs.BModel.Bar;
import GSILabs.BModel.Cliente;
import GSILabs.BModel.Direccion;
import GSILabs.BModel.Local;
import GSILabs.BModel.Pub;
import GSILabs.BModel.Reserva;
import GSILabs.BModel.Restaurante;
import GSILabs.BModel.Review;
import GSILabs.BSystem.BusinessSystem;
import GSILabs.BSystem.PublicBusinessSystem;
import GSILabs.connect.AdminGateway;
import GSILabs.connect.BusinessServer;
import GSILabs.connect.ClientGateway;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Suite de pruebas automatizadas para la Práctica 04 (Acceso remoto con Java RMI).
 *
 * <p>Levanta un registro RMI local en un puerto de pruebas, exporta una instancia
 * de {@link PublicBusinessSystem} bajo {@code ClientGateway} y {@code AdminGateway},
 * y valida todos los métodos remotos, casos límite, serialización {@code transient}
 * de contraseñas, integridad XML, concurrencia y reconexión tras desexportar.
 *
 * @author Joel Vílchez, Urko Inés, Aimar Ijurko, Iker Fernández, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class P04Tester {

    private static final int BASE_TEST_PORT = 11099;
    private static int totalPruebas = 0;
    private static int pruebasCorrectas = 0;
    private static int pruebasFallidas = 0;

    /**
     * Constructor por defecto de P04Tester.
     */
    public P04Tester() {
    }

    private static void comprobar(boolean condicion, String descripcion) {
        totalPruebas++;
        if (condicion) {
            pruebasCorrectas++;
            System.out.println("  [OK] " + descripcion);
        } else {
            pruebasFallidas++;
            System.err.println("  [FALLO] " + descripcion);
        }
    }

    private static Registry crearRegistroPruebas(int[] puertoOut) throws RemoteException {
        RemoteException ultimoError = null;
        for (int p = BASE_TEST_PORT; p <= BASE_TEST_PORT + 20; p++) {
            try {
                Registry reg = LocateRegistry.createRegistry(p);
                puertoOut[0] = p;
                return reg;
            } catch (RemoteException e) {
                ultimoError = e;
            }
        }
        throw ultimoError != null ? ultimoError : new RemoteException("No se encontró puerto libre para el registro RMI.");
    }

    /**
     * Ejecuta la batería completa de pruebas de la Práctica 04.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        System.out.println("=== INICIO DE PRUEBAS DE LA PRÁCTICA 04 (JAVA RMI) ===");

        Registry registro = null;
        PublicBusinessSystem activo = null;
        File tempXml = null;

        try {
            BusinessSystem bs = BusinessServer.crearSistemaPoblado();

            // Bar adicional sin reviews (media 0.0f) para probar mejorBar cuando no hay valoraciones
            Bar barEstella = new Bar(
                    "Bar Estella Cero",
                    new Direccion("Estella", "Navarra", "Plaza de los Fueros", 5),
                    "Bar recién inaugurado sin valoraciones");
            bs.nuevoLocal(barEstella);

            // Reserva futura para comprobar que eliminaReviewsDeUsuario no borra usuario ni reservas
            Cliente martaOrig = (Cliente) bs.obtenerUsuario("marta");
            Bar barTxokoOrig = (Bar) bs.obtenerLocal(new Direccion("Pamplona", "Navarra", "Plaza del Castillo", 20));
            bs.nuevaReserva(martaOrig, barTxokoOrig, LocalDate.now().plusDays(10), LocalTime.of(21, 30));

            activo = new PublicBusinessSystem(bs);
            Remote stub = UnicastRemoteObject.exportObject(activo, 0);

            int[] puertoUsado = new int[1];
            registro = crearRegistroPruebas(puertoUsado);
            registro.rebind(BusinessServer.CLIENT_GATEWAY_TAG, stub);
            registro.rebind(BusinessServer.ADMIN_GATEWAY_TAG, stub);

            Registry clienteReg = LocateRegistry.getRegistry("localhost", puertoUsado[0]);
            ClientGateway clientGw = (ClientGateway) clienteReg.lookup(BusinessServer.CLIENT_GATEWAY_TAG);
            AdminGateway adminGw = (AdminGateway) clienteReg.lookup(BusinessServer.ADMIN_GATEWAY_TAG);

            // ------------------------------------------------------------------
            // 1. Pruebas de LocalFinder
            // ------------------------------------------------------------------
            System.out.println("\n[1/6] Pruebas de LocalFinder...");
            Local exacto = clientGw.getLocal("  bAr   tXoKo  ");
            comprobar(exacto != null && "Bar Txoko".equals(exacto.getNombre()),
                    "getLocal encuentra coincidencia exacta ignorando mayúsculas y espacios sobrantes");
            comprobar(clientGw.getLocal("Bar") == null,
                    "getLocal no devuelve resultados parciales");
            comprobar(clientGw.getLocal(null) == null
                    && clientGw.getLocal("") == null
                    && clientGw.getLocal("   ") == null,
                    "getLocal devuelve null ante null o cadenas vacías");

            Local[] bares = clientGw.getLocals("  bAr ");
            comprobar(bares != null && bares.length == 5,
                    "getLocals(\"bar\") encuentra los 5 bares (parcial + insensible a mayúsculas)");
            Local[] restaurantes = clientGw.getLocals("Restaurante");
            comprobar(restaurantes != null && restaurantes.length == 3,
                    "getLocals(\"Restaurante\") encuentra los 3 restaurantes");
            Local[] pubs = clientGw.getLocals("pub");
            comprobar(pubs != null && pubs.length == 1 && pubs[0] instanceof Pub,
                    "getLocals(\"pub\") encuentra el Pub Subsuelo");
            Local[] vacioNull = clientGw.getLocals(null);
            Local[] vacioBlank = clientGw.getLocals("   ");
            Local[] vacioInex = clientGw.getLocals("NoExisteEsteLocalXYZ");
            comprobar(vacioNull != null && vacioNull.length == 0
                    && vacioBlank != null && vacioBlank.length == 0
                    && vacioInex != null && vacioInex.length == 0,
                    "getLocals devuelve array vacío (no null) para null, blanco o sin coincidencias");

            // ------------------------------------------------------------------
            // 2. Pruebas de ClientGateway
            // ------------------------------------------------------------------
            System.out.println("\n[2/6] Pruebas de ClientGateway...");
            Bar mejorPamplona = clientGw.mejorBar("  pAmPlOnA ");
            comprobar(mejorPamplona != null && "Bar Gaucho".equals(mejorPamplona.getNombre()),
                    "mejorBar(\"Pamplona\") devuelve Bar Gaucho (media 5.0)");
            Bar mejorTudela = clientGw.mejorBar("Tudela");
            comprobar(mejorTudela != null && "Bar La Cepa".equals(mejorTudela.getNombre()),
                    "mejorBar(\"Tudela\") devuelve Bar La Cepa");
            Bar mejorEstella = clientGw.mejorBar("Estella");
            comprobar(mejorEstella != null && "Bar Estella Cero".equals(mejorEstella.getNombre()),
                    "mejorBar(\"Estella\") devuelve el bar aunque su media sea 0.0");
            comprobar(clientGw.mejorBar("CiudadSinBares") == null
                    && clientGw.mejorBar(null) == null
                    && clientGw.mejorBar("   ") == null,
                    "mejorBar devuelve null si no hay bares en la ciudad o ciudad es nula/vacía");

            Restaurante[] top2 = clientGw.mejoresRestaurantes("pamplona", 2);
            comprobar(top2 != null && top2.length == 2
                    && top2[0] != null && "Restaurante Rodero".equals(top2[0].getNombre())
                    && top2[1] != null && "Restaurante Europa".equals(top2[1].getNombre()),
                    "mejoresRestaurantes(\"pamplona\", 2) ordena descendente y recorta a 2");

            Restaurante[] top5 = clientGw.mejoresRestaurantes("Pamplona", 5);
            comprobar(top5 != null && top5.length == 5
                    && "Restaurante Rodero".equals(top5[0].getNombre())
                    && "Restaurante Europa".equals(top5[1].getNombre())
                    && "Restaurante Baserri".equals(top5[2].getNombre())
                    && top5[3] == null && top5[4] == null,
                    "mejoresRestaurantes(\"Pamplona\", 5) devuelve 3 ordenados y 2 nulls al final");

            Restaurante[] ciudadVacia = clientGw.mejoresRestaurantes("Soria", 3);
            Restaurante[] ciudadNula = clientGw.mejoresRestaurantes(null, 3);
            comprobar(ciudadVacia != null && ciudadVacia.length == 3
                    && ciudadVacia[0] == null && ciudadVacia[1] == null && ciudadVacia[2] == null
                    && ciudadNula != null && ciudadNula.length == 3 && ciudadNula[0] == null,
                    "mejoresRestaurantes rellena con nulls si ciudad es desconocida o nula");
            Restaurante[] cero = clientGw.mejoresRestaurantes("Pamplona", 0);
            comprobar(cero != null && cero.length == 0,
                    "mejoresRestaurantes con num=0 devuelve array de longitud 0");
            comprobar(clientGw.mejoresRestaurantes("Pamplona", -1) == null
                    && clientGw.mejoresRestaurantes("Pamplona", null) == null,
                    "mejoresRestaurantes devuelve null si num es negativo o null");

            Cliente anaCopia = new Cliente("ana", "otraPass", LocalDate.of(1995, 3, 15));
            LocalDate fechaNueva = LocalDate.of(2026, 11, 20);
            Review reviewValida = new Review(anaCopia, barTxokoOrig, 5, "Excelente vía RMI", fechaNueva, LocalDate.now());
            comprobar(clientGw.insertaReview(reviewValida),
                    "insertaReview inserta review válida enviada por RMI");
            comprobar(!clientGw.insertaReview(reviewValida),
                    "insertaReview rechaza duplicado para mismo cliente, local y fecha");
            comprobar(!clientGw.insertaReview(null),
                    "insertaReview(null) devuelve false");

            Cliente noRegistrado = new Cliente("desconocido", "pw", LocalDate.of(1995, 1, 1));
            Local localNoRegistrado = new Bar("Fantasma", new Direccion("Pamplona", "Navarra", "Sin Calle", 99), "");
            comprobar(!clientGw.insertaReview(new Review(noRegistrado, barTxokoOrig, 4, "X", fechaNueva.plusDays(1), LocalDate.now())),
                    "insertaReview rechaza cliente no registrado");
            comprobar(!clientGw.insertaReview(new Review(anaCopia, localNoRegistrado, 4, "X", fechaNueva.plusDays(1), LocalDate.now())),
                    "insertaReview rechaza local no registrado");

            comprobar(clientGw.quitaReview(reviewValida),
                    "quitaReview elimina una review existente sin contestación");
            comprobar(!clientGw.quitaReview(reviewValida),
                    "quitaReview devuelve false al intentar eliminarla por segunda vez");

            Bar barGauchoOrig = new Bar("Bar Gaucho", new Direccion("Pamplona", "Navarra", "Calle Espoz y Mina", 7), "");
            Review gauchoContestada = new Review(anaCopia, barGauchoOrig, 5, "X",
                    LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 11));
            comprobar(!clientGw.quitaReview(gauchoContestada) && bs.tieneContestacion(gauchoContestada),
                    "quitaReview NO elimina review que tiene contestación asociada");
            comprobar(!clientGw.quitaReview(null),
                    "quitaReview(null) devuelve false");

            // ------------------------------------------------------------------
            // 3. Pruebas de Serialización transient y XML en servidor
            // ------------------------------------------------------------------
            System.out.println("\n[3/6] Pruebas de serialización transient e integridad XML...");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(new Cliente("ana", "secretoSuperSeguro", LocalDate.of(1995, 3, 15)));
            }
            Cliente anaDeserializada;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
                anaDeserializada = (Cliente) ois.readObject();
            }
            comprobar(anaDeserializada.getPassword() == null
                    && "ana".equals(anaDeserializada.getNick())
                    && anaDeserializada.equals(anaCopia),
                    "Usuario.password es transient durante la serialización Java/RMI conservando nick y equals");

            Review revParaXml = new Review(
                    new Cliente("ana", "passwordTemporalCliente", LocalDate.of(1995, 3, 15)),
                    new Bar("Bar Txoko", new Direccion("Pamplona", "Navarra", "Plaza del Castillo", 20), "Desc diferente"),
                    5, "Comentario para comprobar entidad canónica", LocalDate.of(2026, 11, 21), LocalDate.now());
            comprobar(clientGw.insertaReview(revParaXml),
                    "insertaReview acepta review de instancia deserializada de cliente y local");
            tempXml = File.createTempFile("p04_xml_test_", ".xml");
            boolean guardadoXml = activo.getBusinessSystem().saveToXML(tempXml);
            BusinessSystem recargado = BusinessSystem.parseXMLFile(tempXml);
            comprobar(guardadoXml && recargado.obtenerUsuario("ana") != null
                    && "ana1234".equals(recargado.obtenerUsuario("ana").getPassword()),
                    "saveToXML/parseXMLFile tras RMI mantiene la instancia canónica de Cliente y su password en el servidor");
            // Limpiamos esta review auxiliar antes de contar reviews de AdminGateway
            clientGw.quitaReview(revParaXml);

            // ------------------------------------------------------------------
            // 4. Pruebas de AdminGateway
            // ------------------------------------------------------------------
            System.out.println("\n[4/6] Pruebas de AdminGateway...");
            Pub pubSubsuelo = new Pub("Pub Subsuelo",
                    new Direccion("Pamplona", "Navarra", "Plaza del Castillo", 44),
                    "Música", LocalTime.of(22, 0), LocalTime.of(4, 30));
            Review pubRev = new Review(new Cliente("carlos", "pw", LocalDate.of(1990, 7, 22)),
                    pubSubsuelo, 4, "X", LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 23));
            comprobar(Boolean.TRUE.equals(adminGw.eliminaReview(pubRev))
                    && !bs.tieneContestacion(pubRev)
                    && bs.verReviews(pubSubsuelo).length == 0,
                    "AdminGateway.eliminaReview elimina review aunque tenga contestación y borra la contestación");
            comprobar(Boolean.FALSE.equals(adminGw.eliminaReview(pubRev))
                    && Boolean.FALSE.equals(adminGw.eliminaReview(null)),
                    "AdminGateway.eliminaReview devuelve false si no existe o es null");

            comprobar(Boolean.FALSE.equals(adminGw.insertaReviewFalsa(null, 4))
                    && Boolean.FALSE.equals(adminGw.insertaReviewFalsa(barTxokoOrig, null))
                    && Boolean.FALSE.equals(adminGw.insertaReviewFalsa(barTxokoOrig, -1))
                    && Boolean.FALSE.equals(adminGw.insertaReviewFalsa(barTxokoOrig, 6))
                    && Boolean.FALSE.equals(adminGw.insertaReviewFalsa(localNoRegistrado, 3)),
                    "insertaReviewFalsa devuelve false sin excepciones ante parámetros o local inválidos");

            int reviewsTxokoAntes = bs.verReviews(barTxokoOrig).length;
            boolean falsa1 = Boolean.TRUE.equals(adminGw.insertaReviewFalsa(barTxokoOrig, 5));
            boolean falsa2 = Boolean.TRUE.equals(adminGw.insertaReviewFalsa(barTxokoOrig, 0));
            boolean falsa3 = Boolean.TRUE.equals(adminGw.insertaReviewFalsa(barTxokoOrig, 4));
            Review[] reviewsTxokoDespues = bs.verReviews(barTxokoOrig);
            Set<String> nicksFalsos = new HashSet<>();
            for (Review r : reviewsTxokoDespues) {
                if (r.getCliente().getNick().startsWith("fakeUser_")) {
                    nicksFalsos.add(r.getCliente().getNick());
                }
            }
            comprobar(falsa1 && falsa2 && falsa3
                    && reviewsTxokoDespues.length == reviewsTxokoAntes + 3
                    && nicksFalsos.size() == 3,
                    "insertaReviewFalsa permite múltiples llamadas seguidas con nicks únicos y notas 0..5");

            comprobar(Integer.valueOf(-1).equals(adminGw.eliminaReviewsDeUsuario(null))
                    && Integer.valueOf(-1).equals(adminGw.eliminaReviewsDeUsuario(noRegistrado))
                    && Integer.valueOf(-1).equals(adminGw.eliminaReviewsDeUsuario(
                            new Cliente("dueno_txoko", "pw", LocalDate.of(1978, 4, 10)))),
                    "eliminaReviewsDeUsuario devuelve -1 ante null, cliente inexistente o nick de un Dueño");

            Cliente diegoCopia = new Cliente("diego", "pw", LocalDate.of(1988, 1, 30));
            comprobar(Integer.valueOf(0).equals(adminGw.eliminaReviewsDeUsuario(diegoCopia)),
                    "eliminaReviewsDeUsuario devuelve 0 si el cliente existe pero no tiene reviews");

            Cliente martaCopia = new Cliente("marta", "pw", LocalDate.of(2000, 11, 5));
            Reserva[] reservasMartaAntes = bs.obtenerReservas(martaOrig);
            Integer borradasMarta = adminGw.eliminaReviewsDeUsuario(martaCopia);
            Reserva[] reservasMartaDespues = bs.obtenerReservas(martaOrig);
            comprobar(Integer.valueOf(3).equals(borradasMarta)
                    && bs.obtenerUsuario("marta") instanceof Cliente
                    && reservasMartaAntes != null && reservasMartaDespues != null
                    && reservasMartaAntes.length == 1 && reservasMartaDespues.length == 1,
                    "eliminaReviewsDeUsuario(marta) borra sus 3 reviews y conserva al cliente y sus reservas");
            comprobar(Integer.valueOf(0).equals(adminGw.eliminaReviewsDeUsuario(martaCopia)),
                    "Segunda llamada a eliminaReviewsDeUsuario(marta) devuelve 0");

            comprobar(Boolean.FALSE.equals(adminGw.eliminaReviewsDeLocal(null))
                    && Boolean.FALSE.equals(adminGw.eliminaReviewsDeLocal(localNoRegistrado)),
                    "eliminaReviewsDeLocal devuelve false ante null o local no registrado");
            comprobar(Boolean.TRUE.equals(adminGw.eliminaReviewsDeLocal(barGauchoOrig))
                    && bs.verReviews(barGauchoOrig).length == 0
                    && !bs.tieneContestacion(gauchoContestada)
                    && adminGw.getLocal("Bar Gaucho") != null,
                    "eliminaReviewsDeLocal borra reviews y contestaciones manteniendo el local registrado");
            comprobar(Boolean.TRUE.equals(adminGw.eliminaReviewsDeLocal(barGauchoOrig)),
                    "eliminaReviewsDeLocal devuelve true aunque el local ya tenga 0 reviews");

            Bar barFitero = new Bar("Bar Fitero", new Direccion("Pamplona", "Navarra", "Calle Estafeta", 58), "");
            comprobar(Boolean.FALSE.equals(adminGw.eliminaLocal(null))
                    && Boolean.FALSE.equals(adminGw.eliminaLocal(localNoRegistrado)),
                    "eliminaLocal devuelve false ante null o local no registrado");
            comprobar(Boolean.TRUE.equals(adminGw.eliminaLocal(barFitero))
                    && clientGw.getLocal("Bar Fitero") == null
                    && Boolean.FALSE.equals(adminGw.eliminaLocal(barFitero)),
                    "eliminaLocal borra un local existente y devuelve false si ya no existe");

            // ------------------------------------------------------------------
            // 5. Pruebas de Concurrencia
            // ------------------------------------------------------------------
            System.out.println("\n[5/6] Pruebas de concurrencia RMI...");
            int numHilos = 8;
            int opsPorHilo = 10;
            int reviewsEstellaAntes = bs.verReviews(barEstella).length;
            ExecutorService pool = Executors.newFixedThreadPool(numHilos);
            CountDownLatch listos = new CountDownLatch(numHilos);
            CountDownLatch inicio = new CountDownLatch(1);
            CountDownLatch fin = new CountDownLatch(numHilos);
            AtomicInteger erroresHilos = new AtomicInteger(0);
            List<Throwable> excepciones = new ArrayList<>();

            for (int t = 0; t < numHilos; t++) {
                pool.submit(() -> {
                    try {
                        listos.countDown();
                        inicio.await();
                        for (int k = 0; k < opsPorHilo; k++) {
                            clientGw.getLocals("Bar");
                            clientGw.mejorBar("Pamplona");
                            clientGw.mejoresRestaurantes("Pamplona", 3);
                            if (k < 2) {
                                if (!Boolean.TRUE.equals(adminGw.insertaReviewFalsa(barEstella, 4))) {
                                    erroresHilos.incrementAndGet();
                                }
                            }
                        }
                    } catch (Throwable ex) {
                        erroresHilos.incrementAndGet();
                        synchronized (excepciones) {
                            excepciones.add(ex);
                        }
                    } finally {
                        fin.countDown();
                    }
                });
            }

            listos.await(5, TimeUnit.SECONDS);
            inicio.countDown();
            boolean terminado = fin.await(15, TimeUnit.SECONDS);
            pool.shutdownNow();

            int reviewsEstellaDespues = bs.verReviews(barEstella).length;
            comprobar(terminado && erroresHilos.get() == 0 && excepciones.isEmpty()
                    && reviewsEstellaDespues == reviewsEstellaAntes + (numHilos * 2),
                    "8 hilos concurrentes completan lecturas y escrituras RMI sin errores ("
                            + reviewsEstellaDespues + " reviews en Bar Estella Cero)");

            // ------------------------------------------------------------------
            // 6. Pruebas de Caída y Reconexión RMI
            // ------------------------------------------------------------------
            System.out.println("\n[6/6] Pruebas de desconexión y reconexión...");
            UnicastRemoteObject.unexportObject(activo, true);
            activo = null;

            boolean lanzoRemoteException = false;
            try {
                clientGw.getLocal("Bar Txoko");
            } catch (RemoteException expected) {
                lanzoRemoteException = true;
            }
            comprobar(lanzoRemoteException,
                    "El stub antiguo lanza RemoteException cuando el servidor se cae / desexporta");

            activo = new PublicBusinessSystem(bs);
            Remote nuevoStub = UnicastRemoteObject.exportObject(activo, 0);
            registro.rebind(BusinessServer.CLIENT_GATEWAY_TAG, nuevoStub);
            registro.rebind(BusinessServer.ADMIN_GATEWAY_TAG, nuevoStub);

            ClientGateway nuevoClientGw = (ClientGateway) clienteReg.lookup(BusinessServer.CLIENT_GATEWAY_TAG);
            Local trasReconexion = nuevoClientGw.getLocal("Bar Txoko");
            comprobar(trasReconexion != null && "Bar Txoko".equals(trasReconexion.getNombre()),
                    "Tras reexportar y hacer rebind + nuevo lookup, la invocación remota vuelve a funcionar");

        } catch (Exception e) {
            pruebasFallidas++;
            System.err.println("  [ERROR INESPERADO] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            if (tempXml != null && tempXml.exists()) {
                tempXml.delete();
            }
            if (activo != null) {
                try {
                    UnicastRemoteObject.unexportObject(activo, true);
                } catch (Exception ignored) {
                }
            }
            if (registro != null) {
                try {
                    UnicastRemoteObject.unexportObject(registro, true);
                } catch (Exception ignored) {
                }
            }
        }

        System.out.println("\n=== RESUMEN P04Tester: " + pruebasCorrectas + "/" + totalPruebas
                + " pruebas correctas, " + pruebasFallidas + " fallidas ===");
        if (pruebasFallidas > 0) {
            System.exit(1);
        }
    }
}
