package GSILabs.BModel;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/**
 * Clase abstracta Usuario, esto se debe a que no puede existir un Usuario, sino que un Cliente o un Dueño.
 * @author Aimar Ijurko
 */
public abstract class Usuario {
    
    private String nick;
    private String password;
    private LocalDate fechaNacimiento;

    /**
     * Constructor de Usuario.
     * @param nick El nick del usuario (debe tener al menos 3 caracteres).
     * @param password La contraseña del usuario.
     * @param fechaNacimiento La fecha de nacimiento (debe ser mayor o igual a 14 años).
     * @throws IllegalArgumentException si el nick, la contraseña o la fecha son inválidos, 
     *                                  o si el usuario es menor de 14 años.
     */
    public Usuario(String nick, String password, LocalDate fechaNacimiento) {
        setNick(nick);
        setPassword(password);
        setFechaNacimiento(fechaNacimiento);
    }

    /**
     * Obtiene el nick del usuario.
     * @return el nick del usuario
     */
    public String getNick() {
        return nick;
    }

    /**
     * Establece el nick del usuario.
     * @param nick Nombre de usuario.
     * @throws IllegalArgumentException si el nick del usuario es nulo o de tamaño menor que 3.
     */
    public void setNick(String nick) {
        if (nick == null || nick.trim().length() < 3) {
            throw new IllegalArgumentException("El nick debe tener al menos tres caracteres.");
        }
        this.nick = nick.trim(); // Limpiamos espacios del principio y final.
    }

    /**
     * Obtiene la contraseña del usuario.
     * @return la contraseña del usuario
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña del usuario.
     * @param password La nueva contraseña.
     * @throws IllegalArgumentException si la contraseña es nula o está vacía.
     */
    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula ni vacía.");
        }
        this.password = password;
    }

    /**
     * Obtiene la fecha de nacimiento del usuario.
     * @return la fecha de nacimiento
     */
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Establece la fecha de nacimiento, validando que no sea nula y la edad mínima (>= 14 años).
     * @param fechaNacimiento Fecha de nacimiento del usuario.
     * @throws IllegalArgumentException si la fecha es nula o el usuario tiene menos de 14 años.
     */
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula.");
        }
        // Validamos que tenga al menos 14 años
        if (Period.between(fechaNacimiento, LocalDate.now()).getYears() < 14) {
            throw new IllegalArgumentException("El usuario debe tener al menos 14 años.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nick='" + nick + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Usuario other = (Usuario) obj;
        return Objects.equals(this.nick, other.nick);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nick);
    }
}