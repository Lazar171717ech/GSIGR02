package GSILabs.BModel;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa una Contestación realizada por un Dueño a una Review existente.
 * @author Equipo2
 */
public class Contestacion {

    private Dueño dueño;
    private Review review;
    private String comentario;
    private LocalDate fechaGeneracion;

    /**
     * Constructor de Contestacion.
     * @param dueño el dueño que realiza la contestación
     * @param review la review a la que se contesta
     * @param comentario el comentario de la contestación
     * @param fechaGeneracion la fecha de generación de la contestación
     * @throws IllegalArgumentException si el dueño, review o fechaGeneracion son nulos o si 
     *                                  el comentario excede los 500 caracteres.
     */
    public Contestacion(Dueño dueño, Review review, String comentario, LocalDate fechaGeneracion) {
        if (dueño == null || review == null || fechaGeneracion == null) {
            throw new IllegalArgumentException("El dueño, la review y la fecha de generación no pueden ser nulos.");
        }
        if (comentario != null && comentario.length() > 500) {
            throw new IllegalArgumentException("El comentario de la contestación no puede exceder los 500 caracteres.");
        }

        this.dueño = dueño;
        this.review = review;
        this.comentario = comentario;
        this.fechaGeneracion = fechaGeneracion;
    }

    /**
     * Obtiene el dueño que realizó la contestación.
     * @return el dueño
     */
    public Dueño getDueño() {
        return dueño;
    }

    /**
     * Establece el dueño que realiza la contestación.
     * @param dueño el nuevo dueño
     * @throws IllegalArgumentException si el dueño es nulo
     */
    public void setDueño(Dueño dueño) {
        if (dueño == null) {
            throw new IllegalArgumentException("El dueño no puede ser nulo.");
        }
        this.dueño = dueño;
    }

    /**
     * Obtiene la review asociada a la contestación.
     * @return la review
     */
    public Review getReview() {
        return review;
    }

    /**
     * Establece la review de la contestación.
     * @param review la nueva review
     * @throws IllegalArgumentException si la review es nula
     */
    public void setReview(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("La review no puede ser nula.");
        }
        this.review = review;
    }

    /**
     * Obtiene el comentario de la contestación.
     * @return el comentario
     */
    public String getComentario() {
        return comentario;
    }

    /**
     * Establece el comentario de la contestación.
     * @param comentario el nuevo comentario
     * @throws IllegalArgumentException si el comentario excede los 500 caracteres
     */
    public void setComentario(String comentario) {
        if (comentario != null && comentario.length() > 500) {
            throw new IllegalArgumentException("El comentario de la contestación no puede exceder los 500 caracteres.");
        }
        this.comentario = comentario;
    }

    /**
     * Obtiene la fecha de generación de la contestación.
     * @return la fecha de generación
     */
    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    /**
     * Establece la fecha de generación de la contestación.
     * @param fechaGeneracion la nueva fecha de generación
     * @throws IllegalArgumentException si la fecha de generación es nula
     */
    public void setFechaGeneracion(LocalDate fechaGeneracion) {
        if (fechaGeneracion == null) {
            throw new IllegalArgumentException("La fecha de generación no puede ser nula.");
        }
        this.fechaGeneracion = fechaGeneracion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Contestacion that = (Contestacion) o;

        // Comparamos el NICK del dueño y la review a la que hace referencia
        return this.dueño.getNick().equals(that.getDueño().getNick())
                && Objects.equals(this.review, that.review);
    }

    @Override
    public int hashCode() {
        // Usamos el nick del dueño y la review para el hash
        return Objects.hash(dueño.getNick(), review);
    }

    @Override
    public String toString() {
        return "Contestacion{"
                + "dueño=" + dueño.getNick()
                + ", review=" + review.toString()
                + ", fechaGeneracion=" + fechaGeneracion
                + '}';
    }
}