/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BModel;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Implementación de la clase Local.
 */
public class Local {
    
    private String nombre;
    private Direccion direccion;
    private String descripcion;
    
    public Local(String nombre, Direccion direccion, String descripcion){
        if(nombre == null){
            throw new IllegalArgumentException("El nombre no puede ser nulo");
        }
        if(direccion == null){
            throw new IllegalArgumentException("La direccion no puede ser nula");
        }
        
        this.nombre = nombre;
        this.direccion = direccion;
        this.setDescripcion(descripcion);
    }
    
    public String getNombre(){
        return nombre;
    }
    
    public Direccion getDireccion(){
        return direccion;
    }
    
    public String getDescripcion(){
        return descripcion;
    }

    public void setDescripcion(String descripcion){
        if (descripcion == null) {
            descripcion = "";
        }
        if (descripcion.length() > 300){
            throw new IllegalArgumentException("La descipcion no puede superar los 300 caracteres");
        }
        this.descripcion = descripcion;
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        Local local = (Local) o;
        return direccion.equals(local.direccion);
    }
    
    @Override
    public int hashCode(){
        return Objects.hash(direccion);
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