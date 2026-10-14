package GSILabs.BModel;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Representa un Pub que hereda de Local.
 * @author Aimar Ijurko
 */
public class Pub extends Local {
    
    private LocalTime horaApertura;
    private LocalTime horaClausura;
    
    /**
     * Constructor de Pub.
     * @param nombre el nombre del local
     * @param direccion la dirección del local
     * @param descripcion la descripción del local
     * @param horaApertura la hora de apertura del pub
     * @param horaClausura la hora de clausura del pub
     * @throws IllegalArgumentException si la hora de apertura o la hora de clausura son nulas.
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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), horaApertura, horaClausura);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final Pub other = (Pub) obj;
        if (!Objects.equals(this.horaApertura, other.horaApertura)) {
            return false;
        }
        return Objects.equals(this.horaClausura, other.horaClausura);
    }
}