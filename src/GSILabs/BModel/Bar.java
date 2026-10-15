package GSILabs.BModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un Bar que hereda de Local y es Reservable.
 * @author Equipo2
 */
public class Bar extends Local implements Reservable {
    
   private List<String> especialidades;
   
    /**
     * Constructor de Bar.
     * @param nombre el nombre del local
     * @param direccion la dirección del local
     * @param descripcion la descripción del local
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
        this.especialidades.add(especialidad);
    }
    
    /**
     * Obtiene la lista de especialidades del bar.
     * @return una lista con las especialidades del bar
     */
    public List<String> getEspecialidades() {
        return especialidades;
    }

    @Override
    public String toString() {
        return "Bar{" + super.toString() + ", especialidades=" + especialidades + '}';
    }

    /**
     * Dos bares son iguales si tienen el mismo nombre y la misma dirección
     * (criterio heredado de Local). Las especialidades son atributos secundarios
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
}