package GSILabs.persistence;

import GSILabs.BModel.*;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Utilidad de lectura (parsing) de objetos del paquete GSILabs.BModel a partir
 * de su representación XML. Para cada clase instanciable ofrece dos métodos
 * estáticos: uno que lee desde un String y otro que lo hace desde un File.
 * <p>
 * Los datos de cada objeto se leen por nombre de etiqueta (hijos directos),
 * por lo que el orden de los elementos dentro del XML no importa. Cualquier
 * XML mal formado, incompleto o con valores inválidos provoca una
 * {@link XMLParsingException}.
 *
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class XMLParser {
    
    /**
     * Constructor privado: clase de utilidad que solo ofrece métodos estáticos.
     */
    private XMLParser() {
    }

    /**
     * Construye el árbol DOM de un documento XML a partir de un texto.
     *
     * @param xml Contenido XML en forma de cadena.
     * @return El documento DOM resultante.
     * @throws Exception Si el XML está mal formado o falla la configuración del parser.
     */
    private static Document getDocumentFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    /**
     * Lee el contenido completo de un fichero de texto codificado en UTF-8.
     *
     * @param f Fichero a leer.
     * @return El contenido del fichero como cadena.
     * @throws Exception Si el fichero no existe o no se puede leer.
     */
    private static String readFileToString(File f) throws Exception {
        return new String(Files.readAllBytes(f.toPath()), "UTF-8");
    }

    /**
     * Obtiene el texto del primer hijo directo con la etiqueta indicada.
     *
     * @param tag     Nombre de la etiqueta buscada.
     * @param element Elemento padre.
     * @return El texto sin espacios sobrantes, o cadena vacía si no existe la etiqueta.
     */
    public static String getTagValue(String tag, Element element) {
        Element child = getFirstChildElement(tag, element);
        return child != null ? child.getTextContent().trim() : "";
    }

    /**
     * Convierte un nodo DOM en su representación textual XML (sin cabecera).
     *
     * @param node Nodo a convertir.
     * @return El XML del nodo como String.
     * @throws Exception Si falla la transformación.
     */
    public static String nodeToString(Node node) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

    /**
     * Busca el primer hijo DIRECTO del elemento con la etiqueta indicada.
     * Solo mira hijos directos (no descendientes) para no confundir etiquetas
     * repetidas en objetos anidados (p. ej. el comentario de una contestación
     * y el de la review que lleva dentro).
     *
     * @param tag    Nombre de la etiqueta buscada.
     * @param parent Elemento padre.
     * @return El primer hijo con esa etiqueta, o null si no existe.
     */
    public static Element getFirstChildElement(String tag, Element parent) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element && ((Element) n).getTagName().equals(tag)) {
                return (Element) n;
            }
        }
        return null;
    }

    /**
     * Instancia el tipo concreto de Local (Restaurante, Bar, Pub o Local genérico)
     * a partir del envoltorio {@code <local>} que aparece dentro de una Review o
     * una Reserva. El tipo lo indica la etiqueta hija del envoltorio.
     *
     * @param wrapper Elemento {@code <local>} que contiene el local concreto.
     * @return El local parseado, conservando su clase original.
     * @throws Exception Si el envoltorio es nulo, no contiene ningún tipo de local
     *                   reconocido o el local interior es inválido.
     */
    private static Local parseLocalPolimorfico(Element wrapper) throws Exception {
        if (wrapper == null) throw new XMLParsingException("Elemento wrapper del local nulo");

        if (getFirstChildElement("restaurante", wrapper) != null) {
            return parseRestaurante(nodeToString(getFirstChildElement("restaurante", wrapper)));
        } else if (getFirstChildElement("bar", wrapper) != null) {
            return parseBar(nodeToString(getFirstChildElement("bar", wrapper)));
        } else if (getFirstChildElement("pub", wrapper) != null) {
            return parsePub(nodeToString(getFirstChildElement("pub", wrapper)));
        } else if (getFirstChildElement("local", wrapper) != null) {
            return parseLocal(nodeToString(getFirstChildElement("local", wrapper)));
        }
        throw new XMLParsingException("No se encontró una etiqueta de tipo de local válida dentro del wrapper.");
    }

    /**
     * Crea una Direccion a partir de su representación XML.
     *
     * @param str XML con los elementos localidad, provincia, calle y numero.
     * @return La dirección parseada.
     * @throws XMLParsingException Si el XML está mal formado, falta algún dato
     *                             o el número no es un entero válido.
     */
    public static Direccion parseDireccion(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            return new Direccion(
                getTagValue("localidad", root),
                getTagValue("provincia", root),
                getTagValue("calle", root),
                Integer.parseInt(getTagValue("numero", root))
            );
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Direccion", e);
        }
    }

    /**
     * Crea una Direccion a partir de un fichero XML.
     *
     * @param f Fichero que contiene el XML de la dirección.
     * @return La dirección parseada.
     * @throws XMLParsingException Si el fichero no se puede leer o su contenido es inválido.
     */
    public static Direccion parseDireccion(File f) throws XMLParsingException {
        try { return parseDireccion(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Direccion", e); }
    }

    /**
     * Crea un Cliente a partir de su representación XML.
     *
     * @param str XML con los elementos nick, password y fechaNacimiento.
     * @return El cliente parseado.
     * @throws XMLParsingException Si el XML está mal formado, falta algún dato
     *                             o la fecha no tiene formato válido.
     */
    public static Cliente parseCliente(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            return new Cliente(
                getTagValue("nick", root),
                getTagValue("password", root),
                LocalDate.parse(getTagValue("fechaNacimiento", root))
            );
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Cliente", e);
        }
    }

    /**
     * Crea un Cliente a partir de un fichero XML.
     *
     * @param f Fichero que contiene el XML del cliente.
     * @return El cliente parseado.
     * @throws XMLParsingException Si el fichero no se puede leer o su contenido es inválido.
     */
    public static Cliente parseCliente(File f) throws XMLParsingException {
        try { return parseCliente(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Cliente", e); }
    }

    /**
     * Crea un Dueño a partir de su representación XML.
     *
     * @param str XML con los elementos nick, password y fechaNacimiento.
     * @return El dueño parseado.
     * @throws XMLParsingException Si el XML está mal formado, falta algún dato
     *                             o la fecha no tiene formato válido.
     */
    public static Dueño parseDueño(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            return new Dueño(
                getTagValue("nick", root),
                getTagValue("password", root),
                LocalDate.parse(getTagValue("fechaNacimiento", root))
            );
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Dueño", e);
        }
    }

    /**
     * Crea un Dueño a partir de un fichero XML.
     *
     * @param f Fichero que contiene el XML del dueño.
     * @return El dueño parseado.
     * @throws XMLParsingException Si el fichero no se puede leer o su contenido es inválido.
     */
    public static Dueño parseDueño(File f) throws XMLParsingException {
        try { return parseDueño(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Dueño", e); }
    }

    /**
     * Crea un Local genérico a partir de su representación XML.
     *
     * @param str XML con los elementos nombre, direccion (anidada) y descripcion.
     * @return El local parseado.
     * @throws XMLParsingException Si el XML está mal formado, falta algún dato
     *                             o la dirección es inválida.
     */
    public static Local parseLocal(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            Direccion dir = parseDireccion(nodeToString(getFirstChildElement("direccion", root)));
            return new Local(getTagValue("nombre", root), dir, getTagValue("descripcion", root));
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Local", e);
        }
    }

    /**
     * Crea un Local genérico a partir de un fichero XML.
     *
     * @param f Fichero que contiene el XML del local.
     * @return El local parseado.
     * @throws XMLParsingException Si el fichero no se puede leer o su contenido es inválido.
     */
    public static Local parseLocal(File f) throws XMLParsingException {
        try { return parseLocal(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Local", e); }
    }

    /**
     * Crea un Bar a partir de su representación XML, incluyendo su lista de
     * especialidades.
     *
     * @param str XML con los elementos nombre, direccion (anidada), descripcion
     *            y especialidades (con varios elementos especialidad).
     * @return El bar parseado.
     * @throws XMLParsingException Si el XML está mal formado, falta algún dato
     *                             o la dirección es inválida.
     */
    public static Bar parseBar(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            Direccion dir = parseDireccion(nodeToString(getFirstChildElement("direccion", root)));
            Bar bar = new Bar(getTagValue("nombre", root), dir, getTagValue("descripcion", root));

            // Las especialidades son un campo multivalorado: se añaden una a una
            NodeList espNodes = root.getElementsByTagName("especialidad");
            for (int i = 0; i < espNodes.getLength(); i++) {
                bar.agregarEspecialidad(espNodes.item(i).getTextContent().trim());
            }
            return bar;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Bar", e);
        }
    }

    /**
     * Crea un Bar a partir de un fichero XML.
     *
     * @param f Fichero que contiene el XML del bar.
     * @return El bar parseado.
     * @throws XMLParsingException Si el fichero no se puede leer o su contenido es inválido.
     */
    public static Bar parseBar(File f) throws XMLParsingException {
        try { return parseBar(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Bar", e); }
    }

    /**
     * Crea un Pub a partir de su representación XML.
     *
     * @param str XML con los elementos nombre, direccion (anidada), descripcion,
     *            horaApertura y horaClausura.
     * @return El pub parseado.
     * @throws XMLParsingException Si el XML está mal formado, falta algún dato
     *                             o las horas no tienen formato válido.
     */
    public static Pub parsePub(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            Direccion dir = parseDireccion(nodeToString(getFirstChildElement("direccion", root)));
            return new Pub(
                getTagValue("nombre", root), dir, getTagValue("descripcion", root),
                LocalTime.parse(getTagValue("horaApertura", root)),
                LocalTime.parse(getTagValue("horaClausura", root))
            );
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Pub", e);
        }
    }

    /**
     * Crea un Pub a partir de un fichero XML.
     *
     * @param f Fichero que contiene el XML del pub.
     * @return El pub parseado.
     * @throws XMLParsingException Si el fichero no se puede leer o su contenido es inválido.
     */
    public static Pub parsePub(File f) throws XMLParsingException {
        try { return parsePub(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Pub", e); }
    }

    /**
     * Crea un Restaurante a partir de su representación XML.
     *
     * @param str XML con los elementos nombre, direccion (anidada), descripcion,
     *            precioEstimado, capacidadMaximaTotal y capacidadMaximaMesa.
     * @return El restaurante parseado.
     * @throws XMLParsingException Si el XML está mal formado, falta algún dato
     *                             o los valores numéricos no son válidos.
     */
    public static Restaurante parseRestaurante(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            Direccion dir = parseDireccion(nodeToString(getFirstChildElement("direccion", root)));
            return new Restaurante(
                getTagValue("nombre", root), dir, getTagValue("descripcion", root),
                Float.parseFloat(getTagValue("precioEstimado", root)),
                Integer.parseInt(getTagValue("capacidadMaximaTotal", root)),
                Integer.parseInt(getTagValue("capacidadMaximaMesa", root))
            );
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Restaurante", e);
        }
    }

    /**
     * Crea un Restaurante a partir de un fichero XML.
     *
     * @param f Fichero que contiene el XML del restaurante.
     * @return El restaurante parseado.
     * @throws XMLParsingException Si el fichero no se puede leer o su contenido es inválido.
     */
    public static Restaurante parseRestaurante(File f) throws XMLParsingException {
        try { return parseRestaurante(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Restaurante", e); }
    }

    /**
     * Crea una Review a partir de su representación XML. La review es autocontenida:
     * incluye el cliente y el local (dentro de un envoltorio {@code <local>}), de
     * modo que puede reconstruirse sin depender de otros ficheros.
     *
     * @param str XML con los elementos cliente, local, valoracion, comentario,
     *            fechaVisita y fechaGeneracion.
     * @return La review parseada.
     * @throws XMLParsingException Si el XML está mal formado, falta algún dato o
     *                             los valores (valoración, fechas) no son válidos.
     */
    public static Review parseReview(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            Cliente cliente = parseCliente(nodeToString(getFirstChildElement("cliente", root)));

            // Usamos parseLocalPolimorfico para mantener la clase original del local
            Local local = parseLocalPolimorfico(getFirstChildElement("local", root));

            return new Review(
                cliente, local,
                Integer.parseInt(getTagValue("valoracion", root)),
                getTagValue("comentario", root),
                LocalDate.parse(getTagValue("fechaVisita", root)),
                LocalDate.parse(getTagValue("fechaGeneracion", root))
            );
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Review", e);
        }
    }

    /**
     * Crea una Review a partir de un fichero XML.
     *
     * @param f Fichero que contiene el XML de la review.
     * @return La review parseada.
     * @throws XMLParsingException Si el fichero no se puede leer o su contenido es inválido.
     */
    public static Review parseReview(File f) throws XMLParsingException {
        try { return parseReview(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Review", e); }
    }

    /**
     * Crea una Contestacion a partir de su representación XML. Incluye el dueño que
     * contesta y la review contestada (anidada), además de su propio comentario y
     * fecha de generación.
     *
     * @param str XML con los elementos dueño, review, comentario y fechaGeneracion.
     * @return La contestación parseada.
     * @throws XMLParsingException Si el XML está mal formado, falta algún dato o
     *                             la fecha no es válida.
     */
    public static Contestacion parseContestacion(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            Dueño dueño = parseDueño(nodeToString(getFirstChildElement("dueño", root)));
            Review review = parseReview(nodeToString(getFirstChildElement("review", root)));
            return new Contestacion(
                dueño, review,
                getTagValue("comentario", root),
                LocalDate.parse(getTagValue("fechaGeneracion", root))
            );
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Contestacion", e);
        }
    }

    /**
     * Crea una Contestacion a partir de un fichero XML.
     *
     * @param f Fichero que contiene el XML de la contestación.
     * @return La contestación parseada.
     * @throws XMLParsingException Si el fichero no se puede leer o su contenido es inválido.
     */
    public static Contestacion parseContestacion(File f) throws XMLParsingException {
        try { return parseContestacion(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Contestacion", e); }
    }

    /**
     * Crea una Reserva a partir de su representación XML. Incluye el cliente y el
     * local reservable (dentro de un envoltorio {@code <local>}).
     *
     * @param str XML con los elementos fecha, hora, descuento, cliente y local.
     * @return La reserva parseada.
     * @throws XMLParsingException Si el XML está mal formado, falta algún dato, los
     *                             valores no son válidos o el local no es reservable.
     */
    public static Reserva parseReserva(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            Cliente cliente = parseCliente(nodeToString(getFirstChildElement("cliente", root)));

            // Usamos parseLocalPolimorfico y lo casteamos a Reservable
            Reservable local = (Reservable) parseLocalPolimorfico(getFirstChildElement("local", root));

            return new Reserva(
                LocalDate.parse(getTagValue("fecha", root)),
                LocalTime.parse(getTagValue("hora", root)),
                Float.parseFloat(getTagValue("descuento", root)),
                cliente, local
            );
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Reserva", e);
        }
    }

    /**
     * Crea una Reserva a partir de un fichero XML.
     *
     * @param f Fichero que contiene el XML de la reserva.
     * @return La reserva parseada.
     * @throws XMLParsingException Si el fichero no se puede leer o su contenido es inválido.
     */
    public static Reserva parseReserva(File f) throws XMLParsingException {
        try { return parseReserva(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Reserva", e); }
    }
}