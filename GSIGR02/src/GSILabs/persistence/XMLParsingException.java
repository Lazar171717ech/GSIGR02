package GSILabs.persistence;

/**
 * Excepción personalizada que se lanza cuando se detecta un error, 
 * malformación o incumplimiento de la política de negocio al analizar un XML.
 * 
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class XMLParsingException extends Exception {

    /**
     * Construye una nueva excepción de parseo XML con un mensaje descriptivo.
     * 
     * @param message El detalle del error encontrado.
     */
    public XMLParsingException(String message) {
        super(message);
    }

    /**
     * Construye una nueva excepción de parseo XML con un mensaje y una causa raíz.
     * 
     * @param message El detalle del error encontrado.
     * @param cause   La excepción original que provocó el fallo (ej. IOException o SAXException).
     */
    public XMLParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}