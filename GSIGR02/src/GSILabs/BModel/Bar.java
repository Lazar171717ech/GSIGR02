package GSILabs.BModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa un Bar que hereda de Local y es Reservable.
 * @author Aimar Ijurko
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