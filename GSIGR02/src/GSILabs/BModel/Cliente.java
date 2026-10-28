
package GSILabs.BModel;

import java.time.LocalDate;

/**
 * Clase que representa a un Cliente (tipo de Usuario).
 * @author Joel Vilchez, Urko Inés, Aimar Ijurko, Iker Fernandez, Pablo Lázaro, Pablo Mosquera, Juan Garbayo
 */
public class Cliente extends Usuario {

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
}