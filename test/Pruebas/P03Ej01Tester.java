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
 * Pruebas para comprobar el Ejercicio 1 de la Práctica 03 (implementación de
 * XMLRepresentable en las clases de GSILabs.BModel).
 *
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class P03Ej01Tester {

    private static int totalTests = 0;
    private static int passedTests = 0;

    /**
     * Constructor por defecto de P03Ej01Tester.
     */
    public P03Ej01Tester() {
    }

    /**
     * Ejecuta las pruebas de generación y guardado de XML para cada clase de BModel.
     *
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        System.out.println("=== Pruebas Práctica 03 - Ejercicio 1 (XMLRepresentable) ===");

        DocumentBuilder db = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (Exception e) {
            System.err.println("Error al inicializar DocumentBuilder: " + e.getMessage());
            return;
        }

        File tempDir = new File("test_output_xml");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // 1. Direccion
        System.out.print("Probando Direccion... ");
        Direccion dir = new Direccion("Pamplona", "Navarra", "Calle Estafeta & Mayor", 12);
        comprobar(dir instanceof XMLRepresentable, "Direccion implementa XMLRepresentable");
        String xmlDir = dir.toXML();
        comprobarXML(xmlDir, "direccion", db);
        comprobar(xmlDir.contains("Pamplona"), "Contiene localidad");
        comprobar(xmlDir.contains("&amp;"), "Escapa '&'");
        File fDir = new File(tempDir, "direccion.xml");
        comprobar(dir.saveToXML(fDir) && fDir.exists() && fDir.length() > 0, "saveToXML(File) Direccion");
        comprobar(dir.saveToXML(fDir.getAbsolutePath()), "saveToXML(String) Direccion");
        comprobar(!dir.saveToXML((File) null) && !dir.saveToXML((String) null), "saveToXML con null devuelve false");
        System.out.println("OK");

        // 2. Cliente
        System.out.print("Probando Cliente... ");
        Cliente cli = new Cliente("juan_perez", "pass123<secret>", LocalDate.of(1995, 4, 23));
        comprobar(cli instanceof XMLRepresentable, "Cliente implementa XMLRepresentable");
        String xmlCli = cli.toXML();
        comprobarXML(xmlCli, "cliente", db);
        comprobar(xmlCli.contains("juan_perez"), "Contiene nick");
        comprobar(xmlCli.contains("&lt;secret&gt;"), "Escapa caracteres en password");
        comprobar(xmlCli.contains("1995-04-23"), "Contiene fecha");
        File fCli = new File(tempDir, "cliente.xml");
        comprobar(cli.saveToXML(fCli) && cli.saveToXML(fCli.getAbsolutePath()), "saveToXML Cliente");
        System.out.println("OK");

        // 3. Dueño
        System.out.print("Probando Dueño... ");
        Dueño dueno = new Dueño("carlos_dueno", "admin\"pass", LocalDate.of(1982, 11, 10));
        comprobar(dueno instanceof XMLRepresentable, "Dueño implementa XMLRepresentable");
        String xmlDueno = dueno.toXML();
        comprobarXML(xmlDueno, "dueño", db);
        comprobar(xmlDueno.contains("carlos_dueno") && xmlDueno.contains("&quot;"), "Datos de Dueño correctos");
        File fDueno = new File(tempDir, "dueno.xml");
        comprobar(dueno.saveToXML(fDueno) && dueno.saveToXML(fDueno.getAbsolutePath()), "saveToXML Dueño");
        System.out.println("OK");

        // 4. Local
        System.out.print("Probando Local... ");
        Direccion dirLocal = new Direccion("Barañáin", "Navarra", "Avenida Central", 4);
        Local local = new Local("Local Cultural", dirLocal, "Espacio de exposiciones & talleres");
        comprobar(local instanceof XMLRepresentable, "Local implementa XMLRepresentable");
        String xmlLocal = local.toXML();
        comprobarXML(xmlLocal, "local", db);
        comprobar(xmlLocal.contains("Local Cultural") && xmlLocal.contains("<direccion>"), "Datos de Local correctos");
        File fLocal = new File(tempDir, "local.xml");
        comprobar(local.saveToXML(fLocal) && local.saveToXML(fLocal.getAbsolutePath()), "saveToXML Local");
        System.out.println("OK");

        // 5. Bar
        System.out.print("Probando Bar... ");
        Direccion dirBar = new Direccion("Pamplona", "Navarra", "San Nicolás", 15);
        Bar bar = new Bar("Bar La Mandarra", dirBar, "Pinchos y raciones tradicionales");
        bar.agregarEspecialidad("Chistorra & Pimientos");
        bar.agregarEspecialidad("Tortilla rellena");
        comprobar(bar instanceof XMLRepresentable, "Bar implementa XMLRepresentable");
        String xmlBar = bar.toXML();
        comprobarXML(xmlBar, "bar", db);
        comprobar(xmlBar.contains("<especialidades>") && xmlBar.contains("Chistorra &amp; Pimientos"), "Especialidades de Bar");
        File fBar = new File(tempDir, "bar.xml");
        comprobar(bar.saveToXML(fBar) && bar.saveToXML(fBar.getAbsolutePath()), "saveToXML Bar");
        System.out.println("OK");

        // 6. Pub
        System.out.print("Probando Pub... ");
        Direccion dirPub = new Direccion("Pamplona", "Navarra", "Calle Olite", 20);
        Pub pub = new Pub("Pub Rockola", dirPub, "Musica en vivo y copas", LocalTime.of(19, 0), LocalTime.of(3, 30));
        comprobar(pub instanceof XMLRepresentable, "Pub implementa XMLRepresentable");
        String xmlPub = pub.toXML();
        comprobarXML(xmlPub, "pub", db);
        comprobar(xmlPub.contains("19:00") && xmlPub.contains("03:30"), "Horarios de Pub");
        File fPub = new File(tempDir, "pub.xml");
        comprobar(pub.saveToXML(fPub) && pub.saveToXML(fPub.getAbsolutePath()), "saveToXML Pub");
        System.out.println("OK");

        // 7. Restaurante
        System.out.print("Probando Restaurante... ");
        Direccion dirRest = new Direccion("Pamplona", "Navarra", "Plaza del Castillo", 1);
        Restaurante rest = new Restaurante("Restaurante Iruña", dirRest, "Cocina navarra", 38.5f, 100, 12);
        comprobar(rest instanceof XMLRepresentable, "Restaurante implementa XMLRepresentable");
        String xmlRest = rest.toXML();
        comprobarXML(xmlRest, "restaurante", db);
        comprobar(xmlRest.contains("<precioEstimado>38.5</precioEstimado>")
                && xmlRest.contains("<capacidadMaximaTotal>100</capacidadMaximaTotal>")
                && xmlRest.contains("<capacidadMaximaMesa>12</capacidadMaximaMesa>"), "Campos de Restaurante");
        File fRest = new File(tempDir, "restaurante.xml");
        comprobar(rest.saveToXML(fRest) && rest.saveToXML(fRest.getAbsolutePath()), "saveToXML Restaurante");
        System.out.println("OK");

        // 8. Review
        System.out.print("Probando Review... ");
        Review rev = new Review(cli, bar, 5, "Los mejores pinchos!", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 11));
        comprobar(rev instanceof XMLRepresentable, "Review implementa XMLRepresentable");
        String xmlRev = rev.toXML();
        comprobarXML(xmlRev, "review", db);
        comprobar(xmlRev.contains("<cliente>") && xmlRev.contains("<local>") && xmlRev.contains("<valoracion>5</valoracion>"), "Campos de Review");
        File fRev = new File(tempDir, "review.xml");
        comprobar(rev.saveToXML(fRev) && rev.saveToXML(fRev.getAbsolutePath()), "saveToXML Review");
        System.out.println("OK");

        // 9. Contestacion
        System.out.print("Probando Contestacion... ");
        Contestacion cont = new Contestacion(dueno, rev, "Muchas gracias por la visita!", LocalDate.of(2026, 10, 12));
        comprobar(cont instanceof XMLRepresentable, "Contestacion implementa XMLRepresentable");
        String xmlCont = cont.toXML();
        comprobarXML(xmlCont, "contestacion", db);
        comprobar(xmlCont.contains("<dueño>") && xmlCont.contains("<review>"), "Campos de Contestacion");
        File fCont = new File(tempDir, "contestacion.xml");
        comprobar(cont.saveToXML(fCont) && cont.saveToXML(fCont.getAbsolutePath()), "saveToXML Contestacion");
        System.out.println("OK");

        // 10. Reserva
        System.out.print("Probando Reserva... ");
        Reserva res = new Reserva(LocalDate.of(2026, 11, 20), LocalTime.of(21, 30), 10.0f, cli, rest);
        comprobar(res instanceof XMLRepresentable, "Reserva implementa XMLRepresentable");
        String xmlRes = res.toXML();
        comprobarXML(xmlRes, "reserva", db);
        comprobar(xmlRes.contains("<fecha>2026-11-20</fecha>") && xmlRes.contains("<cliente>") && xmlRes.contains("<local>"), "Campos de Reserva");
        File fRes = new File(tempDir, "reserva.xml");
        comprobar(res.saveToXML(fRes) && res.saveToXML(fRes.getAbsolutePath()), "saveToXML Reserva");
        System.out.println("OK");

        // Borrar carpeta temporal
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        tempDir.delete();

        System.out.println("Resultado final: " + passedTests + " / " + totalTests + " comprobaciones superadas.");
    }

    private static void comprobarXML(String xml, String etiquetaRaiz, DocumentBuilder db) {
        comprobar(xml != null && !xml.trim().isEmpty(), "XML no vacío");
        try {
            Document doc = db.parse(new InputSource(new StringReader(xml)));
            String rootName = doc.getDocumentElement().getNodeName();
            comprobar(rootName.equals(etiquetaRaiz), "Etiqueta raíz <" + etiquetaRaiz + ">");
        } catch (Exception e) {
            throw new AssertionError("XML mal formado: " + e.getMessage(), e);
        }
    }

    private static void comprobar(boolean condicion, String mensaje) {
        totalTests++;
        if (condicion) {
            passedTests++;
        } else {
            throw new AssertionError("Fallo: " + mensaje);
        }
    }
}
