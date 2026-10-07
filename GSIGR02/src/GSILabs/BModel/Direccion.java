/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BModel;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Implementación de la clase Direccion.
 */
public class Direccion {
    
    private String localidad;
    private String provincia;
    private String calle;
    private int numero;

    public Direccion(String localidad, String provincia, String calle, int numero){
        if(localidad == null){
            throw new IllegalArgumentException("La localidad no puede ser nula");
        }
        if(provincia == null){
            throw new IllegalArgumentException("La provincia no puede ser nula");
        }
        if(calle == null){
            throw new IllegalArgumentException("La calle no puede ser nula");
        }
        if(numero <= 0){
            throw new IllegalArgumentException("El numero no puede ser nulo");
        }
        
        this.localidad = localidad;
        this.provincia = provincia;
        this.calle = calle;
        this.numero = numero;
    }

    public String getLocalidad(){
        return localidad;
    }

    public String getProvincia(){
        return provincia;
    }

    public String getCalle(){
        return calle;
    }

    public int getNumero(){
        return numero;
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
        return Objects.hash(localidad, provincia, calle, numero);
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
}
