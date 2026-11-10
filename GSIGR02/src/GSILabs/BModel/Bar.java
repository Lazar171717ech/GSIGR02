package GSILabs.BModel;

import GSILabs.serializable.XMLRepresentable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un Bar que hereda de Local y es Reservable.
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class Bar extends Local implements Reservable, XMLRepresentable {
    
   private List<String> especialidades;
   
    /**
     * Constructor de Bar.
     * @param nombre el nombre del local
     * @param direccion la dirección del local
     * @param descripcion la descripción del local
     * @throws IllegalArgumentException si el nombre es nulo o vacío, la dirección es nula,
     *                                  o la descripción supera los 300 caracteres.
     */
    public Bar(String nombre, Direccion direccion, String descripcion) {
        super(nombre, direccion, descripcion);
        this.especialidades = new ArrayList<>();
    }

    /**
     * Agrega una nueva especialidad a la lista del bar.
     * @param especialidad la especialidad a añadir
     * @throws IllegalArgumentException si la especialidad es nula o está vacía
     */
    public void agregarEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.trim().isEmpty()) {
            throw new IllegalArgumentException("La especialidad no puede ser nula ni vacía.");
        }
        String tag = especialidad.trim();
        if (!this.especialidades.contains(tag)) {
            this.especialidades.add(tag);
        }
    }
    
    /**
     * Obtiene la lista de especialidades del bar.
     * @return una lista con las especialidades del bar
     */
    public List<String> getEspecialidades() {
        return especialidades;
    }

    /**
     * Devuelve una representación textual del objeto.
     *
     * @return Cadena descriptiva con los datos principales del objeto.
     */
    @Override
    public String toString() {
        return "Bar{" + super.toString() + ", especialidades=" + especialidades + '}';
    }

    /**
     * Calcula el código hash del bar a partir de los atributos que usa
     * {@link #equals(Object)} (nombre y dirección, heredados de Local).
     *
     * @return El código hash del objeto.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Compara este bar con otro objeto. Dos bares son iguales si son de la
     * misma clase y coinciden en nombre y dirección; las especialidades no
     * intervienen en la comparación.
     *
     * @param obj Objeto con el que comparar.
     * @return true si ambos objetos son iguales; false en caso contrario.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Genera la representación en formato XML del bar, incluyendo sus especialidades.
     * @return Cadena de texto con la estructura XML del objeto.
     */
    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<bar>\n");
        sb.append("    <nombre>").append(escapeXML(getNombre())).append("</nombre>\n");
        if (getDireccion() != null) {
            String dirXml = getDireccion().toXML().replace("\n", "\n    ");
            sb.append("    ").append(dirXml).append("\n");
        }
        sb.append("    <descripcion>").append(escapeXML(getDescripcion())).append("</descripcion>\n");
        sb.append("    <especialidades>\n");
        if (especialidades != null) {
            for (String esp : especialidades) {
                sb.append("        <especialidad>").append(escapeXML(esp)).append("</especialidad>\n");
            }
        }
        sb.append("    </especialidades>\n");
        sb.append("</bar>");
        return sb.toString();
    }
}