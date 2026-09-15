/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BModel;

/**
 *
 * @author alumno
 */
public class Restaurante extends Local implements Reservable{
    
    private float precioEstimado;
    private int capacidadMaximaTotal;
    private int capacidadMaximaMesa;
    
    public Restaurante(String nombre, Direccion direccion, String descripcion, 
            float precioEstimado, int capacidadMaximaTotal, int capacidadMaximaMesa) {
        super(nombre, direccion, descripcion);
        this.precioEstimado = precioEstimado;
        this.capacidadMaximaTotal = capacidadMaximaTotal;
        this.capacidadMaximaMesa = capacidadMaximaMesa;
    }

    public float getPrecioEstimado() {
        return precioEstimado;
    }

    public void setPrecioEstimado(float precioEstimado) {
        this.precioEstimado = precioEstimado;
    }

    public int getCapacidadMaximaTotal() {
        return capacidadMaximaTotal;
    }

    public void setCapacidadMaximaTotal(int capacidadMaximaTotal) {
        this.capacidadMaximaTotal = capacidadMaximaTotal;
    }

    public int getCapacidadMaximaMesa() {
        return capacidadMaximaMesa;
    }

    public void setCapacidadMaximaMesa(int capacidadMaximaMesa) {
        this.capacidadMaximaMesa = capacidadMaximaMesa;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), precioEstimado, capacidadMaximaTotal, capacidadMaximaMesa);
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
        final Restaurante other = (Restaurante) obj;
        if (Float.floatToIntBits(this.precioEstimado) != Float.floatToIntBits(other.precioEstimado)) {
            return false;
        }
        if (this.capacidadMaximaTotal != other.capacidadMaximaTotal) {
            return false;
        }
        return this.capacidadMaximaMesa == other.capacidadMaximaMesa;
    }

    @Override
    public String toString() {
        return "Restaurante{" + super.toString() + ", precioEstimado=" + precioEstimado + ", capacidadMaximaTotal=" + capacidadMaximaTotal + ", capacidadMaximaMesa=" + capacidadMaximaMesa + '}';
    }
    
    
    
}
