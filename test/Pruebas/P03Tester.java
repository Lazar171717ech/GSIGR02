package Pruebas;

import GSILabs.BModel.Bar;
import GSILabs.BModel.Cliente;
import GSILabs.BModel.Contestacion;
import GSILabs.BModel.Direccion;
import GSILabs.BModel.Dueño;
import GSILabs.BModel.Review;
import GSILabs.BSystem.BusinessSystem;
import GSILabs.persistence.XMLParsingException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Clase de pruebas para verificar la funcionalidad de lectura y carga de ficheros XML
 * implementada en la Práctica 03 (Ejercicio 5). Genera sus propios ficheros de prueba
 * dentro de la carpeta resources/ del proyecto (ruta relativa).
 *
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class P03Tester {

    private static void resultado(String test, boolean ok) {
        System.out.println("[" + test + " - " + (ok ? "ÉXITO" : "FALLO") + "]");
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== INICIO DE PRUEBAS DE LA PRÁCTICA 03 (XML) ===");

        // Sistema de ejemplo con todos los tipos de datos
        BusinessSystem s = new BusinessSystem();
        Cliente c = new Cliente("ana", "pw", LocalDate.of(1995, 3, 2));
        Dueño d = new Dueño("luis", "pw", LocalDate.of(1980, 5, 9));
        Bar bar = new Bar("Txoko", new Direccion("Pamplona", "Navarra", "Calle Mayor", 3), "Pintxos");
        s.nuevoUsuario(c);
        s.nuevoUsuario(d);
        s.nuevoLocal(bar);
        s.asociarLocal(bar, d);
        Review r = new Review(c, bar, 5, "Genial & rico", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2));
        s.nuevaReview(r);
        s.nuevaContestacion(new Contestacion(d, r, "Gracias", LocalDate.of(2026, 10, 3)), r);
        s.nuevaReserva(c, bar, LocalDate.now().plusDays(7), LocalTime.of(21, 0));

        File valido = new File("resources/test_valido.xml");
        File malFormado = new File("resources/test_invalido.xml");
        File sinSeccion = new File("resources/test_sin_seccion.xml");

        s.saveToXML(valido);
        String xml = new String(Files.readAllBytes(valido.toPath()), StandardCharsets.UTF_8);
        Files.write(malFormado.toPath(), xml.substring(0, xml.length() / 2).getBytes(StandardCharsets.UTF_8));
        Files.write(sinSeccion.toPath(), xml.replace("reviews>", "reviewz>").getBytes(StandardCharsets.UTF_8));

        // --- PRUEBA 1: parseXMLFile con fichero válido (round-trip) ---
        try {
            BusinessSystem copia = BusinessSystem.parseXMLFile(valido);
            Cliente cc = (Cliente) copia.obtenerUsuario("ana");
            boolean ok = cc != null
                    && copia.obtenerUsuario("luis") instanceof Dueño
                    && copia.obtenerTodosLosLocales().length == 1
                    && copia.verReviews(copia.obtenerTodosLosLocales()[0]).length == 1
                    && copia.obtenerReservas(cc).length == 1
                    && copia.tieneContestacion(copia.verReviews(copia.obtenerTodosLosLocales()[0])[0])
                    && copia.obtenerDueñosDeLocal(copia.obtenerTodosLosLocales()[0]).length == 1;
            resultado("TEST 1 parseXMLFile válido", ok);
        } catch (XMLParsingException e) {
            System.err.println("[TEST 1 - ERROR] Excepción inesperada: " + e.getMessage());
        }

        // --- PRUEBA 2: parseXMLFile con XML mal formado ---
        try {
            BusinessSystem.parseXMLFile(malFormado);
            resultado("TEST 2 parseXMLFile mal formado", false);
        } catch (XMLParsingException e) {
            resultado("TEST 2 parseXMLFile mal formado (" + e.getMessage() + ")", true);
        }

        // --- PRUEBA 3: parseXMLFile con una sección que falta ---
        try {
            BusinessSystem.parseXMLFile(sinSeccion);
            resultado("TEST 3 parseXMLFile sin sección", false);
        } catch (XMLParsingException e) {
            resultado("TEST 3 parseXMLFile sin sección (" + e.getMessage() + ")", true);
        }

        // --- PRUEBA 4: loadXMLFile con fichero válido ---
        BusinessSystem actual = new BusinessSystem();
        boolean cargado = actual.loadXMLFile(valido);
        resultado("TEST 4 loadXMLFile válido", cargado && actual.obtenerUsuario("ana") != null);

        // --- PRUEBA 5: loadXMLFile con fichero inválido conserva los datos ---
        boolean fallo = actual.loadXMLFile(malFormado);
        resultado("TEST 5 loadXMLFile inválido", !fallo && actual.obtenerUsuario("ana") != null);

        System.out.println("=== FIN DE LAS PRUEBAS ===");
    }
}