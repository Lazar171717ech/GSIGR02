package GSILabs.BModel;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Implementación de la clase Local.
 * @author Pablo Lázaro
 */
public class Local {
    
    private String nombre;
    private Direccion direccion;
    private String descripcion;
    
    /**
     * Constructor de la clase Local.
     * @param nombre el nombre del local
     * @param direccion la dirección del local
     * @param descripcion la descripción del local
     * @throws IllegalArgumentException si el nombre o la dirección son nulos
     */
    public Local(String nombre, Direccion direccion, String descripcion){
        if(nombre == null){
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        if(direccion == null){
            throw new IllegalArgumentException("La dirección no puede ser nula");
        }
        
        this.nombre = nombre;
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
}