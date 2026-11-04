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


public class XMLParser {

    private static Document getDocumentFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private static String readFileToString(File f) throws Exception {
        return new String(Files.readAllBytes(f.toPath()), "UTF-8");
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }

    private static String nodeToString(Node node) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

    private static Element getFirstChildElement(String tag, Element parent) {
        NodeList nodeList = parent.getElementsByTagName(tag);
        if (nodeList != null && nodeList.getLength() > 0) {
            return (Element) nodeList.item(0);
        }
        return null;
    }

    // Instanciar el tipo correcto de Local basándose en las etiquetas hijas del envoltorio XML.

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
    public static Direccion parseDireccion(File f) throws XMLParsingException {
        try { return parseDireccion(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Direccion", e); }
    }

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
    public static Cliente parseCliente(File f) throws XMLParsingException {
        try { return parseCliente(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Cliente", e); }
    }

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
    public static Dueño parseDueño(File f) throws XMLParsingException {
        try { return parseDueño(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Dueño", e); }
    }

    public static Local parseLocal(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            Direccion dir = parseDireccion(nodeToString(getFirstChildElement("direccion", root)));
            return new Local(getTagValue("nombre", root), dir, getTagValue("descripcion", root));
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Local", e);
        }
    }
    public static Local parseLocal(File f) throws XMLParsingException {
        try { return parseLocal(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Local", e); }
    }

    public static Bar parseBar(String str) throws XMLParsingException {
        try {
            Element root = getDocumentFromString(str).getDocumentElement();
            Direccion dir = parseDireccion(nodeToString(getFirstChildElement("direccion", root)));
            Bar bar = new Bar(getTagValue("nombre", root), dir, getTagValue("descripcion", root));
            
            NodeList espNodes = root.getElementsByTagName("especialidad");
            for (int i = 0; i < espNodes.getLength(); i++) {
                bar.agregarEspecialidad(espNodes.item(i).getTextContent().trim());
            }
            return bar;
        } catch (Exception e) {
            throw new XMLParsingException("Error al parsear Bar", e);
        }
    }
    public static Bar parseBar(File f) throws XMLParsingException {
        try { return parseBar(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Bar", e); }
    }

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
    public static Pub parsePub(File f) throws XMLParsingException {
        try { return parsePub(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Pub", e); }
    }

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
    public static Restaurante parseRestaurante(File f) throws XMLParsingException {
        try { return parseRestaurante(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Restaurante", e); }
    }

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
    public static Review parseReview(File f) throws XMLParsingException {
        try { return parseReview(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Review", e); }
    }

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
    public static Contestacion parseContestacion(File f) throws XMLParsingException {
        try { return parseContestacion(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Contestacion", e); }
    }

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
    public static Reserva parseReserva(File f) throws XMLParsingException {
        try { return parseReserva(readFileToString(f)); } catch (Exception e) { throw new XMLParsingException("Error lectura Reserva", e); }
    }
}