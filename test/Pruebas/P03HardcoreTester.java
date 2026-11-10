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
import GSILabs.BSystem.BusinessSystem;
import GSILabs.persistence.XMLParser;
import GSILabs.persistence.XMLParsingException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Suite de pruebas de estrés y robustez extrema para la Práctica 03.
 * Verifica seguridad (XXE), tolerancia a orden aleatorio de etiquetas,
 * validación estricta de tipos de entidad, caracteres especiales,
 * valores límite, nulidad e integridad transaccional (atomicidad).
 *
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class P03HardcoreTester {

    private static int totalAssertions = 0;
    private static int passedAssertions = 0;

    private static void assertTrue(boolean condition, String message) {
        totalAssertions++;
        if (condition) {
            passedAssertions++;
            System.out.println("  [PASS] " + message);
        } else {
            System.err.println("  [FAIL] " + message);
            throw new AssertionError("Fallo en verificación de robustez: " + message);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("===============================================================================");
        System.out.println(" BATERÍA DE PRUEBAS DE ESTRÉS Y MÁXIMA ROBUSTEZ - PRÁCTICA 03 (XML)");
        System.out.println("===============================================================================");

        File scratchDir = new File("scratch_test_xml");
        if (!scratchDir.exists()) {
            scratchDir.mkdirs();
        }

        // -------------------------------------------------------------------------
        // 1. SEGURIDAD: Protección contra XXE y Billion Laughs (Doctype / Entities)
        // -------------------------------------------------------------------------
        System.out.println("\n--- [1] PRUEBAS DE SEGURIDAD XML (XXE & ENTITY INJECTION) ---");
        String xxePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE bar [\n"
                + "  <!ELEMENT bar ANY >\n"
                + "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]>\n"
                + "<bar><nombre>&xxe;</nombre><direccion><localidad>P</localidad><provincia>N</provincia><calle>C</calle><numero>1</numero></direccion><descripcion>d</descripcion></bar>";
        try {
            XMLParser.parseBar(xxePayload);
            assertTrue(false, "El parser no debe procesar documentos con DOCTYPE/XXE malicioso");
        } catch (XMLParsingException e) {
            assertTrue(true, "Ataque XXE neutralizado correctamente con XMLParsingException: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // 2. PARSING INDEPENDIENTE DEL ORDEN DE ETIQUETAS
        // -------------------------------------------------------------------------
        System.out.println("\n--- [2] PRUEBAS DE INDEPENDENCIA DE ORDEN DE ETIQUETAS ---");
        // Direccion con orden invertido de tags
        String dirDesordenada = "<direccion>\n"
                + "  <numero>42</numero>\n"
                + "  <calle>Avenida de Navarra</calle>\n"
                + "  <provincia>Navarra</provincia>\n"
                + "  <localidad>Pamplona</localidad>\n"
                + "</direccion>";
        try {
            Direccion d = XMLParser.parseDireccion(dirDesordenada);
            assertTrue(d.getNumero() == 42 && "Pamplona".equals(d.getLocalidad()) && "Avenida de Navarra".equals(d.getCalle()),
                    "Direccion parseada correctamente con orden de etiquetas totalmente invertido");
        } catch (XMLParsingException e) {
            assertTrue(false, "Fallo al parsear Direccion con etiquetas desordenadas: " + e.getMessage());
        }

        // Pub con orden desordenado y espacios extra
        String pubDesordenado = "<pub>\n"
                + "  <horaClausura>04:00</horaClausura>\n"
                + "  <descripcion>Copas y buena música</descripcion>\n"
                + "  <direccion>\n"
                + "     <numero>10</numero>\n"
                + "     <calle>Calle Estafeta</calle>\n"
                + "     <provincia>Navarra</provincia>\n"
                + "     <localidad>Pamplona</localidad>\n"
                + "  </direccion>\n"
                + "  <horaApertura>20:00</horaApertura>\n"
                + "  <nombre>Pub Central</nombre>\n"
                + "</pub>";
        try {
            Pub p = XMLParser.parsePub(pubDesordenado);
            assertTrue("Pub Central".equals(p.getNombre()) && p.getHoraClausura().equals(LocalTime.of(4, 0)),
                    "Pub parseado correctamente con orden aleatorio de hijos");
        } catch (XMLParsingException e) {
            assertTrue(false, "Fallo al parsear Pub desordenado: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // 3. VALIDACIÓN ESTRICTA DE TIPOS Y ETIQUETA RAÍZ
        // -------------------------------------------------------------------------
        System.out.println("\n--- [3] PRUEBAS DE VALIDACIÓN ESTRICTA DE TIPO (CROSS-TYPE ATTACKS) ---");
        // Pasar XML de Restaurante a parseBar
        String restauranteXML = "<restaurante>\n"
                + "  <nombre>Restaurante Italiano</nombre>\n"
                + "  <direccion><localidad>P</localidad><provincia>N</provincia><calle>C</calle><numero>1</numero></direccion>\n"
                + "  <descripcion>Pasta fresca</descripcion>\n"
                + "  <precioEstimado>25.5</precioEstimado>\n"
                + "  <capacidadMaximaTotal>60</capacidadMaximaTotal>\n"
                + "  <capacidadMaximaMesa>6</capacidadMaximaMesa>\n"
                + "</restaurante>";
        try {
            XMLParser.parseBar(restauranteXML);
            assertTrue(false, "parseBar NO debe aceptar un elemento raíz <restaurante>");
        } catch (XMLParsingException e) {
            assertTrue(true, "parseBar rechazó correctamente un XML con raíz <restaurante>");
        }

        // Pasar XML de Cliente a parseDueño
        String clienteXML = "<cliente><nick>user1</nick><password>pass123</password><fechaNacimiento>1990-01-01</fechaNacimiento></cliente>";
        try {
            XMLParser.parseDueño(clienteXML);
            assertTrue(false, "parseDueño NO debe aceptar un <cliente>");
        } catch (XMLParsingException e) {
            assertTrue(true, "parseDueño rechazó correctamente un XML con raíz <cliente>");
        }

        // Reserva donde el local NO es reservable (un Pub)
        String reservaInvalidaLocal = "<reserva>\n"
                + "  <fecha>2026-12-01</fecha>\n"
                + "  <hora>21:00</hora>\n"
                + "  <descuento>5.0</descuento>\n"
                + "  <cliente><nick>usuarioValido</nick><password>passValido123</password><fechaNacimiento>2000-01-01</fechaNacimiento></cliente>\n"
                + "  <local><pub><nombre>P1</nombre><direccion><localidad>L</localidad><provincia>P</provincia><calle>C</calle><numero>1</numero></direccion><descripcion>d</descripcion><horaApertura>18:00</horaApertura><horaClausura>02:00</horaClausura></pub></local>\n"
                + "</reserva>";
        try {
            XMLParser.parseReserva(reservaInvalidaLocal);
            assertTrue(false, "parseReserva NO debe aceptar locales no reservables como un Pub");
        } catch (XMLParsingException e) {
            assertTrue(true, "parseReserva rechazó con éxito una reserva con local no reservable: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // 4. ROBUSTEZ FRENTE A NULOS, CADENAS VACÍAS Y FICHEROS INEXISTENTES
        // -------------------------------------------------------------------------
        System.out.println("\n--- [4] CONTROL DE PARÁMETROS NULOS Y VALORES LÍMITE ---");
        // Null / vacío en String
        try {
            XMLParser.parseCliente((String) null);
            assertTrue(false, "parseCliente(null) debe lanzar XMLParsingException");
        } catch (XMLParsingException e) {
            assertTrue(true, "parseCliente((String) null) capturado y controlado");
        }
        try {
            XMLParser.parseDireccion("   ");
            assertTrue(false, "parseDireccion(\"   \") debe lanzar XMLParsingException");
        } catch (XMLParsingException e) {
            assertTrue(true, "parseDireccion(\"   \") controlado correctamente");
        }

        // Null / inexistente en File
        try {
            XMLParser.parseBar((File) null);
            assertTrue(false, "parseBar((File) null) debe lanzar XMLParsingException");
        } catch (XMLParsingException e) {
            assertTrue(true, "parseBar((File) null) controlado");
        }
        try {
            XMLParser.parseBar(new File("archivo_fantasma_que_no_existe_9999.xml"));
            assertTrue(false, "parseBar(ficheroInexistente) debe lanzar XMLParsingException");
        } catch (XMLParsingException e) {
            assertTrue(true, "parseBar(ficheroInexistente) controlado");
        }

        // Null en BusinessSystem
        try {
            BusinessSystem.parseXMLFile(null);
            assertTrue(false, "BusinessSystem.parseXMLFile(null) debe lanzar XMLParsingException");
        } catch (XMLParsingException e) {
            assertTrue(true, "BusinessSystem.parseXMLFile(null) controlado");
        }
        assertTrue(!new BusinessSystem().loadXMLFile(null), "loadXMLFile(null) debe devolver false sin lanzar excepción");

        // -------------------------------------------------------------------------
        // 5. CARACTERES ESPECIALES Y ESCAPADO COMPLETO
        // -------------------------------------------------------------------------
        System.out.println("\n--- [5] PRUEBAS DE CARACTERES ESPECIALES (XML ENTITIES Y UNICODE) ---");
        Direccion dirEspecial = new Direccion("Barañáin & Cizur", "Navarra", "Calle <Principal> \"Alta\" 'Norte'", 7);
        Bar barEspecial = new Bar("Bar & Tapas \"El Ñandú\"", dirEspecial, "Descripción con <etiquetas> y 'comillas' & símbolos");
        barEspecial.agregarEspecialidad("Pinchos de tortilla & jamón");
        barEspecial.agregarEspecialidad("<Pimientos del Padrón>");

        File fBar = new File(scratchDir, "bar_especial.xml");
        assertTrue(barEspecial.saveToXML(fBar), "saveToXML con caracteres especiales en nombre, direccion y especialidades");

        try {
            Bar barRecuperado = XMLParser.parseBar(fBar);
            assertTrue(barRecuperado.getNombre().equals(barEspecial.getNombre()), "Nombre con & y comillas restaurado idénticamente");
            assertTrue(barRecuperado.getDescripcion().equals(barEspecial.getDescripcion()), "Descripción con tags ficticios recuperada intacta");
            assertTrue(barRecuperado.getDireccion().getCalle().equals(dirEspecial.getCalle()), "Calle con comillas y ángulos recuperada con fidelidad");
        } catch (XMLParsingException e) {
            assertTrue(false, "Fallo al recuperar Bar con caracteres especiales: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // 6. ROUND-TRIP COMPLETO DE BUSINESS SYSTEM CON TODAS LAS ENTIDADES
        // -------------------------------------------------------------------------
        System.out.println("\n--- [6] ROUND-TRIP EXHAUSTIVO DEL SISTEMA COMPLETO ---");
        BusinessSystem bs = new BusinessSystem();

        Cliente cli1 = new Cliente("marta_g", "pass#123", LocalDate.of(1996, 7, 15));
        Cliente cli2 = new Cliente("pedro_r", "pass#456", LocalDate.of(2001, 11, 2));
        Dueño due1 = new Dueño("carmen_d", "dueñoPass1", LocalDate.of(1975, 2, 20));
        Dueño due2 = new Dueño("javier_d", "dueñoPass2", LocalDate.of(1982, 8, 30));

        bs.nuevoUsuario(cli1);
        bs.nuevoUsuario(cli2);
        bs.nuevoUsuario(due1);
        bs.nuevoUsuario(due2);

        Direccion dBar = new Direccion("Pamplona", "Navarra", "San Nicolás", 14);
        Bar bar1 = new Bar("Bar Iruña Pinta", dBar, "Especialidad en fritos");
        bar1.agregarEspecialidad("Frito de pimiento");
        bar1.agregarEspecialidad("Croqueta de jamón");

        Direccion dPub = new Direccion("Pamplona", "Navarra", "Calle Mayor", 33);
        Pub pub1 = new Pub("Pub La Roca", dPub, "Pub nocturno", LocalTime.of(22, 0), LocalTime.of(4, 30));

        Direccion dRest = new Direccion("Tudela", "Navarra", "Plaza Foral", 5);
        Restaurante rest1 = new Restaurante("Restaurante Mejana", dRest, "Cocina de huerta", 35.0f, 90, 8);

        Direccion dLoc = new Direccion("Estella", "Navarra", "Calle Curtidores", 1);
        Local loc1 = new Local("Local de Eventos", dLoc, "Espacio polivalente");

        bs.nuevoLocal(bar1);
        bs.nuevoLocal(pub1);
        bs.nuevoLocal(rest1);
        bs.nuevoLocal(loc1);

        // Asociaciones de dueños
        bs.asociarLocal(bar1, due1);
        bs.asociarLocal(bar1, due2);
        bs.asociarLocal(rest1, due1);

        // Reviews y Contestaciones
        Review rev1 = new Review(cli1, bar1, 5, "Insuperable calidad", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2));
        Review rev2 = new Review(cli2, rest1, 4, "Muy rico todo", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 6));
        bs.nuevaReview(rev1);
        bs.nuevaReview(rev2);

        Contestacion cont1 = new Contestacion(due1, rev1, "¡Muchísimas gracias Marta!", LocalDate.of(2026, 10, 3));
        bs.nuevaContestacion(cont1, rev1);

        // Reservas (en Bar y en Restaurante)
        bs.nuevaReserva(cli1, bar1, LocalDate.now().plusDays(5), LocalTime.of(14, 0));
        bs.nuevaReserva(cli2, rest1, LocalDate.now().plusDays(10), LocalTime.of(21, 30));

        File sysFile = new File(scratchDir, "full_system_test.xml");
        assertTrue(bs.saveToXML(sysFile), "saveToXML del sistema completo con todas las clases instanciables");

        try {
            BusinessSystem bsCopia = BusinessSystem.parseXMLFile(sysFile);
            assertTrue(bsCopia.obtenerTodosLosLocales().length == 4, "Los 4 locales fueron restaurados");
            assertTrue(bsCopia.obtenerUsuario("marta_g") instanceof Cliente, "Cliente recuperado con tipo exacto");
            assertTrue(bsCopia.obtenerUsuario("carmen_d") instanceof Dueño, "Dueño recuperado con tipo exacto");
            assertTrue(bsCopia.obtenerDueñosDeLocal(bar1).length == 2, "La cotitularidad (2 dueños) se restauró con éxito");
            assertTrue(bsCopia.verReviews(bar1).length == 1, "Review del bar recuperada");
            assertTrue(bsCopia.tieneContestacion(bsCopia.verReviews(bar1)[0]), "Contestación enlazada correctamente");
            assertTrue(bsCopia.obtenerReservas(cli1).length == 1, "Reserva del cliente recuperada");
        } catch (XMLParsingException e) {
            assertTrue(false, "Fallo inesperado al deserializar BusinessSystem completo: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // 7. ATOMICIDAD EN LOADXMLFILE TRAS INTENTO DE CARGA CORRUPTA
        // -------------------------------------------------------------------------
        System.out.println("\n--- [7] COMPROBACIÓN DE ATOMICIDAD (INTEGRIDAD TRAS ERROR) ---");
        BusinessSystem estadoInicial = new BusinessSystem();
        estadoInicial.loadXMLFile(sysFile);
        int totalLocalesAntes = estadoInicial.obtenerTodosLosLocales().length;

        File archivoCorrupto = new File(scratchDir, "corrupto.xml");
        Files.write(archivoCorrupto.toPath(), "<businessSystem><usuarios><cliente><nick>solo</nick></businessSystem>".getBytes(StandardCharsets.UTF_8));

        boolean cargaExitosa = estadoInicial.loadXMLFile(archivoCorrupto);
        assertTrue(!cargaExitosa, "loadXMLFile devolvió false ante un fichero truncado/corrupto");
        assertTrue(estadoInicial.obtenerTodosLosLocales().length == totalLocalesAntes,
                "Atomicidad verificada: los datos previos se conservan íntegros tras un fallo de carga");

        // Limpiar archivos de prueba
        File[] scratchFiles = scratchDir.listFiles();
        if (scratchFiles != null) {
            for (File sf : scratchFiles) {
                sf.delete();
            }
        }
        scratchDir.delete();

        System.out.println("\n===============================================================================");
        System.out.printf(" RESULTADO: %d / %d AFIRMACIONES SUPERADAS CON ÉXITO (%.1f%%)\n",
                passedAssertions, totalAssertions, (passedAssertions * 100.0) / totalAssertions);
        System.out.println("===============================================================================");
        if (passedAssertions == totalAssertions) {
            System.out.println(" >>> SISTEMA 100% ROBUSTO: SUPERA TODAS LAS PRUEBAS DE MÁXIMA EXIGENCIA <<<");
        } else {
            System.err.println(" >>> FALLOS ENCONTRADOS EN LAS PRUEBAS DE ROBUSTEZ <<<");
            System.exit(1);
        }
    }
}
