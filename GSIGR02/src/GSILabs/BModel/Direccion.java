package GSILabs.BModel;

import GSILabs.serializable.XMLRepresentable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Representación de la clase dirección.
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class Direccion implements XMLRepresentable {
    
    private String localidad;
    private String provincia;
    private String calle;
    private int numero;

    /**
     * Constructor de Direccion.
     * @param localidad la localidad de la dirección
     * @param provincia la provincia de la dirección
     * @param calle la calle de la dirección
     * @param numero el número de la calle
     * @throws IllegalArgumentException si la localidad, provincia o calle son nulas o vacías, 
     *                                  o si el número es menor o igual a 0.
     */
    public Direccion(String localidad, String provincia, String calle, int numero){
        if(localidad == null || localidad.trim().isEmpty()){
            throw new IllegalArgumentException("La localidad no puede ser nula ni vacía.");
        }
        if(provincia == null || provincia.trim().isEmpty()){
            throw new IllegalArgumentException("La provincia no puede ser nula ni vacía.");
        }
        if(calle == null || calle.trim().isEmpty()){
            throw new IllegalArgumentException("La calle no puede ser nula ni vacía.");
        }
        if(numero <= 0){
            throw new IllegalArgumentException("El número debe ser mayor que cero.");
        }
        
        this.localidad = localidad.trim();
        this.provincia = provincia.trim();
        this.calle = calle.trim();
        this.numero = numero;
    }

    /**
     * Obtiene la localidad de la dirección.
     * @return la localidad
     */
    public String getLocalidad(){
        return localidad;
    }

    /**
     * Establece la localidad.
     * @param localidad la nueva localidad
     * @throws IllegalArgumentException si la localidad es nula o vacía
     */
    public void setLocalidad(String localidad) {
        if(localidad == null || localidad.trim().isEmpty()){
            throw new IllegalArgumentException("La localidad no puede ser nula ni vacía.");
        }
        this.localidad = localidad.trim();
    }

    /**
     * Obtiene la provincia de la dirección.
     * @return la provincia
     */
    public String getProvincia(){
        return provincia;
    }

    /**
     * Establece la provincia.
     * @param provincia la nueva provincia
     * @throws IllegalArgumentException si la provincia es nula o vacía
     */
    public void setProvincia(String provincia) {
        if(provincia == null || provincia.trim().isEmpty()){
            throw new IllegalArgumentException("La provincia no puede ser nula ni vacía.");
        }
        this.provincia = provincia.trim();
    }

    /**
     * Obtiene la calle de la dirección.
     * @return la calle
     */
    public String getCalle(){
        return calle;
    }

    /**
     * Establece la calle.
     * @param calle la nueva calle
     * @throws IllegalArgumentException si la calle es nula o vacía
     */
    public void setCalle(String calle) {
        if(calle == null || calle.trim().isEmpty()){
            throw new IllegalArgumentException("La calle no puede ser nula ni vacía.");
        }
        this.calle = calle.trim();
    }

    /**
     * Obtiene el número de la calle.
     * @return el número
     */
    public int getNumero(){
        return numero;
    }

    /**
     * Establece el número.
     * @param numero el nuevo número
     * @throws IllegalArgumentException si el número es menor o igual a 0
     */
    public void setNumero(int numero) {
        if(numero <= 0){
            throw new IllegalArgumentException("El número debe ser mayor que cero.");
        }
        this.numero = numero;
    }

    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        Direccion that = (Direccion) o;
        return numero == that.numero
            && localidad.equalsIgnoreCase(that.localidad)
            && provincia.equalsIgnoreCase(that.provincia)
            && calle.equalsIgnoreCase(that.calle);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(
            localidad == null ? null : localidad.toLowerCase(), 
            provincia == null ? null : provincia.toLowerCase(), 
            calle == null ? null : calle.toLowerCase(), 
            numero
        );
    }
    
    @Override
    public String toString() {
        return "Direccion{"
                + "localidad=" + localidad
                + ", provincia=" + provincia
                + ", calle=" + calle
                + ", numero=" + numero
                + "}";
    }

    /**
     * Genera la representación en formato XML de la dirección.
     * @return Cadena de texto con la estructura XML del objeto.
     */
    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<direccion>\n");
        sb.append("    <localidad>").append(escapeXML(localidad)).append("</localidad>\n");
        sb.append("    <provincia>").append(escapeXML(provincia)).append("</provincia>\n");
        sb.append("    <calle>").append(escapeXML(calle)).append("</calle>\n");
        sb.append("    <numero>").append(numero).append("</numero>\n");
        sb.append("</direccion>");
        return sb.toString();
    }

    /**
     * Guarda la representación XML de la dirección en el fichero especificado.
     * @param f Fichero de destino.
     * @return true si se guardó con éxito; false en caso de error.
     */
    @Override
    public boolean saveToXML(File f) {
        if (f == null) {
            return false;
        }
        try {
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
                String xml = toXML();
                if (!xml.startsWith("<?xml")) {
                    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                }
                writer.print(xml);
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Guarda la representación XML de la dirección en la ruta de fichero especificada.
     * @param filePath Ruta del fichero de destino.
     * @return true si se guardó con éxito; false en caso de error o ruta inválida.
     */
    @Override
    public boolean saveToXML(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        return saveToXML(new File(filePath.trim()));
    }

    private static String escapeXML(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}