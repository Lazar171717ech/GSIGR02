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
 * Representa una Contestación realizada por un Dueño a una Review existente.
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class Contestacion implements XMLRepresentable {

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
        if (this.dueño == null || that.dueño == null) {
            return false;
        }

        // Comparamos el NICK del dueño y la review a la que hace referencia
        return Objects.equals(this.dueño.getNick(), that.getDueño().getNick())
                && Objects.equals(this.review, that.review);
    }

    @Override
    public int hashCode() {
        // Usamos el nick del dueño y la review para el hash
        return Objects.hash(dueño != null ? dueño.getNick() : null, review);
    }

    @Override
    public String toString() {
        return "Contestacion{"
                + "dueño=" + dueño.getNick()
                + ", review=" + review.toString()
                + ", fechaGeneracion=" + fechaGeneracion
                + '}';
    }

    /**
     * Genera la representación en formato XML de la contestación, incluyendo el dueño y la review asociada.
     * @return Cadena de texto con la estructura XML del objeto.
     */
    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<contestacion>\n");
        if (dueño != null) {
            String duenoXml = dueño.toXML().replace("\n", "\n    ");
            sb.append("    ").append(duenoXml).append("\n");
        }
        if (review != null) {
            String revXml = review.toXML().replace("\n", "\n    ");
            sb.append("    ").append(revXml).append("\n");
        }
        sb.append("    <comentario>").append(escapeXML(comentario)).append("</comentario>\n");
        sb.append("    <fechaGeneracion>").append(fechaGeneracion != null ? fechaGeneracion.toString() : "").append("</fechaGeneracion>\n");
        sb.append("</contestacion>");
        return sb.toString();
    }

    /**
     * Guarda la representación XML de la contestación en el fichero especificado.
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
     * Guarda la representación XML de la contestación en la ruta de fichero especificada.
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