/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author alumno
 */
public class Bar extends Local implements Reservable{
    
   private List<String> especialidades;
    
    public Bar(String nombre, Direccion direccion, String descripcion) {
        super(nombre, direccion, descripcion);
        this.especialidades = new ArrayList<>();
    }

    public void agregarEspecialidad(String especialidad) {
        this.especialidades.add(especialidad);
    }
    
    public List<String> getEspecialidades() {
        return especialidades;
    }

    @Override
    public String toString() {
        return "Bar{" + super.toString() + ", especialidades=" + especialidades + '}';
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), especialidades);
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
        final Bar other = (Bar) obj;
        return Objects.equals(this.especialidades, other.especialidades);
    }
    
    
    
}
