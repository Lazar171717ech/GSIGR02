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
 * Implementación de la clase Local.
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class Local implements XMLRepresentable {
    
    private String nombre;
    private Direccion direccion;
    private String descripcion;
    
    /**
     * Constructor de la clase Local.
     * @param nombre el nombre del local
     * @param direccion la dirección del local
     * @param descripcion la descripción del local
     * @throws IllegalArgumentException si el nombre es nulo o vacío, o la dirección es nula
     */
    public Local(String nombre, Direccion direccion, String descripcion){
        if(nombre == null || nombre.trim().isEmpty()){
            throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío");
        }
        if(direccion == null){
            throw new IllegalArgumentException("La dirección no puede ser nula");
        }
        
        this.nombre = nombre.trim();
        this.direccion = direccion;
        this.setDescripcion(descripcion);
    }
    
    /**
     * Obtiene el nombre del local.
     * @return el nombre del local
     */
    public String getNombre(){
        return nombre;
    }
    
    /**
     * Obtiene la dirección del local.
     * @return la dirección del local
     */
    public Direccion getDireccion(){
        return direccion;
    }
    
    /**
     * Obtiene la descripción del local.
     * @return la descripción del local
     */
    public String getDescripcion(){
        return descripcion;
    }
    
    /**
     * Establece la descripción del local.
     * @param descripcion la nueva descripción del local
     * @throws IllegalArgumentException si el tamaño de la descripción es mayor que 300 caracteres
     */
    public void setDescripcion(String descripcion){
        if (descripcion == null) {
            descripcion = "";
        }
        if (descripcion.length() > 300){
            throw new IllegalArgumentException("La descripción no puede superar los 300 caracteres");
        }
        this.descripcion = descripcion;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nombre, direccion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Local other = (Local) obj;
        return Objects.equals(this.nombre, other.nombre) && Objects.equals(this.direccion, other.direccion);
    }
    
    @Override
    public String toString(){
        return "Local{"
                + "nombre=" + nombre
                + ", direccion=" + direccion.toString()
                + ", descripcion=" + descripcion
                + "}";
    }

    /**
     * Genera la representación en formato XML del local.
     * @return Cadena de texto con la estructura XML del objeto.
     */
    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<local>\n");
        sb.append("    <nombre>").append(escapeXML(nombre)).append("</nombre>\n");
        if (direccion != null) {
            String dirXml = direccion.toXML().replace("\n", "\n    ");
            sb.append("    ").append(dirXml).append("\n");
        }
        sb.append("    <descripcion>").append(escapeXML(descripcion)).append("</descripcion>\n");
        sb.append("</local>");
        return sb.toString();
    }

    /**
     * Guarda la representación XML del local en el fichero especificado.
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
     * Guarda la representación XML del local en la ruta de fichero especificada.
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

    protected static String escapeXML(String input) {
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