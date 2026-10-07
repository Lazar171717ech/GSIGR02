/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GSILabs.BModel;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa una opinión (Review) dejada en el sistema por un Cliente acerca de
 * un Local.
 */
public class Review {

    private Cliente cliente;
    private Local local;
    private int valoracion;
    private String comentario;
    private LocalDate fechaVisita;
    private LocalDate fechaGeneracion;

    /**
     * Constructor de Review.
     */
    public Review(Cliente cliente, Local local, int valoracion, String comentario, LocalDate fechaVisita, LocalDate fechaGeneracion) {
        if (cliente == null || local == null || fechaVisita == null || fechaGeneracion == null) {
            throw new IllegalArgumentException("El cliente, el local y las fechas no pueden ser nulos.");
        }
        if (valoracion < 0 || valoracion > 5) {
            throw new IllegalArgumentException("La valoración debe estar comprendida entre 0 y 5 estrellas.");
        }
        if (comentario != null && comentario.length() > 500) {
            throw new IllegalArgumentException("El comentario no puede exceder los 500 caracteres.");
        }

        this.cliente = cliente;
        this.local = local;
        this.valoracion = valoracion;
        this.comentario = comentario;
        this.fechaVisita = fechaVisita;
        this.fechaGeneracion = fechaGeneracion;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Local getLocal() {
        return local;
    }

    public int getValoracion() {
        return valoracion;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDate getFechaVisita() {
        return fechaVisita;
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
        Review review = (Review) o;

        // Comparamos directamente el NICK del cliente en lugar del objeto completo
        return this.cliente.getNick().equals(review.getCliente().getNick())
                && Objects.equals(this.local, review.local)
                && Objects.equals(this.fechaVisita, review.fechaVisita);
    }

    @Override
    public int hashCode() {
        // Usamos el nick para generar el hash
        return Objects.hash(cliente.getNick(), local, fechaVisita);
    }

    @Override
    public String toString() {
        return "Review{"
                + "cliente=" + cliente.getNick()
                + ", local=" + local
                + ", valoracion=" + valoracion
                + ", fechaVisita=" + fechaVisita
                + '}';
    }
}
