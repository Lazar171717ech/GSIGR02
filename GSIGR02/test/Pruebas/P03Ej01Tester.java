package Pruebas;

import GSILabs.BModel.Bar;
import GSILabs.BModel.Cliente;
import GSILabs.BModel.Contestacion;
import GSILabs.BModel.Direccion;
import GSILabs.BModel.Dueño;
import GSILabs.BModel.Local;
import GSILabs.BModel.Pub;
import GSILabs.BModel.Reserva;
import GSILabs.BModel.Restaurante;
import GSILabs.BModel.Review;
import GSILabs.serializable.XMLRepresentable;
import java.io.File;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Batería de pruebas unitarias para validar la implementación del Ejercicio 1 de la Práctica 03:
 * Implementación de XMLRepresentable en todas las clases de GSILabs.BModel.
 * 
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class P03Ej01Tester {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println(" INICIANDO BATERÍA DE PRUEBAS - PRÁCTICA 03 (EJERCICIO 1)");
        System.out.println("===============================================================");

        DocumentBuilder db = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (Exception e) {
            System.err.println("Error fatal inicializando DocumentBuilder: " + e.getMessage());
            System.exit(1);
        }

        File tempDir = new File("test_output_xml");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // 1. Probar Direccion
        testClass("Direccion", () -> {
            Direccion dir = new Direccion("Pamplona", "Navarra", "Calle Estafeta & Mayor", 12);
            assertXMLRepresentable(dir);
            String xml = dir.toXML();
            assertValidXML(xml, "direccion");
            assertTrue(xml.contains("Pamplona"), "Contiene localidad");
            assertTrue(xml.contains("&amp;"), "Escapa correctamente '&'");
            File f = new File(tempDir, "direccion.xml");
            assertTrue(dir.saveToXML(f), "saveToXML(File)");
            assertTrue(f.exists() && f.length() > 0, "Fichero creado correctamente");
            assertTrue(dir.saveToXML(f.getAbsolutePath()), "saveToXML(String)");
            assertFalse(dir.saveToXML((File) null), "saveToXML(null File) devuelve false");
            assertFalse(dir.saveToXML((String) null), "saveToXML(null String) devuelve false");
        }, db);

        // 2. Probar Cliente
        testClass("Cliente", () -> {
            Cliente cli = new Cliente("juan_perez", "pass123<secret>", LocalDate.of(1995, 4, 23));
            assertXMLRepresentable(cli);
            String xml = cli.toXML();
            assertValidXML(xml, "cliente");
            assertTrue(xml.contains("juan_perez"), "Contiene nick");
            assertTrue(xml.contains("&lt;secret&gt;"), "Escapa caracteres en password");
            assertTrue(xml.contains("1995-04-23"), "Contiene fecha ISO");
            File f = new File(tempDir, "cliente.xml");
            assertTrue(cli.saveToXML(f), "saveToXML(File)");
            assertTrue(cli.saveToXML(f.getAbsolutePath()), "saveToXML(String)");
        }, db);

        // 3. Probar Dueño
        testClass("Dueño", () -> {
            Dueño dueno = new Dueño("carlos_dueno", "admin\"pass", LocalDate.of(1982, 11, 10));
            assertXMLRepresentable(dueno);
            String xml = dueno.toXML();
            assertValidXML(xml, "dueño");
            assertTrue(xml.contains("carlos_dueno"), "Contiene nick");
            assertTrue(xml.contains("&quot;"), "Escapa comillas en password");
            File f = new File(tempDir, "dueno.xml");
            assertTrue(dueno.saveToXML(f), "saveToXML(File)");
            assertTrue(dueno.saveToXML(f.getAbsolutePath()), "saveToXML(String)");
        }, db);

        // 4. Probar Local (genérico)
        testClass("Local", () -> {
            Direccion dir = new Direccion("Barañáin", "Navarra", "Avenida Central", 4);
            Local local = new Local("Local Cultural", dir, "Espacio de exposiciones & talleres");
            assertXMLRepresentable(local);
            String xml = local.toXML();
            assertValidXML(xml, "local");
            assertTrue(xml.contains("Local Cultural"), "Contiene nombre");
            assertTrue(xml.contains("<direccion>"), "Contiene direccion embebida");
            File f = new File(tempDir, "local.xml");
            assertTrue(local.saveToXML(f), "saveToXML(File)");
            assertTrue(local.saveToXML(f.getAbsolutePath()), "saveToXML(String)");
        }, db);

        // 5. Probar Bar
        testClass("Bar", () -> {
            Direccion dir = new Direccion("Pamplona", "Navarra", "San Nicolás", 15);
            Bar bar = new Bar("Bar La Mandarra", dir, "Pinchos y raciones tradicionales");
            bar.agregarEspecialidad("Chistorra & Pimientos");
            bar.agregarEspecialidad("Tortilla rellena");
            assertXMLRepresentable(bar);
            String xml = bar.toXML();
            assertValidXML(xml, "bar");
            assertTrue(xml.contains("Bar La Mandarra"), "Contiene nombre");
            assertTrue(xml.contains("<especialidades>"), "Contiene bloque especialidades");
            assertTrue(xml.contains("Chistorra &amp; Pimientos"), "Especialidad con '&' escapado");
            File f = new File(tempDir, "bar.xml");
            assertTrue(bar.saveToXML(f), "saveToXML(File)");
            assertTrue(bar.saveToXML(f.getAbsolutePath()), "saveToXML(String)");
        }, db);

        // 6. Probar Pub
        testClass("Pub", () -> {
            Direccion dir = new Direccion("Pamplona", "Navarra", "Calle Olite", 20);
            Pub pub = new Pub("Pub Rockola", dir, "Musica en vivo y copas", LocalTime.of(19, 0), LocalTime.of(3, 30));
            assertXMLRepresentable(pub);
            String xml = pub.toXML();
            assertValidXML(xml, "pub");
            assertTrue(xml.contains("Pub Rockola"), "Contiene nombre");
            assertTrue(xml.contains("<horaApertura>19:00</horaApertura>") || xml.contains("19:00:00"), "Hora apertura");
            assertTrue(xml.contains("<horaClausura>03:30</horaClausura>") || xml.contains("03:30:00"), "Hora clausura");
            File f = new File(tempDir, "pub.xml");
            assertTrue(pub.saveToXML(f), "saveToXML(File)");
            assertTrue(pub.saveToXML(f.getAbsolutePath()), "saveToXML(String)");
        }, db);

        // 7. Probar Restaurante
        testClass("Restaurante", () -> {
            Direccion dir = new Direccion("Pamplona", "Navarra", "Plaza del Castillo", 1);
            Restaurante rest = new Restaurante("Restaurante Iruña", dir, "Cocina navarra de temporada", 38.5f, 100, 12);
            assertXMLRepresentable(rest);
            String xml = rest.toXML();
            assertValidXML(xml, "restaurante");
            assertTrue(xml.contains("Restaurante Iruña"), "Contiene nombre");
            assertTrue(xml.contains("<precioEstimado>38.5</precioEstimado>"), "Precio estimado");
            assertTrue(xml.contains("<capacidadMaximaTotal>100</capacidadMaximaTotal>"), "Capacidad total");
            assertTrue(xml.contains("<capacidadMaximaMesa>12</capacidadMaximaMesa>"), "Capacidad mesa");
            File f = new File(tempDir, "restaurante.xml");
            assertTrue(rest.saveToXML(f), "saveToXML(File)");
            assertTrue(rest.saveToXML(f.getAbsolutePath()), "saveToXML(String)");
        }, db);

        // 8. Probar Review
        testClass("Review", () -> {
            Cliente cli = new Cliente("maria_gomez", "pass1234", LocalDate.of(1998, 2, 14));
            Direccion dir = new Direccion("Pamplona", "Navarra", "Calle San Nicolás", 5);
            Bar bar = new Bar("Bar Txoko", dir, "Bar de pinchos");
            Review rev = new Review(cli, bar, 5, "Los mejores pinchos de tortilla!", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 11));
            assertXMLRepresentable(rev);
            String xml = rev.toXML();
            assertValidXML(xml, "review");
            assertTrue(xml.contains("<cliente>"), "Review incluye cliente embebido");
            assertTrue(xml.contains("maria_gomez"), "Contiene nick del cliente");
            assertTrue(xml.contains("<local>"), "Review incluye local embebido");
            assertTrue(xml.contains("Bar Txoko"), "Contiene nombre del local");
            assertTrue(xml.contains("<valoracion>5</valoracion>"), "Contiene valoracion");
            File f = new File(tempDir, "review.xml");
            assertTrue(rev.saveToXML(f), "saveToXML(File)");
            assertTrue(rev.saveToXML(f.getAbsolutePath()), "saveToXML(String)");
        }, db);

        // 9. Probar Contestacion
        testClass("Contestacion", () -> {
            Cliente cli = new Cliente("inaki_z", "pass9876", LocalDate.of(2001, 7, 20));
            Direccion dir = new Direccion("Tudela", "Navarra", "Herrerías", 8);
            Restaurante rest = new Restaurante("Restaurante Mejana", dir, "Verduras de la ribera", 30.0f, 50, 6);
            Review rev = new Review(cli, rest, 4, "Excelente menestra", LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 16));
            Dueño dueno = new Dueño("patxi_dueno", "duenoPass1", LocalDate.of(1975, 3, 5));
            Contestacion cont = new Contestacion(dueno, rev, "Muchas gracias por tu visita Iñaki!", LocalDate.of(2026, 9, 17));
            assertXMLRepresentable(cont);
            String xml = cont.toXML();
            assertValidXML(xml, "contestacion");
            assertTrue(xml.contains("<dueño>"), "Contestacion incluye dueño embebido");
            assertTrue(xml.contains("patxi_dueno"), "Contiene nick del dueño");
            assertTrue(xml.contains("<review>"), "Contestacion incluye review embebida");
            assertTrue(xml.contains("Excelente menestra"), "Contiene comentario de review");
            File f = new File(tempDir, "contestacion.xml");
            assertTrue(cont.saveToXML(f), "saveToXML(File)");
            assertTrue(cont.saveToXML(f.getAbsolutePath()), "saveToXML(String)");
        }, db);

        // 10. Probar Reserva
        testClass("Reserva", () -> {
            Cliente cli = new Cliente("laura_m", "lauraPass", LocalDate.of(1992, 12, 1));
            Direccion dir = new Direccion("Pamplona", "Navarra", "Paulino Caballero", 14);
            Restaurante rest = new Restaurante("Restaurante El Burladero", dir, "Carnes y asados", 42.0f, 80, 8);
            Reserva res = new Reserva(LocalDate.of(2026, 11, 20), LocalTime.of(21, 30), 10.0f, cli, rest);
            assertXMLRepresentable(res);
            String xml = res.toXML();
            assertValidXML(xml, "reserva");
            assertTrue(xml.contains("<fecha>2026-11-20</fecha>"), "Fecha de reserva");
            assertTrue(xml.contains("<descuento>10.0</descuento>"), "Descuento de reserva");
            assertTrue(xml.contains("<cliente>"), "Reserva incluye cliente embebido");
            assertTrue(xml.contains("laura_m"), "Nick del cliente de reserva");
            assertTrue(xml.contains("<local>"), "Reserva incluye local embebido");
            assertTrue(xml.contains("Restaurante El Burladero"), "Nombre del local reservable");
            File f = new File(tempDir, "reserva.xml");
            assertTrue(res.saveToXML(f), "saveToXML(File)");
            assertTrue(res.saveToXML(f.getAbsolutePath()), "saveToXML(String)");
        }, db);

        // Limpiar archivos de prueba temporales
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        tempDir.delete();

        System.out.println("===============================================================");
        System.out.printf(" RESULTADO: %d / %d PRUEBAS SUPERADAS (%.1f%%)\n", 
                passedTests, totalTests, (passedTests * 100.0) / totalTests);
        System.out.println("===============================================================");
        if (passedTests == totalTests) {
            System.out.println(" >>> TODAS LAS PRUEBAS DEL EJERCICIO 1 HAN PASADO CON ÉXITO <<<");
        } else {
            System.err.println(" >>> SE ENCONTRARON FALLOS EN LAS PRUEBAS <<<");
            System.exit(1);
        }
    }

    private static void testClass(String className, TestRunnable test, DocumentBuilder db) {
        System.out.printf("Probando clase %-15s ... ", className);
        try {
            test.run();
            System.out.println("[OK]");
        } catch (Throwable t) {
            System.out.println("[FALLO]");
            t.printStackTrace(System.out);
        }
    }

    private static void assertXMLRepresentable(Object o) {
        assertTrue(o instanceof XMLRepresentable, "El objeto debe implementar XMLRepresentable");
    }

    private static void assertValidXML(String xml, String expectedRootTag) {
        assertTrue(xml != null && !xml.trim().isEmpty(), "El XML no debe estar vacío");
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml)));
            String rootName = doc.getDocumentElement().getNodeName();
            assertTrue(rootName.equals(expectedRootTag), 
                    "La etiqueta raíz debe ser <" + expectedRootTag + "> pero fue <" + rootName + ">");
        } catch (Exception e) {
            throw new AssertionError("El XML no es un documento bien formado: " + e.getMessage(), e);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        totalTests++;
        if (condition) {
            passedTests++;
        } else {
            throw new AssertionError("Fallo en afirmación: " + message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    @FunctionalInterface
    private interface TestRunnable {
        void run() throws Throwable;
    }
}
