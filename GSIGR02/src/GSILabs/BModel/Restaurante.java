package GSILabs.BModel;

/**
 * Representa un Restaurante que hereda de Local y es Reservable.
 * @author Aimar Ijurko
 */
public class Restaurante extends Local implements Reservable{
    
    private float precioEstimado;
    private int capacidadMaximaTotal;
    private int capacidadMaximaMesa;
    
    /**
     * Constructor de Restaurante.
     * @param nombre el nombre del local
     * @param direccion la dirección del local
     * @param descripcion la descripción del local
     * @param precioEstimado el precio estimado del restaurante
     * @param capacidadMaximaTotal la capacidad máxima total de personas
     * @param capacidadMaximaMesa la capacidad máxima por mesa
     * @throws IllegalArgumentException si el precio estimado es negativo, si las capacidades son menores o iguales a 0, 
     *                                  o si la capacidad por mesa supera a la total.
     */
    public Restaurante(String nombre, Direccion direccion, String descripcion, 
            float precioEstimado, int capacidadMaximaTotal, int capacidadMaximaMesa) {
        super(nombre, direccion, descripcion);
        
        if (precioEstimado < 0) {
            throw new IllegalArgumentException("El precio estimado no puede ser negativo.");
        }
        if (capacidadMaximaTotal <= 0 || capacidadMaximaMesa <= 0) {
            throw new IllegalArgumentException("Las capacidades máximas deben ser mayores que cero.");
        }
        if (capacidadMaximaMesa > capacidadMaximaTotal) {
            throw new IllegalArgumentException("La capacidad máxima por mesa no puede superar la capacidad máxima total.");
        }
        
        this.precioEstimado = precioEstimado;
        this.capacidadMaximaTotal = capacidadMaximaTotal;
        this.capacidadMaximaMesa = capacidadMaximaMesa;
    }

    /**
     * Obtiene el precio estimado del restaurante.
     * @return el precio estimado
     */
    public float getPrecioEstimado() {
        return precioEstimado;
    }

    /**
     * Establece el precio estimado del restaurante.
     * @param precioEstimado el nuevo precio estimado
     * @throws IllegalArgumentException si el precio estimado es negativo
     */
    public void setPrecioEstimado(float precioEstimado) {
        if (precioEstimado < 0) {
            throw new IllegalArgumentException("El precio estimado no puede ser negativo.");
        }
        this.precioEstimado = precioEstimado;
    }

    /**
     * Obtiene la capacidad máxima total del restaurante.
     * @return la capacidad máxima total
     */
    public int getCapacidadMaximaTotal() {
        return capacidadMaximaTotal;
    }

    /**
     * Establece la capacidad máxima total del restaurante.
     * @param capacidadMaximaTotal la nueva capacidad máxima total
     * @throws IllegalArgumentException si la capacidad es menor o igual a 0, o menor que la capacidad por mesa actual
     */
    public void setCapacidadMaximaTotal(int capacidadMaximaTotal) {
        if (capacidadMaximaTotal <= 0) {
            throw new IllegalArgumentException("La capacidad máxima total debe ser mayor que cero.");
        }
        if (capacidadMaximaTotal < this.capacidadMaximaMesa) {
            throw new IllegalArgumentException("La capacidad máxima total no puede ser menor que la capacidad máxima por mesa actual.");
        }
        this.capacidadMaximaTotal = capacidadMaximaTotal;
    }

    /**
     * Obtiene la capacidad máxima por mesa del restaurante.
     * @return la capacidad máxima por mesa
     */
    public int getCapacidadMaximaMesa() {
        return capacidadMaximaMesa;
    }

    /**
     * Establece la capacidad máxima por mesa del restaurante.
     * @param capacidadMaximaMesa la nueva capacidad máxima por mesa
     * @throws IllegalArgumentException si la capacidad es menor o igual a 0, o mayor que la capacidad total actual
     */
    public void setCapacidadMaximaMesa(int capacidadMaximaMesa) {
        if (capacidadMaximaMesa <= 0) {
            throw new IllegalArgumentException("La capacidad máxima por mesa debe ser mayor que cero.");
        }
        if (capacidadMaximaMesa > this.capacidadMaximaTotal) {
            throw new IllegalArgumentException("La capacidad máxima por mesa no puede superar la capacidad máxima total actual.");
        }
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