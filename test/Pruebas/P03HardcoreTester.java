package Pruebas;

import GSILabs.BModel.Bar;
import GSILabs.BModel.Cliente;
import GSILabs.BModel.Contestacion;
import GSILabs.BModel.Direccion;
import GSILabs.BModel.Dueño;
import GSILabs.BModel.Local;
import GSILabs.BModel.Pub;
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
 * Pruebas adicionales para la Práctica 03: comprobación de errores de formato,
 * orden de las etiquetas, tipos de local, parámetros nulos, caracteres
 * especiales y guardado/lectura completa de BusinessSystem.
 *
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class P03HardcoreTester {

    private static int totalPruebas = 0;
    private static int pruebasCorrectas = 0;

    /**
     * Constructor por defecto de P03HardcoreTester.
     */
    public P03HardcoreTester() {
    }

    private static void comprobar(boolean condicion, String descripcion) {
        totalPruebas++;
        if (condicion) {
            pruebasCorrectas++;
            System.out.println("  [OK] " + descripcion);
        } else {
            System.err.println("  [FALLO] " + descripcion);
            throw new AssertionError("Fallo en la prueba: " + descripcion);
        }
    }

    /**
     * Ejecuta las pruebas adicionales de la Práctica 03.
     *
     * @param args Argumentos de línea de comandos.
     * @throws IOException Si ocurre algún problema con los ficheros de prueba.
     */
    public static void main(String[] args) throws IOException {
        System.out.println("=== Pruebas adicionales Práctica 03 (XML) ===");

        File dirTemporal = new File("temp_test_xml");
        if (!dirTemporal.exists()) {
            dirTemporal.mkdirs();
        }

        // 1. XML mal formado (etiquetas sin cerrar)
        System.out.println("\n1. Comprobación de XML mal formado:");
        String xmlSinCerrar = "<bar><nombre>Bar Central</nombre><direccion><localidad>Pamplona</bar>";
        try {
            XMLParser.parseBar(xmlSinCerrar);
            comprobar(false, "Debería fallar si el XML tiene etiquetas mal cerradas");
        } catch (XMLParsingException e) {
            comprobar(true, "Se detecta XML mal formado: " + e.getMessage());
        }

        // 2. Orden distinto en las etiquetas hijas
        System.out.println("\n2. Independencia del orden de las etiquetas:");
        String dirDesordenada = "<direccion>\n"
                + "  <numero>42</numero>\n"
                + "  <calle>Avenida de Navarra</calle>\n"
                + "  <provincia>Navarra</provincia>\n"
                + "  <localidad>Pamplona</localidad>\n"
                + "</direccion>";
        try {
            Direccion d = XMLParser.parseDireccion(dirDesordenada);
            comprobar(d.getNumero() == 42 && "Pamplona".equals(d.getLocalidad()) && "Avenida de Navarra".equals(d.getCalle()),
                    "Direccion leída cambiando el orden de las etiquetas");
        } catch (XMLParsingException e) {
            comprobar(false, "Error al leer Direccion con otro orden: " + e.getMessage());
        }

        String pubDesordenado = "<pub>\n"
                + "  <horaClausura>04:00</horaClausura>\n"
                + "  <descripcion>Copas y música</descripcion>\n"
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
            comprobar("Pub Central".equals(p.getNombre()) && p.getHoraClausura().equals(LocalTime.of(4, 0)),
                    "Pub leído con etiquetas desordenadas");
        } catch (XMLParsingException e) {
            comprobar(false, "Error al leer Pub desordenado: " + e.getMessage());
        }

        // 3. Comprobación del tipo de etiqueta raíz
        System.out.println("\n3. Comprobación de etiqueta raíz y tipos:");
        String restauranteXML = "<restaurante>\n"
                + "  <nombre>Restaurante Italiano</nombre>\n"
                + "  <direccion><localidad>Pamplona</localidad><provincia>Navarra</provincia><calle>Mayor</calle><numero>1</numero></direccion>\n"
                + "  <descripcion>Pasta fresca</descripcion>\n"
                + "  <precioEstimado>25.5</precioEstimado>\n"
                + "  <capacidadMaximaTotal>60</capacidadMaximaTotal>\n"
                + "  <capacidadMaximaMesa>6</capacidadMaximaMesa>\n"
                + "</restaurante>";
        try {
            XMLParser.parseBar(restauranteXML);
            comprobar(false, "parseBar no debe aceptar un <restaurante>");
        } catch (XMLParsingException e) {
            comprobar(true, "parseBar rechaza un XML con etiqueta <restaurante>");
        }

        String clienteXML = "<cliente><nick>user1</nick><password>pass123</password><fechaNacimiento>1990-01-01</fechaNacimiento></cliente>";
        try {
            XMLParser.parseDueño(clienteXML);
            comprobar(false, "parseDueño no debe aceptar un <cliente>");
        } catch (XMLParsingException e) {
            comprobar(true, "parseDueño rechaza un XML con etiqueta <cliente>");
        }

        String reservaConPub = "<reserva>\n"
                + "  <fecha>2026-12-01</fecha>\n"
                + "  <hora>21:00</hora>\n"
                + "  <descuento>5.0</descuento>\n"
                + "  <cliente><nick>usuario1</nick><password>pass123</password><fechaNacimiento>2000-01-01</fechaNacimiento></cliente>\n"
                + "  <local><pub><nombre>P1</nombre><direccion><localidad>L</localidad><provincia>P</provincia><calle>C</calle><numero>1</numero></direccion><descripcion>d</descripcion><horaApertura>18:00</horaApertura><horaClausura>02:00</horaClausura></pub></local>\n"
                + "</reserva>";
        try {
            XMLParser.parseReserva(reservaConPub);
            comprobar(false, "parseReserva no debe permitir reservar un Pub");
        } catch (XMLParsingException e) {
            comprobar(true, "parseReserva rechaza un local que no es Reservable");
        }

        // 4. Parámetros nulos y ficheros que no existen
        System.out.println("\n4. Parámetros nulos y ficheros inexistentes:");
        try {
            XMLParser.parseCliente((String) null);
            comprobar(false, "parseCliente(null) debe lanzar excepción");
        } catch (XMLParsingException e) {
            comprobar(true, "parseCliente((String) null) lanza XMLParsingException");
        }
        try {
            XMLParser.parseDireccion("   ");
            comprobar(false, "parseDireccion vacío debe lanzar excepción");
        } catch (XMLParsingException e) {
            comprobar(true, "parseDireccion con cadena vacía lanza XMLParsingException");
        }
        try {
            XMLParser.parseBar((File) null);
            comprobar(false, "parseBar((File) null) debe lanzar excepción");
        } catch (XMLParsingException e) {
            comprobar(true, "parseBar((File) null) lanza XMLParsingException");
        }
        try {
            XMLParser.parseBar(new File("no_existe.xml"));
            comprobar(false, "parseBar con fichero inexistente debe lanzar excepción");
        } catch (XMLParsingException e) {
            comprobar(true, "parseBar con fichero inexistente lanza XMLParsingException");
        }
        try {
            BusinessSystem.parseXMLFile(null);
            comprobar(false, "BusinessSystem.parseXMLFile(null) debe lanzar excepción");
        } catch (XMLParsingException e) {
            comprobar(true, "BusinessSystem.parseXMLFile(null) lanza XMLParsingException");
        }
        comprobar(!new BusinessSystem().loadXMLFile(null), "loadXMLFile(null) devuelve false");

        // 5. Caracteres especiales en XML
        System.out.println("\n5. Caracteres especiales (&, <, >, comillas):");
        Direccion dirEspecial = new Direccion("Barañáin & Cizur", "Navarra", "Calle <Principal> \"Alta\" 'Norte'", 7);
        Bar barEspecial = new Bar("Bar & Tapas \"El Ñandú\"", dirEspecial, "Descripción con <etiquetas> y 'comillas' & símbolos");
        barEspecial.agregarEspecialidad("Pinchos de tortilla & jamón");
        barEspecial.agregarEspecialidad("<Pimientos del Padrón>");

        File fBar = new File(dirTemporal, "bar_especial.xml");
        comprobar(barEspecial.saveToXML(fBar), "Guardado de Bar con caracteres especiales");

        try {
            Bar barLeido = XMLParser.parseBar(fBar);
            comprobar(barLeido.getNombre().equals(barEspecial.getNombre()), "Nombre con caracteres especiales conservado");
            comprobar(barLeido.getDescripcion().equals(barEspecial.getDescripcion()), "Descripción con caracteres especiales conservada");
            comprobar(barLeido.getDireccion().getCalle().equals(dirEspecial.getCalle()), "Calle con caracteres especiales conservada");
        } catch (XMLParsingException e) {
            comprobar(false, "Error al leer Bar con caracteres especiales: " + e.getMessage());
        }

        // 6. Guardado y lectura de un BusinessSystem completo
        System.out.println("\n6. Guardado y carga de BusinessSystem completo:");
        BusinessSystem bs = new BusinessSystem();

        Cliente cli1 = new Cliente("marta_g", "pass123", LocalDate.of(1996, 7, 15));
        Cliente cli2 = new Cliente("pedro_r", "pass456", LocalDate.of(2001, 11, 2));
        Dueño due1 = new Dueño("carmen_d", "duenoPass1", LocalDate.of(1975, 2, 20));
        Dueño due2 = new Dueño("javier_d", "duenoPass2", LocalDate.of(1982, 8, 30));

        bs.nuevoUsuario(cli1);
        bs.nuevoUsuario(cli2);
        bs.nuevoUsuario(due1);
        bs.nuevoUsuario(due2);

        Direccion dBar = new Direccion("Pamplona", "Navarra", "San Nicolás", 14);
        Bar bar1 = new Bar("Bar Iruña", dBar, "Especialidad en fritos");
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

        bs.asociarLocal(bar1, due1);
        bs.asociarLocal(bar1, due2);
        bs.asociarLocal(rest1, due1);

        Review rev1 = new Review(cli1, bar1, 5, "Muy buena calidad", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2));
        Review rev2 = new Review(cli2, rest1, 4, "Muy rico todo", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 6));
        bs.nuevaReview(rev1);
        bs.nuevaReview(rev2);

        Contestacion cont1 = new Contestacion(due1, rev1, "Muchas gracias Marta", LocalDate.of(2026, 10, 3));
        bs.nuevaContestacion(cont1, rev1);

        bs.nuevaReserva(cli1, bar1, LocalDate.now().plusDays(5), LocalTime.of(14, 0));
        bs.nuevaReserva(cli2, rest1, LocalDate.now().plusDays(10), LocalTime.of(21, 30));

        File sysFile = new File(dirTemporal, "sistema_completo.xml");
        comprobar(bs.saveToXML(sysFile), "saveToXML del sistema completo");

        try {
            BusinessSystem bsCopia = BusinessSystem.parseXMLFile(sysFile);
            comprobar(bsCopia.obtenerTodosLosLocales().length == 4, "Se recuperan los 4 locales");
            comprobar(bsCopia.obtenerUsuario("marta_g") instanceof Cliente, "Cliente recuperado correctamente");
            comprobar(bsCopia.obtenerUsuario("carmen_d") instanceof Dueño, "Dueño recuperado correctamente");
            comprobar(bsCopia.obtenerDueñosDeLocal(bar1).length == 2, "El bar mantiene sus 2 dueños");
            comprobar(bsCopia.verReviews(bar1).length == 1, "Review del bar recuperada");
            Review revRecuperada = bsCopia.verReviews(bar1)[0];
            comprobar(bsCopia.tieneContestacion(revRecuperada), "Contestación recuperada");
            comprobar(bsCopia.obtenerReservas(cli1).length == 1, "Reserva del cliente recuperada");
            comprobar(revRecuperada.getCliente() == bsCopia.obtenerUsuario("marta_g"),
                    "La review apunta al mismo objeto Cliente de la lista de usuarios");
            comprobar(revRecuperada.getLocal() == bsCopia.obtenerLocal(dBar),
                    "La review apunta al mismo objeto Local de la lista de locales");
            comprobar(bsCopia.obtenerContestacion(revRecuperada).getReview() == revRecuperada,
                    "La contestación apunta a la misma instancia de Review del sistema");
        } catch (XMLParsingException e) {
            comprobar(false, "Error al leer BusinessSystem completo: " + e.getMessage());
        }

        // 7. Comprobar que si loadXMLFile falla, se mantienen los datos que había antes
        System.out.println("\n7. Conservación de datos si falla loadXMLFile:");
        BusinessSystem sistemaActual = new BusinessSystem();
        sistemaActual.loadXMLFile(sysFile);
        int localesAntes = sistemaActual.obtenerTodosLosLocales().length;

        File archivoInvalido = new File(dirTemporal, "incompleto.xml");
        Files.write(archivoInvalido.toPath(), "<businessSystem><usuarios><cliente><nick>solo</nick></businessSystem>".getBytes(StandardCharsets.UTF_8));

        boolean cargado = sistemaActual.loadXMLFile(archivoInvalido);
        comprobar(!cargado, "loadXMLFile devuelve false con un fichero incompleto");
        comprobar(sistemaActual.obtenerTodosLosLocales().length == localesAntes,
                "Se mantienen los datos anteriores tras fallar la carga");

        // Borrar archivos temporales
        File[] archivos = dirTemporal.listFiles();
        if (archivos != null) {
            for (File f : archivos) {
                f.delete();
            }
        }
        dirTemporal.delete();

        System.out.println("\nPruebas finalizadas: " + pruebasCorrectas + " / " + totalPruebas + " correctas.");
    }
}
