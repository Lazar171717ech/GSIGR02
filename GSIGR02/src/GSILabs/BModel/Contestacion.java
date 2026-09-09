/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BModel;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa una Contestación realizada por un Dueño a una Review existente.
 */
public class Contestacion {

    private Dueño dueño;
    private Review review;
    private String comentario;
    private LocalDate fechaGeneracion;

    /**
     * Constructor de Contestacion.
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

    public Dueño getDueño() {
        return dueño;
    }

    public Review getReview() {
        return review;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
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
