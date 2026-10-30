package GSILabs.BModel;

import GSILabs.serializable.XMLRepresentable;
import java.time.LocalTime;

/**
 * Representa un Pub que hereda de Local.
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class Pub extends Local implements XMLRepresentable {
    
    private LocalTime horaApertura;
    private LocalTime horaClausura;
    
    /**
     * Constructor de Pub.
     * @param nombre el nombre del local
     * @param direccion la dirección del local
     * @param descripcion la descripción del local
     * @param horaApertura la hora de apertura del pub
     * @param horaClausura la hora de clausura del pub
     * @throws IllegalArgumentException si la hora de apertura o la hora de clausura son nulas,
     *                                  o si el nombre es nulo o vacío, la dirección es nula,
     *                                  o la descripción supera los 300 caracteres.
     */
    public Pub(String nombre, Direccion direccion, String descripcion, 
            LocalTime horaApertura, LocalTime horaClausura) {
        super(nombre, direccion, descripcion);
        
        if (horaApertura == null || horaClausura == null) {
            throw new IllegalArgumentException("La hora de apertura y la hora de clausura no pueden ser nulas.");
        }
        
        this.horaApertura = horaApertura;
        this.horaClausura = horaClausura;
    }

    /**
     * Obtiene la hora de apertura del pub.
     * @return la hora de apertura
     */
    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    /**
     * Establece la hora de apertura del pub.
     * @param horaApertura la nueva hora de apertura
     * @throws IllegalArgumentException si la hora de apertura es nula
     */
    public void setHoraApertura(LocalTime horaApertura) {
        if (horaApertura == null) {
            throw new IllegalArgumentException("La hora de apertura no puede ser nula.");
        }
        this.horaApertura = horaApertura;
    }

    /**
     * Obtiene la hora de clausura del pub.
     * @return la hora de clausura
     */
    public LocalTime getHoraClausura() {
        return horaClausura;
    }

    /**
     * Establece la hora de clausura del pub.
     * @param horaClausura la nueva hora de clausura
     * @throws IllegalArgumentException si la hora de clausura es nula
     */
    public void setHoraClausura(LocalTime horaClausura) {
        if (horaClausura == null) {
            throw new IllegalArgumentException("La hora de clausura no puede ser nula.");
        }
        this.horaClausura = horaClausura;
    }

    @Override
    public String toString() {
        return "Pub{" + super.toString() + ", horaApertura=" + horaApertura + ", horaClausura=" + horaClausura + '}';
    }

    /**
     * Dos pubs son iguales si tienen el mismo nombre y la misma dirección
     * (criterio heredado de Local). Los horarios son atributos secundarios
     * que no forman parte de la identidad del local.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

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
     * Genera la representación en formato XML del pub, incluyendo sus horarios.
     * @return Cadena de texto con la estructura XML del objeto.
     */
    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<pub>\n");
        sb.append("    <nombre>").append(escapeXML(getNombre())).append("</nombre>\n");
        if (getDireccion() != null) {
            String dirXml = getDireccion().toXML().replace("\n", "\n    ");
            sb.append("    ").append(dirXml).append("\n");
        }
        sb.append("    <descripcion>").append(escapeXML(getDescripcion())).append("</descripcion>\n");
        sb.append("    <horaApertura>").append(horaApertura != null ? horaApertura.toString() : "").append("</horaApertura>\n");
        sb.append("    <horaClausura>").append(horaClausura != null ? horaClausura.toString() : "").append("</horaClausura>\n");
        sb.append("</pub>");
        return sb.toString();
    }
}