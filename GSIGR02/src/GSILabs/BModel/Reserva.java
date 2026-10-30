package GSILabs.BModel;

import GSILabs.serializable.XMLRepresentable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Representa una Reserva realizada por un Cliente en un Local reservable.
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class Reserva implements XMLRepresentable {
    private LocalDate fecha;
    private LocalTime hora;
    private float descuento;
    private Cliente cliente;
    private Reservable local;

    /**
     * Constructor de Reserva.
     * @param fecha la fecha de la reserva
     * @param hora la hora de la reserva
     * @param descuento el descuento aplicado a la reserva
     * @param cliente el cliente que realiza la reserva
     * @param local el local reservable
     * @throws IllegalArgumentException si la fecha, hora, cliente o local son nulos, 
     *                                  o si el descuento es menor que 0 o mayor que 100.
     */
    public Reserva(LocalDate fecha, LocalTime hora, float descuento, Cliente cliente, Reservable local) {
        if (fecha == null || hora == null || cliente == null || local == null) {
            throw new IllegalArgumentException("La fecha, la hora, el cliente y el local no pueden ser nulos.");
        }
        if (descuento < 0 || descuento > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100.");
        }
        
        this.fecha = fecha;
        this.hora = hora;
        this.descuento = descuento;
        this.cliente = cliente;
        this.local = local;
    }

    /**
     * Obtiene la fecha de la reserva.
     * @return la fecha de la reserva
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha de la reserva.
     * @param fecha la nueva fecha
     * @throws IllegalArgumentException si la fecha es nula
     */
    public void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula.");
        }
        this.fecha = fecha;
    }

    /**
     * Obtiene la hora de la reserva.
     * @return la hora de la reserva
     */
    public LocalTime getHora() {
        return hora;
    }

    /**
     * Establece la hora de la reserva.
     * @param hora la nueva hora
     * @throws IllegalArgumentException si la hora es nula
     */
    public void setHora(LocalTime hora) {
        if (hora == null) {
            throw new IllegalArgumentException("La hora no puede ser nula.");
        }
        this.hora = hora;
    }

    /**
     * Obtiene el descuento aplicado a la reserva.
     * @return el descuento de la reserva
     */
    public float getDescuento() {
        return descuento;
    }

    /**
     * Establece el descuento de la reserva.
     * @param descuento el nuevo descuento (entre 0 y 100)
     * @throws IllegalArgumentException si el descuento es menor que 0 o mayor que 100
     */
    public void setDescuento(float descuento) {
        if (descuento < 0 || descuento > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100.");
        }
        this.descuento = descuento;
    }

    /**
     * Obtiene el cliente que realizó la reserva.
     * @return el cliente de la reserva
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * Establece el cliente que realiza la reserva.
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
     * Obtiene el local reservable asociado a la reserva.
     * @return el local reservable
     */
    public Reservable getLocal() {
        return local;
    }

    /**
     * Establece el local reservable.
     * @param local el nuevo local
     * @throws IllegalArgumentException si el local es nulo
     */
    public void setLocal(Reservable local) {
        if (local == null) {
            throw new IllegalArgumentException("El local no puede ser nulo.");
        }
        this.local = local;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(fecha, hora, descuento, cliente, local);
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
        final Reserva other = (Reserva) obj;
        if (Float.floatToIntBits(this.descuento) != Float.floatToIntBits(other.descuento)) {
            return false;
        }
        if (!Objects.equals(this.fecha, other.fecha)) {
            return false;
        }
        if (!Objects.equals(this.hora, other.hora)) {
            return false;
        }
        if (!Objects.equals(this.cliente, other.cliente)) {
            return false;
        }
        return Objects.equals(this.local, other.local);
    }

    @Override
    public String toString() {
        return "Reserva{"  + "fecha=" + fecha + ", hora=" + hora + ", descuento=" + descuento + ", cliente=" + cliente + ", local=" + local + '}';
    }

    /**
     * Genera la representación en formato XML de la reserva, incluyendo el cliente y el local asociado.
     * @return Cadena de texto con la estructura XML del objeto.
     */
    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<reserva>\n");
        sb.append("    <fecha>").append(fecha != null ? fecha.toString() : "").append("</fecha>\n");
        sb.append("    <hora>").append(hora != null ? hora.toString() : "").append("</hora>\n");
        sb.append("    <descuento>").append(descuento).append("</descuento>\n");
        if (cliente != null) {
            String cliXml = cliente.toXML().replace("\n", "\n    ");
            sb.append("    ").append(cliXml).append("\n");
        }
        if (local != null) {
            sb.append("    <local>\n");
            String locXml = (local instanceof XMLRepresentable) ? ((XMLRepresentable) local).toXML() : "";
            locXml = locXml.replace("\n", "\n        ");
            sb.append("        ").append(locXml).append("\n");
            sb.append("    </local>\n");
        }
        sb.append("</reserva>");
        return sb.toString();
    }

    /**
     * Guarda la representación XML de la reserva en el fichero especificado.
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
     * Guarda la representación XML de la reserva en la ruta de fichero especificada.
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
}