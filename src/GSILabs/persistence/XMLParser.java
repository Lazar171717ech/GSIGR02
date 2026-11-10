package GSILabs.persistence;

import GSILabs.BModel.*;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.xml.XMLConstants;
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
     * Incorpora medidas de seguridad contra ataques XXE y Entity Expansion.
     *
     * @param xml Contenido XML en forma de cadena.
     * @return El documento DOM resultante.
     * @throws Exception Si el XML está mal formado o falla la configuración del parser.
     */
    private static Document getDocumentFromString(String xml) throws Exception {
        if (xml == null || xml.trim().isEmpty()) {
            throw new XMLParsingException("El contenido XML no puede ser nulo ni vacío.");
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (Throwable ignored) {
            // Continuar si la implementación DOM subyacente no admite alguna de las opciones
        }
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml.trim())));
    }

    /**
     * Lee el contenido completo de un fichero de texto codificado en UTF-8.
     *
     * @param f Fichero a leer.
     * @return El contenido del fichero como cadena.
     * @throws Exception Si el fichero es nulo, no existe o no se puede leer.
     */
    private static String readFileToString(File f) throws Exception {
        if (f == null) {
            throw new XMLParsingException("El fichero no puede ser nulo.");
        }
        if (!f.isFile() || !f.exists()) {
            throw new XMLParsingException("El fichero no existe o no es válido: " + f.getPath());
        }
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }

    /**
     * Obtiene el texto del primer hijo directo con la etiqueta indicada.
     *
     * @param tag     Nombre de la etiqueta buscada.
     * @param element Elemento padre.
     * @return El texto sin espacios sobrantes, o cadena vacía si no existe la etiqueta.
     */
    public static String getTagValue(String tag, Element element) {
        if (element == null || tag == null) {
            return "";
        }
        Element child = getFirstChildElement(tag, element);
        return child != null ? child.getTextContent().trim() : "";
    }

    /**
     * Convierte un nodo DOM en su representación textual XML (sin cabecera).
     *
     * @param node Nodo a convertir.
     * @return El XML del nodo como String.
     * @throws Exception Si el nodo es nulo o falla la transformación.
     */
    public static String nodeToString(Node node) throws Exception {
        if (node == null) {
            throw new XMLParsingException("El nodo DOM proporcionado es nulo.");
        }
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
        if (parent == null || tag == null) {
            return null;
        }
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element && ((Element) n).getTagName().equalsIgnoreCase(tag)) {
                return (Element) n;
            }
        }
        return null;
    }

    /**
     * Instancia el tipo concreto de Local (Restaurante, Bar, Pub o Local genérico)
     * a partir del envoltorio {@code <local>} que aparece dentro de una Review o
     * una Reserva, o bien a partir de la propia etiqueta del local.
     *
     * @param wrapper Elemento que contiene el local o es el propio local.
     * @return El local parseado, conservando su clase original.
     * @throws Exception Si el envoltorio es nulo, no contiene ningún tipo de local
     *                   reconocido o el local interior es inválido.
     */
    private static Local parseLocalPolimorfico(Element wrapper) throws Exception {
        if (wrapper == null) {
            throw new XMLParsingException("Elemento wrapper del local nulo");
        }

        String wrapperTag = wrapper.getTagName().toLowerCase();
        // Caso 1: el wrapper es directamente la etiqueta del local concreto
        if ("restaurante".equals(wrapperTag)) return parseRestaurante(nodeToString(wrapper));
        if ("bar".equals(wrapperTag)) return parseBar(nodeToString(wrapper));
        if ("pub".equals(wrapperTag)) return parsePub(nodeToString(wrapper));

        // Caso 2: contenedor <local> que envuelve el tipo específico
        if (getFirstChildElement("restaurante", wrapper) != null) {
            return parseRestaurante(nodeToString(getFirstChildElement("restaurante", wrapper)));
        } else if (getFirstChildElement("bar", wrapper) != null) {
            return parseBar(nodeToString(getFirstChildElement("bar", wrapper)));
        } else if (getFirstChildElement("pub", wrapper) != null) {
            return parsePub(nodeToString(getFirstChildElement("pub", wrapper)));
        } else if (getFirstChildElement("local", wrapper) != null) {
            return parseLocal(nodeToString(getFirstChildElement("local", wrapper)));
        } else if ("local".equals(wrapperTag) && getFirstChildElement("direccion", wrapper) != null) {
            // Local genérico sin sub-envoltorio
            return parseLocal(nodeToString(wrapper));
        }
        throw new XMLParsingException("No se encontró una etiqueta de tipo de local válida dentro del wrapper <" + wrapper.getTagName() + ">.");
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
            if (!"direccion".equalsIgnoreCase(root.getTagName())) {
                throw new XMLParsingException("Se esperaba etiqueta raíz <direccion> pero se encontró <" + root.getTagName() + ">");
            }
            String localidad = getTagValue("localidad", root);
            String provincia = getTagValue("provincia", root);
            String calle = getTagValue("calle", root);
            String numStr = getTagValue("numero", root);
            if (numStr == null || numStr.isEmpty()) {
                throw new XMLParsingException("Falta el número de la dirección.");
            }
            return new Direccion(localidad, provincia, calle, Integer.parseInt(numStr));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Direccion: " + e.getMessage(), e);
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
        try {
            return parseDireccion(readFileToString(f));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error lectura Direccion: " + e.getMessage(), e);
        }
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
            if (!"cliente".equalsIgnoreCase(root.getTagName())) {
                throw new XMLParsingException("Se esperaba etiqueta raíz <cliente> pero se encontró <" + root.getTagName() + ">");
            }
            String nick = getTagValue("nick", root);
            String pass = getTagValue("password", root);
            String fechaStr = getTagValue("fechaNacimiento", root);
            if (fechaStr.isEmpty()) {
                throw new XMLParsingException("Falta la fecha de nacimiento del Cliente.");
            }
            return new Cliente(nick, pass, LocalDate.parse(fechaStr));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Cliente: " + e.getMessage(), e);
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
        try {
            return parseCliente(readFileToString(f));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error lectura Cliente: " + e.getMessage(), e);
        }
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
            if (!"dueño".equalsIgnoreCase(root.getTagName())) {
                throw new XMLParsingException("Se esperaba etiqueta raíz <dueño> pero se encontró <" + root.getTagName() + ">");
            }
            String nick = getTagValue("nick", root);
            String pass = getTagValue("password", root);
            String fechaStr = getTagValue("fechaNacimiento", root);
            if (fechaStr.isEmpty()) {
                throw new XMLParsingException("Falta la fecha de nacimiento del Dueño.");
            }
            return new Dueño(nick, pass, LocalDate.parse(fechaStr));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Dueño: " + e.getMessage(), e);
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
        try {
            return parseDueño(readFileToString(f));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error lectura Dueño: " + e.getMessage(), e);
        }
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
            if (!"local".equalsIgnoreCase(root.getTagName())) {
                throw new XMLParsingException("Se esperaba etiqueta raíz <local> pero se encontró <" + root.getTagName() + ">");
            }
            Element dirEl = getFirstChildElement("direccion", root);
            if (dirEl == null) {
                throw new XMLParsingException("Falta la etiqueta <direccion> en Local.");
            }
            Direccion dir = parseDireccion(nodeToString(dirEl));
            return new Local(getTagValue("nombre", root), dir, getTagValue("descripcion", root));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Local: " + e.getMessage(), e);
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
        try {
            return parseLocal(readFileToString(f));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error lectura Local: " + e.getMessage(), e);
        }
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
            if (!"bar".equalsIgnoreCase(root.getTagName())) {
                throw new XMLParsingException("Se esperaba etiqueta raíz <bar> pero se encontró <" + root.getTagName() + ">");
            }
            Element dirEl = getFirstChildElement("direccion", root);
            if (dirEl == null) {
                throw new XMLParsingException("Falta la etiqueta <direccion> en Bar.");
            }
            Direccion dir = parseDireccion(nodeToString(dirEl));
            Bar bar = new Bar(getTagValue("nombre", root), dir, getTagValue("descripcion", root));

            Element espContainer = getFirstChildElement("especialidades", root);
            if (espContainer != null) {
                for (Node n = espContainer.getFirstChild(); n != null; n = n.getNextSibling()) {
                    if (n instanceof Element && "especialidad".equalsIgnoreCase(((Element) n).getTagName())) {
                        String esp = n.getTextContent();
                        if (esp != null && !esp.trim().isEmpty()) {
                            bar.agregarEspecialidad(esp.trim());
                        }
                    }
                }
            }
            return bar;
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Bar: " + e.getMessage(), e);
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
        try {
            return parseBar(readFileToString(f));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error lectura Bar: " + e.getMessage(), e);
        }
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
            if (!"pub".equalsIgnoreCase(root.getTagName())) {
                throw new XMLParsingException("Se esperaba etiqueta raíz <pub> pero se encontró <" + root.getTagName() + ">");
            }
            Element dirEl = getFirstChildElement("direccion", root);
            if (dirEl == null) {
                throw new XMLParsingException("Falta la etiqueta <direccion> en Pub.");
            }
            Direccion dir = parseDireccion(nodeToString(dirEl));
            String hAp = getTagValue("horaApertura", root);
            String hCl = getTagValue("horaClausura", root);
            if (hAp.isEmpty() || hCl.isEmpty()) {
                throw new XMLParsingException("Faltan las horas de apertura o clausura en Pub.");
            }
            return new Pub(
                getTagValue("nombre", root), dir, getTagValue("descripcion", root),
                LocalTime.parse(hAp),
                LocalTime.parse(hCl)
            );
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Pub: " + e.getMessage(), e);
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
        try {
            return parsePub(readFileToString(f));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error lectura Pub: " + e.getMessage(), e);
        }
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
            if (!"restaurante".equalsIgnoreCase(root.getTagName())) {
                throw new XMLParsingException("Se esperaba etiqueta raíz <restaurante> pero se encontró <" + root.getTagName() + ">");
            }
            Element dirEl = getFirstChildElement("direccion", root);
            if (dirEl == null) {
                throw new XMLParsingException("Falta la etiqueta <direccion> en Restaurante.");
            }
            Direccion dir = parseDireccion(nodeToString(dirEl));
            String prStr = getTagValue("precioEstimado", root);
            String capTotStr = getTagValue("capacidadMaximaTotal", root);
            String capMesaStr = getTagValue("capacidadMaximaMesa", root);
            if (prStr.isEmpty() || capTotStr.isEmpty() || capMesaStr.isEmpty()) {
                throw new XMLParsingException("Faltan campos numéricos en Restaurante.");
            }
            return new Restaurante(
                getTagValue("nombre", root), dir, getTagValue("descripcion", root),
                Float.parseFloat(prStr),
                Integer.parseInt(capTotStr),
                Integer.parseInt(capMesaStr)
            );
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Restaurante: " + e.getMessage(), e);
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
        try {
            return parseRestaurante(readFileToString(f));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error lectura Restaurante: " + e.getMessage(), e);
        }
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
            if (!"review".equalsIgnoreCase(root.getTagName())) {
                throw new XMLParsingException("Se esperaba etiqueta raíz <review> pero se encontró <" + root.getTagName() + ">");
            }
            Element cliEl = getFirstChildElement("cliente", root);
            if (cliEl == null) {
                throw new XMLParsingException("Falta el elemento <cliente> en Review.");
            }
            Cliente cliente = parseCliente(nodeToString(cliEl));

            Element locWrapper = getFirstChildElement("local", root);
            if (locWrapper == null) {
                throw new XMLParsingException("Falta el elemento <local> en Review.");
            }
            Local local = parseLocalPolimorfico(locWrapper);

            String valStr = getTagValue("valoracion", root);
            String fVisStr = getTagValue("fechaVisita", root);
            String fGenStr = getTagValue("fechaGeneracion", root);
            if (valStr.isEmpty() || fVisStr.isEmpty() || fGenStr.isEmpty()) {
                throw new XMLParsingException("Faltan campos obligatorios en Review.");
            }

            return new Review(
                cliente, local,
                Integer.parseInt(valStr),
                getTagValue("comentario", root),
                LocalDate.parse(fVisStr),
                LocalDate.parse(fGenStr)
            );
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Review: " + e.getMessage(), e);
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
        try {
            return parseReview(readFileToString(f));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error lectura Review: " + e.getMessage(), e);
        }
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
            if (!"contestacion".equalsIgnoreCase(root.getTagName())) {
                throw new XMLParsingException("Se esperaba etiqueta raíz <contestacion> pero se encontró <" + root.getTagName() + ">");
            }
            Element dueEl = getFirstChildElement("dueño", root);
            if (dueEl == null) {
                throw new XMLParsingException("Falta el elemento <dueño> en Contestacion.");
            }
            Dueño dueño = parseDueño(nodeToString(dueEl));

            Element revEl = getFirstChildElement("review", root);
            if (revEl == null) {
                throw new XMLParsingException("Falta el elemento <review> en Contestacion.");
            }
            Review review = parseReview(nodeToString(revEl));

            String fGenStr = getTagValue("fechaGeneracion", root);
            if (fGenStr.isEmpty()) {
                throw new XMLParsingException("Falta la fecha de generación en Contestacion.");
            }

            return new Contestacion(
                dueño, review,
                getTagValue("comentario", root),
                LocalDate.parse(fGenStr)
            );
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Contestacion: " + e.getMessage(), e);
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
        try {
            return parseContestacion(readFileToString(f));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error lectura Contestacion: " + e.getMessage(), e);
        }
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
            if (!"reserva".equalsIgnoreCase(root.getTagName())) {
                throw new XMLParsingException("Se esperaba etiqueta raíz <reserva> pero se encontró <" + root.getTagName() + ">");
            }
            Element cliEl = getFirstChildElement("cliente", root);
            if (cliEl == null) {
                throw new XMLParsingException("Falta el elemento <cliente> en Reserva.");
            }
            Cliente cliente = parseCliente(nodeToString(cliEl));

            Element locWrapper = getFirstChildElement("local", root);
            if (locWrapper == null) {
                throw new XMLParsingException("Falta el elemento <local> en Reserva.");
            }
            Local localObj = parseLocalPolimorfico(locWrapper);
            if (!(localObj instanceof Reservable)) {
                throw new XMLParsingException("El local de la reserva debe ser Reservable (Bar o Restaurante).");
            }
            Reservable local = (Reservable) localObj;

            String fStr = getTagValue("fecha", root);
            String hStr = getTagValue("hora", root);
            String descStr = getTagValue("descuento", root);
            if (fStr.isEmpty() || hStr.isEmpty() || descStr.isEmpty()) {
                throw new XMLParsingException("Faltan datos obligatorios en Reserva.");
            }

            return new Reserva(
                LocalDate.parse(fStr),
                LocalTime.parse(hStr),
                Float.parseFloat(descStr),
                cliente, local
            );
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Reserva: " + e.getMessage(), e);
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
        try {
            return parseReserva(readFileToString(f));
        } catch (XMLParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParsingException("Error lectura Reserva: " + e.getMessage(), e);
        }
    }
}
