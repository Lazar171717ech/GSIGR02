package GSILabs.BModel;

import GSILabs.serializable.XMLRepresentable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa una opinión (Review) dejada en el sistema por un Cliente acerca de
 * un Local.
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class Review implements XMLRepresentable {

    private Cliente cliente;
    private Local local;
    private int valoracion;
    private String comentario;
    private LocalDate fechaVisita;
    private LocalDate fechaGeneracion;

    /**
     * Constructor de Review.
     * @param cliente el cliente que realiza la review
     * @param local el local sobre el que se hace la review
     * @param valoracion la valoración numérica
     * @param comentario el comentario de la review
     * @param fechaVisita la fecha en la que se visitó el local
     * @param fechaGeneracion la fecha en la que se genera la review
     * @throws IllegalArgumentException si cliente, local o fechas son nulos, si el valor de valoración no esta entre 0 y 5 o si 
     *                                  el comentario tiene un tamaño mayor que 500.
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

    /**
     * Obtiene el cliente que realizó la review.
     * @return el cliente
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * Establece el cliente que realiza la review.
     * @param cliente el nuevo cliente
     * @throws IllegalArgumentException si el cliente es nulo
     */
    public void setCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        this.cliente = cliente;
    }

    /**
     * Obtiene el local sobre el que se realizó la review.
     * @return el local
     */
    public Local getLocal() {
        return local;
    }

    /**
     * Establece el local de la review.
     * @param local el nuevo local
     * @throws IllegalArgumentException si el local es nulo
     */
    public void setLocal(Local local) {
        if (local == null) {
            throw new IllegalArgumentException("El local no puede ser nulo.");
        }
        this.local = local;
    }

    /**
     * Obtiene la valoración numérica de la review.
     * @return la valoración
     */
    public int getValoracion() {
        return valoracion;
    }

    /**
     * Establece la valoración de la review.
     * @param valoracion la nueva valoración
     * @throws IllegalArgumentException si la valoración no está entre 0 y 5
     */
    public void setValoracion(int valoracion) {
        if (valoracion < 0 || valoracion > 5) {
            throw new IllegalArgumentException("La valoración debe estar comprendida entre 0 y 5 estrellas.");
        }
        this.valoracion = valoracion;
    }

    /**
     * Obtiene el comentario de la review.
     * @return el comentario
     */
    public String getComentario() {
        return comentario;
    }

    /**
     * Establece el comentario de la review.
     * @param comentario el nuevo comentario
     * @throws IllegalArgumentException si el comentario excede los 500 caracteres
     */
    public void setComentario(String comentario) {
        if (comentario != null && comentario.length() > 500) {
            throw new IllegalArgumentException("El comentario no puede exceder los 500 caracteres.");
        }
        this.comentario = comentario;
    }

    /**
     * Obtiene la fecha de la visita al local.
     * @return la fecha de visita
     */
    public LocalDate getFechaVisita() {
        return fechaVisita;
    }

    /**
     * Establece la fecha de visita.
     * @param fechaVisita la nueva fecha de visita
     * @throws IllegalArgumentException si la fecha de visita es nula
     */
    public void setFechaVisita(LocalDate fechaVisita) {
        if (fechaVisita == null) {
            throw new IllegalArgumentException("La fecha de visita no puede ser nula.");
        }
        this.fechaVisita = fechaVisita;
    }

    /**
     * Obtiene la fecha de generación de la review.
     * @return la fecha de generación
     */
    public LocalDate getFechaGeneracion() {
        return fechaGeneracion;
    }

    /**
     * Establece la fecha de generación.
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
        Review review = (Review) o;
        if (this.cliente == null || review.cliente == null) {
            return false;
        }

        // Comparamos el NICK del cliente, local y fecha de visita
        return Objects.equals(this.cliente.getNick(), review.cliente.getNick())
                && Objects.equals(this.local, review.local)
                && Objects.equals(this.fechaVisita, review.fechaVisita);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cliente != null ? cliente.getNick() : null, local, fechaVisita);
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

    /**
     * Genera la representación en formato XML de la review, incluyendo el cliente y el local asociado.
     * @return Cadena de texto con la estructura XML del objeto.
     */
    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<review>\n");
        if (cliente != null) {
            String cliXml = cliente.toXML().replace("\n", "\n    ");
            sb.append("    ").append(cliXml).append("\n");
        }
        if (local != null) {
            sb.append("    <local>\n");
            String locXml = local.toXML().replace("\n", "\n        ");
            sb.append("        ").append(locXml).append("\n");
            sb.append("    </local>\n");
        }
        sb.append("    <valoracion>").append(valoracion).append("</valoracion>\n");
        sb.append("    <comentario>").append(escapeXML(comentario)).append("</comentario>\n");
        sb.append("    <fechaVisita>").append(fechaVisita != null ? fechaVisita.toString() : "").append("</fechaVisita>\n");
        sb.append("    <fechaGeneracion>").append(fechaGeneracion != null ? fechaGeneracion.toString() : "").append("</fechaGeneracion>\n");
        sb.append("</review>");
        return sb.toString();
    }

    /**
     * Guarda la representación XML de la review en el fichero especificado.
     * @param f Fichero de destino.
     * @return true si se guardó con éxito; false en caso de error.
     */
    @Override
    public boolean saveToXML(File f) {
        if (f == null) {
            return false;
        }
        try {
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
                String xml = toXML();
                if (!xml.startsWith("<?xml")) {
                    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                }
                writer.print(xml);
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Guarda la representación XML de la review en la ruta de fichero especificada.
     * @param filePath Ruta del fichero de destino.
     * @return true si se guardó con éxito; false en caso de error o ruta inválida.
     */
    @Override
    public boolean saveToXML(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        return saveToXML(new File(filePath.trim()));
    }

    private static String escapeXML(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}