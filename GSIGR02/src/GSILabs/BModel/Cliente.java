
package GSILabs.BModel;

import GSILabs.serializable.XMLRepresentable;
import java.time.LocalDate;

/**
 * Clase que representa a un Cliente (tipo de Usuario).
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class Cliente extends Usuario implements XMLRepresentable {

    /**
     * Constructor de Cliente.
     * @param nick El nick del usuario (debe tener al menos 3 caracteres).
     * @param password La contraseña del usuario.
     * @param fechaNacimiento La fecha de nacimiento (debe ser mayor o igual a 14 años).
     * @throws IllegalArgumentException si el nick, la contraseña o la fecha son inválidos, 
     *                                  o si el usuario es menor de 14 años.
     */
    public Cliente(String nick, String password, LocalDate fechaNacimiento) {
        super(nick, password, fechaNacimiento);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "nick='" + getNick() + '\'' +
                ", fechaNacimiento=" + getFechaNacimiento() +
                '}';
    }

    /**
     * Genera la representación en formato XML del cliente.
     * @return Cadena de texto con la estructura XML del objeto.
     */
    @Override
    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<cliente>\n");
        sb.append("    <nick>").append(escapeXML(getNick())).append("</nick>\n");
        sb.append("    <password>").append(escapeXML(getPassword())).append("</password>\n");
        sb.append("    <fechaNacimiento>").append(getFechaNacimiento() != null ? getFechaNacimiento().toString() : "").append("</fechaNacimiento>\n");
        sb.append("</cliente>");
        return sb.toString();
    }
}