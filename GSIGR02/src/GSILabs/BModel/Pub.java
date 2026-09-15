/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BModel;

import java.time.LocalTime;
import java.util.Objects;

/**
 *
 * @author alumno
 */
public class Pub extends Local {
    
    private LocalTime horaApertura;
    private LocalTime horaClausura;
    
    public Pub(String nombre, Direccion direccion, String descripcion, 
            LocalTime horaApertura, LocalTime horaClausura) {
        super(nombre, direccion, descripcion);
        this.horaApertura = horaApertura;
        this.horaClausura = horaClausura;
    }

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraClausura() {
        return horaClausura;
    }

    public void setHoraClausura(LocalTime horaClausura) {
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
